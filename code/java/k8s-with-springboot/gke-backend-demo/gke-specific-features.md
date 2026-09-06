# What's Actually GKE-Specific Here

Everything in [k8s/](./k8s/) is real Kubernetes YAML — a `Deployment`/`Service`/`Ingress`
looks the same on any cluster. What differs on GKE is (a) three CRDs that only exist
because GKE's Ingress controller is backed by Google's actual load balancer product,
and (b) how identity and secrets flow from GCP into the cluster. This doc walks through
each, and contrasts it with the equivalent already documented for the minikube demo in
[secrets-tls-demo-app](../secrets-tls-demo-app).

## 1. Workload Identity — pods that *are* an IAM identity

**The problem it solves:** every other cluster's answer to "how does my pod call a
cloud API" is a downloaded JSON service-account key, mounted as a Secret or baked into
an image. That key never expires on its own, works from anywhere it's copied to, and
is one `kubectl get secret` away from being exfiltrated.

**How GKE solves it:** a Kubernetes ServiceAccount (KSA) is bound to a Google Cloud IAM
ServiceAccount (GSA) with `iam.gke.io/gcp-service-account` on the KSA plus a
`roles/iam.workloadIdentityUser` binding the other way (`01-serviceaccount.yaml`,
README step 5). Once bound, any pod running as that KSA automatically gets short-lived
GCP credentials for the GSA from GKE's metadata server — no key file exists anywhere,
ever. Revoking access is deleting the IAM binding, not rotating a key.

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: gke-backend-demo
  annotations:
    iam.gke.io/gcp-service-account: gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com
```

Every other pattern in this repo for "how does a pod get a credential" — the
[secrets-management doc](../../../../concepts/secrets-management/Readme.md)'s ESO and Vault
Agent Injector patterns — solves a more general version of this problem for *any*
cluster. Workload Identity solves it specifically for *GCP API access from GKE*, and is
strictly better than either when that's the actual need, because there's no secret
material syncing into the cluster at all — the credential is minted on demand and never
persisted anywhere.

## 2. Cloud SQL Auth Proxy sidecar — no DB password, no VPC peering setup

**The problem it solves:** connecting to a managed Postgres from a pod normally means
either exposing the database on a public IP (bad) or setting up VPC peering / a
Serverless VPC Connector (correct, but a separate infra project). And even once
networking works, you still need a DB password somewhere.

**How this demo solves it:** the Cloud SQL Auth Proxy runs as a **second container in
the same pod** (`02-deployment.yaml`). It uses the pod's Workload Identity credential
to authenticate to the Cloud SQL Admin API, opens an encrypted tunnel to the instance,
and exposes it on `127.0.0.1:5432` inside the pod. Combined with `--auto-iam-authn` and
a Postgres user created via `gcloud sql users create ... --type=cloud_iam_service_account`
(README step 4), the app connects with **no password at all** — the proxy handles
authentication using the same Workload Identity credential end to end.

```yaml
- name: cloud-sql-proxy
  image: gcr.io/cloud-sql-connectors/cloud-sql-proxy:2.14.0
  args: ["--structured-logs", "--auto-iam-authn", "PROJECT_ID:REGION:gke-backend-demo-db"]
```

Compare with `secrets-tls-demo-app`: there, the DB password is a real value that lives
in Vault and gets synced into a K8s `Secret` by External Secrets Operator — a good
pattern, but it's still a password that exists and must be rotated. Here, IAM DB auth
means there's no password to leak in the first place. The tradeoff: this only works
because the database is Cloud SQL specifically — it's not a portable pattern for a
self-hosted Postgres.

## 3. `BackendConfig` — tuning the actual GCP load balancer

**The problem it solves:** a plain Kubernetes `Service`/`Ingress` has no vocabulary for
GCP-specific load balancer features — custom health checks beyond a basic port check,
Cloud CDN, Cloud Armor, IAP. There's nothing in the core Kubernetes API for any of that,
because it's not portable to other clouds.

**How GKE solves it:** `BackendConfig` is a GKE-only CRD (`cloud.google.com/v1`) that
configures the GCE backend service GKE creates behind the scenes. A `Service`
annotation (`cloud.google.com/backend-config`) points at it by name:

```yaml
# Service
metadata:
  annotations:
    cloud.google.com/backend-config: '{"default": "gke-backend-demo-backendconfig"}'
```

`04-backendconfig.yaml` sets health-check tuning and connection draining; the commented
`cdn`/`iap` blocks show where you'd turn on caching or identity-aware proxying without
touching the app or the Deployment at all.

## 4. `FrontendConfig` — HTTPS redirect at the load balancer

**The problem it solves:** redirecting `http://` to `https://` is normally an
application- or ingress-controller-level concern (an nginx-ingress annotation, a Spring
Security filter). On GKE, the load balancer terminates TLS before traffic ever reaches
the cluster, so the redirect has to happen at that layer too.

**How GKE solves it:** `FrontendConfig` (`networking.gke.io/v1beta1`), referenced from
the `Ingress` via `networking.gke.io/v1beta1.FrontendConfig`:

```yaml
spec:
  redirectToHttps:
    enabled: true
```

## 5. `ManagedCertificate` — Google-issued, Google-renewed TLS, no cert-manager

**The problem it solves:** getting a trusted TLS certificate onto an Ingress normally
means running cert-manager plus an ACME `ClusterIssuer` (the pattern in
[secrets-tls-demo-app/production-hardening.md](../secrets-tls-demo-app/production-hardening.md#1-real-certificates-instead-of-self-signed)) —
correct and portable, but it's a controller you have to install, watch, and keep
healthy.

**How GKE solves it (as an alternative, not a replacement):** `ManagedCertificate`
(`networking.gke.io/v1`) tells GKE itself to provision and silently auto-renew a
publicly trusted cert for the listed domains, referenced from the `Ingress` via
`networking.gke.io/managed-certificates`:

```yaml
spec:
  domains:
    - api.example.com
```

No extra controller, no `Certificate`/`Issuer` objects, no Secret holding a private
key to manage. The tradeoffs: it only works with GKE's own Ingress (not usable if you
run nginx-ingress instead), first issuance takes up to ~60 minutes and needs DNS
already pointed at the load balancer's IP first, and you get zero control over the CA
or validation method (always HTTP-01-equivalent, domain-only, no wildcards). Real
platforms pick cert-manager when they need wildcard certs, DNS-01, or a non-GKE
ingress controller; they pick `ManagedCertificate` when "just works, zero moving
parts" outweighs that flexibility.

## 6. GKE Autopilot vs. Standard — what actually changes in these manifests

Everything above works identically on both modes. What's mode-specific:

| | GKE Standard | GKE Autopilot |
|---|---|---|
| Node management | You choose machine types, node pools, manage upgrades | Fully managed — no nodes to see or size |
| `resources.requests`/`limits` | Recommended | **Required** on every container, or the pod is rejected at admission |
| Workload Identity | Opt-in (`--workload-pool` flag at cluster creation) | On by default |
| `hostPath` volumes, privileged containers, DaemonSets | Allowed | Blocked or heavily restricted |
| Billing | Per node, whether or not it's fully utilized | Per pod resource request |

`02-deployment.yaml` sets `requests`/`limits` on both containers specifically so this
chart runs unmodified on Autopilot — on Standard those same values just mean sane
bin-packing.

## 7. What's deliberately *not* GKE-specific here

The `Deployment`'s liveness/readiness probes, the `HPA`, and the app's Spring Boot
config are plain portable Kubernetes — no reason to make them GKE-only. The
[values-to-templates walkthrough](../../../../helm/values-to-templates-walkthrough.md) and
its dev/stg/prod gotchas (null probes, `replicaCount` vs. `autoscaling.enabled`) apply
here exactly as written; this doc only covers what's different *because* the target is
GKE.
