# GKE backend demo: Workload Identity + Cloud SQL Auth Proxy + GKE Ingress

A Spring Boot backend and a set of **plain Kubernetes manifests** (no Helm, no live
cluster) showing what actually changes when the target is GKE instead of a generic
cluster (minikube, EKS, on-prem). See
[gke-specific-features.md](./gke-specific-features.md) for what's unique to GKE and why
each piece exists; this file is the run-order walkthrough.

New to the Google Cloud Console specifically (not just new to this repo)? The
click-by-click version of every step below — including where each button actually is,
why each step exists, and what people commonly forget to clean up — is the companion
interactive walkthrough: see the link the assistant gave you alongside this repo, or
ask for it again if you've lost it. This file stays the terse command reference.

**This is documentation, not a script to run blind.** A real GKE cluster, Artifact
Registry repo, and Cloud SQL instance all cost money and need a GCP project with
billing enabled. Every command below is real and correct — read it, understand it,
adapt the placeholders (`PROJECT_ID`, `REGION`, domain names) — rather than pasting
blindly into a shell against infrastructure you didn't mean to create.

## 0. Prerequisites

```bash
gcloud auth login
gcloud config set project PROJECT_ID
gcloud services enable container.googleapis.com sqladmin.googleapis.com \
  artifactregistry.googleapis.com
```

## 1. Create the cluster

Autopilot is the simpler default for this walkthrough — it manages nodes for you and
enables Workload Identity out of the box:

```bash
gcloud container clusters create-auto gke-backend-demo \
  --region REGION
```

On GKE **Standard** instead, you'd add `--workload-pool=PROJECT_ID.svc.id.goog` to an
otherwise normal `gcloud container clusters create` — Workload Identity isn't on by
default there. Everything after this step is identical for both.

```bash
gcloud container clusters get-credentials gke-backend-demo --region REGION
```

## 2. Create an Artifact Registry repo and push the image

GKE doesn't require Artifact Registry specifically — any registry the cluster's nodes
can pull from works — but it's the GCP-native choice and integrates with Workload
Identity for pull auth instead of an `imagePullSecret`.

```bash
gcloud artifacts repositories create gke-backend-demo \
  --repository-format=docker --location=REGION

gcloud auth configure-docker REGION-docker.pkg.dev

cd app
docker build -t REGION-docker.pkg.dev/PROJECT_ID/gke-backend-demo/gke-backend-demo:0.1.0 .
docker push REGION-docker.pkg.dev/PROJECT_ID/gke-backend-demo/gke-backend-demo:0.1.0
```

## 3. Create the Cloud SQL instance and database

```bash
gcloud sql instances create gke-backend-demo-db \
  --database-version=POSTGRES_15 \
  --region=REGION \
  --tier=db-f1-micro \
  --database-flags=cloudsql.iam_authentication=on

gcloud sql databases create gkedemo --instance=gke-backend-demo-db
```

## 4. Create the GSA, grant it Cloud SQL access, and add it as an IAM DB user

```bash
gcloud iam service-accounts create gke-backend-demo \
  --display-name "gke-backend-demo workload identity"

gcloud projects add-iam-policy-binding PROJECT_ID \
  --member "serviceAccount:gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com" \
  --role roles/cloudsql.client

# Adds the GSA itself as a Postgres user authenticated purely via IAM -- no password
# to create, store, or rotate.
gcloud sql users create gke-backend-demo@PROJECT_ID.iam \
  --instance=gke-backend-demo-db \
  --type=cloud_iam_service_account
```

## 5. Apply the namespace and ServiceAccount, then bind Workload Identity

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-serviceaccount.yaml

gcloud iam service-accounts add-iam-policy-binding \
  gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:PROJECT_ID.svc.id.goog[gke-demo-ns/gke-backend-demo]"
```

This last binding is the actual Workload Identity link: it says "pods running as the
`gke-backend-demo` KSA in the `gke-demo-ns` namespace of this specific GKE cluster may
act as this GSA." Nothing else in the cluster can impersonate it.

## 6. Enable the Secret Manager add-on and create the app's secret

This is the piece that has no IAM-auth shortcut (see
[gke-specific-features.md #6](./gke-specific-features.md#6-secret-manager-csi-driver--gkes-own-way-to-get-a-secret-into-an-env-var)) —
a stand-in for any real third-party API key or webhook secret the app would need:

```bash
gcloud container clusters update gke-backend-demo --region REGION \
  --enable-secret-manager

printf 'replace-with-a-real-value' | gcloud secrets create gke-backend-demo-external-api-key \
  --data-file=-

gcloud secrets add-iam-policy-binding gke-backend-demo-external-api-key \
  --member "serviceAccount:gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com" \
  --role roles/secretmanager.secretAccessor
```

## 7. Reserve a static IP for the load balancer (needed by the ManagedCertificate)

```bash
gcloud compute addresses create gke-backend-demo-ip --global
gcloud compute addresses describe gke-backend-demo-ip --global --format="value(address)"
```

Point `api.example.com`'s DNS A record at that IP before moving on — `ManagedCertificate`
won't issue until the domain actually resolves to the load balancer.

## 8. Edit the placeholders, then apply everything else

Replace `PROJECT_ID`, `REGION`, and `api.example.com` in `k8s/01-serviceaccount.yaml`,
`k8s/02-deployment.yaml`, `k8s/06-managedcertificate.yaml`, and
`k8s/09-secretproviderclass.yaml` with real values, then:

```bash
kubectl apply -f k8s/04-backendconfig.yaml
kubectl apply -f k8s/05-frontendconfig.yaml
kubectl apply -f k8s/06-managedcertificate.yaml
kubectl apply -f k8s/09-secretproviderclass.yaml
kubectl apply -f k8s/03-service.yaml
kubectl apply -f k8s/02-deployment.yaml
kubectl apply -f k8s/08-hpa.yaml
kubectl apply -f k8s/07-ingress.yaml
```

Order matters loosely here: `BackendConfig`/`FrontendConfig`/`ManagedCertificate`/
`SecretProviderClass` exist before the `Service`/`Deployment`/`Ingress` that reference
them by name, so GKE doesn't have to retry-and-reconcile a dangling reference (it would
eventually anyway, but this avoids the noise).

## 9. Verify each piece

```bash
kubectl get pods -n gke-demo-ns -w
# both containers (app, cloud-sql-proxy) should reach 2/2 Running

kubectl logs -n gke-demo-ns deploy/gke-backend-demo -c cloud-sql-proxy
# look for "Ready for new connections" -- confirms the proxy authenticated via
# Workload Identity and opened the tunnel to Cloud SQL

kubectl get secret gke-backend-demo-api-key-secret -n gke-demo-ns
# should exist once the Secret Manager CSI driver has synced it -- created by
# secretObjects in 09-secretproviderclass.yaml, not by anything you applied directly

kubectl get managedcertificate -n gke-demo-ns -w
# Status.CertificateStatus moves Provisioning -> Active once DNS resolves and Google
# finishes issuance (can take up to ~60 minutes)

kubectl get ingress -n gke-demo-ns
# ADDRESS column should match the static IP reserved in step 7
```

## 10. Hit the API

```bash
curl https://api.example.com/actuator/health
curl -X POST https://api.example.com/api/v1/items \
  -H "Content-Type: application/json" -d '{"name":"widget","quantity":5}'
curl https://api.example.com/api/v1/items
```

## 11. Tear down (to stop being billed)

```bash
kubectl delete -f k8s/07-ingress.yaml -f k8s/08-hpa.yaml -f k8s/02-deployment.yaml \
  -f k8s/03-service.yaml -f k8s/09-secretproviderclass.yaml \
  -f k8s/06-managedcertificate.yaml -f k8s/05-frontendconfig.yaml -f k8s/04-backendconfig.yaml
kubectl delete namespace gke-demo-ns
gcloud secrets delete gke-backend-demo-external-api-key
gcloud compute addresses delete gke-backend-demo-ip --global
gcloud sql instances delete gke-backend-demo-db
gcloud container clusters delete gke-backend-demo --region REGION
gcloud artifacts repositories delete gke-backend-demo --location=REGION
```

## Reviewing before you apply

Nothing here was pushed to a live cluster as part of writing this repo. What *was*
verified locally, without touching GCP:

- `app/` compiles clean: `cd app && mvn -q compile`.
- Every file under `k8s/` is valid YAML with a `kind`/`apiVersion` pair (checked with
  a `yaml.safe_load_all` pass over each file).

What a real review pass adds once you do have a cluster to point at:

```bash
kubectl apply --dry-run=server -f k8s/02-deployment.yaml -n gke-demo-ns
```

`--dry-run=server` validates against the live API server (including the GKE-only CRDs
above, once cert-manager/BackendConfig/etc. CRDs are registered) without persisting
anything — the closest thing to a real review step that still touches zero pods.
