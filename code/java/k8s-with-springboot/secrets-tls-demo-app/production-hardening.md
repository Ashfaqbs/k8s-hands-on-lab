# Production Hardening — What Changes Beyond This Local Setup

The [README](./README.md) gets this chart running on minikube: self-signed TLS, a Vault
dev-mode root token, no autoscaling, no network restrictions. None of that is how a real
environment runs it. This doc lists what actually changes, with real manifests — the kind of
thing you don't normally bother wiring up locally, but that a production values file/overlay
needs.

## 1. Real certificates instead of self-signed

Locally, `clusterissuer.yaml` creates a `selfSigned` `ClusterIssuer` — fine for learning the
cert-manager → `Certificate` → `Secret` → `Ingress` wiring, useless for a real browser (it'll
show untrusted-cert warnings). In production you point at a real ACME issuer instead —
Let's Encrypt is the common free option:

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: platform-team@example.com
    privateKeySecretRef:
      name: letsencrypt-prod-account-key
    solvers:
      - http01:
          ingress:
            ingressClassName: nginx
      # or, for wildcard certs / no public HTTP endpoint:
      # - dns01:
      #     cloudflare:
      #       apiTokenSecretRef: { name: cloudflare-api-token, key: api-token }
```

The `Certificate` resource and the `Ingress`'s `spec.tls[].secretName` reference don't
change at all — only `issuerRef.name` needs to point at `letsencrypt-prod` instead of the
local self-signed issuer. This is the entire point of the cert-manager abstraction: the
issuer backend is swappable without touching how the app consumes its TLS secret.

## 2. NetworkPolicy — restrict who can talk to what

Nothing stops any pod in the cluster from reaching Postgres or the app locally. In
production, default-deny plus explicit allows (see the full writeup in
[../../../../concepts/network-policy/Readme.md](../../../../concepts/network-policy/Readme.md)):

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: demo-ns
spec:
  podSelector: {}
  policyTypes: [Ingress, Egress]
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-app-to-postgres
  namespace: demo-ns
spec:
  podSelector:
    matchLabels: { app.kubernetes.io/component: postgres }
  policyTypes: [Ingress]
  ingress:
    - from:
        - podSelector: { matchLabels: { app.kubernetes.io/component: app } }
      ports: [{ port: 5432, protocol: TCP }]
---
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-ingress-to-app
  namespace: demo-ns
spec:
  podSelector:
    matchLabels: { app.kubernetes.io/component: app }
  policyTypes: [Ingress]
  ingress:
    - from:
        - namespaceSelector: { matchLabels: { kubernetes.io/metadata.name: ingress-nginx } }
      ports: [{ port: 8080, protocol: TCP }]
```

Without this, any compromised pod anywhere in the cluster could reach `demo-app-postgres`
directly.

## 3. PodDisruptionBudget — survive voluntary disruptions

Locally you never drain a node or run a cluster upgrade mid-demo. In production, a
`PodDisruptionBudget` stops a rolling node drain or cluster upgrade from taking down every
replica at once:

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: demo-app-pdb
  namespace: demo-ns
spec:
  minAvailable: 2   # or maxUnavailable: 1 — pick one style
  selector:
    matchLabels: { app.kubernetes.io/component: app }
```

## 4. Spread replicas across nodes/zones

With `replicaCount: 1` locally there's nothing to spread. In production, pin replicas apart
so a single node failure doesn't take out every pod:

```yaml
# under the Deployment's pod template spec:
topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: kubernetes.io/hostname
    whenUnsatisfiable: DoNotSchedule
    labelSelector:
      matchLabels: { app.kubernetes.io/component: app }
```

## 5. Resource requests/limits and namespace-wide guardrails

The demo chart ships with no `resources` set. In production, always set both requests and
limits on every container (see
[../../../../concepts/resource-quota/Readme.md](../../../../concepts/resource-quota/Readme.md) for
the namespace-wide version of this):

```yaml
resources:
  requests: { cpu: 250m, memory: 256Mi }
  limits: { cpu: 500m, memory: 512Mi }
```

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: demo-ns-quota
  namespace: demo-ns
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 4Gi
    limits.cpu: "8"
    limits.memory: 8Gi
    pods: "20"
```

## 6. Secrets: swap dev-mode Vault for the real thing

The README's Vault install uses `server.dev.enabled=true` with a static `root` token — that
mode auto-unseals and is explicitly unsafe outside of learning. In production:

- Run Vault in HA mode with a real storage backend (Raft integrated storage or an external
  one), unsealed via a real unseal process (auto-unseal via a cloud KMS is the common choice).
- Swap `vault-secretstore.yaml`'s `auth.tokenSecretRef` (a static token) for
  `auth.kubernetes` — the `ClusterSecretStore` authenticates using the pod's own
  ServiceAccount token instead of a shared static secret:

```yaml
spec:
  provider:
    vault:
      server: https://vault.internal:8200
      path: secret
      version: v2
      auth:
        kubernetes:
          mountPath: kubernetes
          role: demo-app
          serviceAccountRef:
            name: demo-app
```

See [../../../../concepts/secrets-management/Readme.md](../../../../concepts/secrets-management/Readme.md)
for the full comparison between this ESO approach and the Vault Agent sidecar injector
alternative.

## 7. Image tags: never `latest`

`values.yaml` defaults to `image.tag: "0.1.0"`, which is at least pinned — but watch for
`latest` creeping into any environment override. Production should always deploy an
immutable, specific tag (or digest) so a rollback via `helm rollback` actually rolls back the
image, not just the values.
