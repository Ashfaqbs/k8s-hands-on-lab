# Console Walkthrough: Deploying `gke-backend-demo` Step by Step

[README.md](./README.md) is the terse command reference. This file is for someone who
hasn't used the Google Cloud Console before and wants to know exactly where to click,
not just what to type — plus *why* each step exists, and what tends to get forgotten.
Console labels shift slightly over time; the structure and order below won't.

**This is still documentation, not a script.** Creating a real GKE cluster, Cloud SQL
instance, and Artifact Registry repo costs real money on a real GCP project with
billing enabled. Read each step before running it.

## 0. Four ways an env var/secret reaches your pod

Read this before touching the console — it's the actual decision being made at steps 6
and 8 below.

| | Plain Secret | IAM database auth | Secret Manager CSI | ESO + Vault |
|---|---|---|---|---|
| Extra infra to run | None | None | None (managed add-on) | ESO controller + Vault |
| A password/key exists at all | Yes | **No** | Yes (in Secret Manager) | Yes (in Vault) |
| Works outside GKE | Yes | N/A | No | Yes |
| Used in this app for | — | `DB_USERNAME` | `EXTERNAL_API_KEY` | — |

- **Plain env var / Secret** — the value sits directly in the Deployment YAML, or in a
  K8s `Secret` someone applied by hand. Zero extra infrastructure, fine for
  non-sensitive config.
- **IAM database auth** — the Cloud SQL Auth Proxy sidecar authenticates using the
  pod's own identity. No password is created, stored, or rotated, because none exists.
- **Secret Manager CSI** — a managed GKE cluster add-on mounts a Secret Manager value
  straight into the pod and syncs it into a normal `Secret`. GKE-only, but no separate
  controller to run.
- **External Secrets Operator + Vault** — portable across GKE, EKS, or bare metal.
  Covered in the sibling [secrets-tls-demo-app](../secrets-tls-demo-app) project, not
  repeated here.

> **Why this matters first:** every step from here on is either creating one of these
> four things, or wiring a pod up to read from one. The manifests make a lot more sense
> once you know which of the four each one is doing.

## 1. Project and billing

1. Go to `console.cloud.google.com` and sign in.
2. Top-left, next to the "Google Cloud" logo: click the **project picker** dropdown →
   **New Project**.
3. Name it (e.g. `gke-backend-demo`), leave the organization as-is if you don't have
   one, click **Create**.
4. Use the project picker again to **switch into it** — the console silently stays on
   your previous project otherwise.
5. Left hamburger menu (☰) → **Billing** → if this project shows "no billing account
   linked," click **Link a billing account** and attach one. GKE Autopilot cluster
   creation fails immediately without this.

> **Why a new project:** a dedicated project means "delete the project" is a valid
> teardown strategy later — one action that guarantees nothing keeps billing. Reusing
> an existing project works too, but then step 11's cleanup has to happen
> resource-by-resource.

## 2. Enable the APIs you'll need

1. Use the **search bar at the very top of the console** (not the left menu) — type
   **Kubernetes Engine API**, click the result, click **Enable**.
2. Repeat for: **Cloud SQL Admin API**, **Artifact Registry API**, **Secret Manager
   API**, **Compute Engine API** (this last one usually enables itself with GKE, but
   check).

Or, from Cloud Shell (see step 4 for what that is) — one line instead of four clicks:

```bash
gcloud services enable container.googleapis.com sqladmin.googleapis.com \
  artifactregistry.googleapis.com secretmanager.googleapis.com
```

> **Why enable these up front:** creating the cluster or the SQL instance before its
> API is enabled produces an "API not enabled" error mid-wizard instead of an early
> warning. Enabling all four now means every later step just works.

## 3. Create the GKE cluster

Takes 5–10 minutes once you click Create — this is the slow step, start it and read
ahead.

1. Left hamburger menu (☰) → scroll to **Kubernetes Engine** → **Clusters**.
2. Click **Create** (top of the page).
3. You'll be offered **Autopilot** or **Standard** — keep **Autopilot** selected (it's
   the default) and click **Configure**.
4. Name: `gke-backend-demo`. Region: pick one close to you, e.g. `us-central1`.
5. Leave every other setting at its default and click **Create** at the bottom.

> **Why Autopilot over Standard:** Standard makes you size and manage the underlying
> VMs (node pools) yourself — a whole extra layer to learn before you've deployed
> anything. Autopilot bills per pod resource request instead of per node, and enables
> Workload Identity (step 7) by default. The one thing it demands in return: every
> container needs `resources.requests` set, which `k8s/02-deployment.yaml` already
> does.

## 4. Connect to the cluster

1. Once the cluster shows a green checkmark in the Clusters list, click its name.
2. Click **Connect** (top of the cluster detail page) — it shows a ready-made
   `gcloud container clusters get-credentials` command.
3. Click the terminal icon `>_` in the console's top-right bar to open **Cloud
   Shell** — a real terminal running in your browser, no local install needed — and
   paste that command in.

> **Why Cloud Shell instead of installing `gcloud` locally:** everything from here on
> is `kubectl`/`gcloud` commands. Cloud Shell already has both installed and
> pre-authenticated as you — it's still "inside the console," just the console's
> terminal panel rather than a form. Clicking through raw YAML application in the
> Workloads UI is possible for simple cases but breaks down fast for anything with
> CRDs like this app has (see step 8).

## 5. Push the image to Artifact Registry

**In the console (create the repo):**

1. Left menu → **Artifact Registry** → **Repositories** → **Create Repository**.
2. Name: `gke-backend-demo`. Format: **Docker**. Region: the same region from step 3.
3. Click **Create**.

**From Cloud Shell (build and push):**

```bash
gcloud auth configure-docker REGION-docker.pkg.dev

cd code/java/k8s-with-springboot/gke-backend-demo/app
docker build -t REGION-docker.pkg.dev/PROJECT_ID/gke-backend-demo/gke-backend-demo:0.1.0 .
docker push REGION-docker.pkg.dev/PROJECT_ID/gke-backend-demo/gke-backend-demo:0.1.0
```

> **Why Artifact Registry specifically:** it's the piece that talks to Workload
> Identity for pull authentication — the cluster's own identity is enough to pull the
> image, no `imagePullSecret` to create or rotate. Any registry the nodes can reach
> would technically work; this one needs the least extra plumbing on GKE.

## 6. Create the Cloud SQL database

1. Left menu → **SQL** → **Create Instance** → choose **PostgreSQL**.
2. Instance ID: `gke-backend-demo-db`. Set a password for the default `postgres` user
   (the app itself won't use it — see below). Region: same as your cluster.
3. Expand **Customize your instance** → under **Flags**, add flag
   `cloudsql.iam_authentication` → set to **On**. Easy to miss, required for step 7.
4. Click **Create** (several minutes).
5. Once ready: open the instance → **Databases** tab → **Create Database** → name it
   `gkedemo`.

```bash
# equivalent from Cloud Shell
gcloud sql instances create gke-backend-demo-db \
  --database-version=POSTGRES_15 --region=REGION --tier=db-f1-micro \
  --database-flags=cloudsql.iam_authentication=on
gcloud sql databases create gkedemo --instance=gke-backend-demo-db
```

> **Why a password field exists if the app doesn't use one:** Cloud SQL always has a
> default superuser for admin access; the app's own connection goes through IAM auth
> instead, so that password is something you set once and never touch again.

## 7. Service account and Workload Identity

The identity binding at the end of this step is the one place that's genuinely easier
from Cloud Shell than the console — explained below.

**In the console (create the GSA and grant roles):**

1. Left menu → **IAM & Admin** → **Service Accounts** → **Create Service Account**.
2. Name: `gke-backend-demo` → **Create and Continue**.
3. Grant it two roles: **Cloud SQL Client** and **Secret Manager Secret Accessor** →
   **Continue** → **Done**.
4. Back in **SQL** → your instance → **Users** tab → **Add User Account** → switch to
   the **Cloud IAM** tab → enter `gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com`
   as the principal → **Add**. This makes the GSA itself a Postgres user with no
   password.

**From Cloud Shell (apply the namespace/ServiceAccount, then bind Workload Identity):**

```bash
kubectl apply -f k8s/00-namespace.yaml -f k8s/01-serviceaccount.yaml

# the actual identity link -- pods running as this KSA may act as this GSA:
gcloud iam service-accounts add-iam-policy-binding \
  gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:PROJECT_ID.svc.id.goog[gke-demo-ns/gke-backend-demo]"
```

> **Why this one isn't a clean console click:** the console *can* do this — on the
> service account's Permissions tab, "Grant Access" with a principal string like
> `principal://iam.googleapis.com/projects/.../workloadIdentityPools/PROJECT_ID.svc.id.goog/subject/ns/gke-demo-ns/sa/gke-backend-demo`
> — but that string is long and easy to mistype. The single command above does the
> same thing with far less room for error. This is the honest exception to
> "everything is clickable."

## 8. Put a secret in Secret Manager

Stands in for any real third-party API key — the one env var with no IAM-auth
shortcut.

1. Left menu → **Security** → **Secret Manager** → **Create Secret**.
2. Name: `gke-backend-demo-external-api-key`. Secret value: any placeholder string.
3. Click **Create Secret**.
4. Open the secret → **Permissions** tab → **Grant Access** → principal:
   `gke-backend-demo@PROJECT_ID.iam.gserviceaccount.com` → role: **Secret Manager
   Secret Accessor** → **Save**.

```bash
# enable the cluster add-on, from Cloud Shell
gcloud container clusters update gke-backend-demo --region REGION \
  --enable-secret-manager
```

> **Why this is GKE's own way, not a generic Kubernetes pattern:** on any other
> cluster, getting a Secret-Manager-equivalent value into a pod means installing and
> managing the open-source Secrets Store CSI Driver yourself. On GKE,
> `--enable-secret-manager` turns it into a managed feature of the cluster — same tier
> as Workload Identity. See [`k8s/09-secretproviderclass.yaml`](./k8s/09-secretproviderclass.yaml)
> for the manifest this powers, and
> [gke-specific-features.md](./gke-specific-features.md#6-secret-manager-csi-driver--gkes-own-way-to-get-a-secret-into-an-env-var)
> for the full comparison.

## 9. Reserve a static IP

1. Left menu → **VPC network** → **IP addresses** → **Reserve External Static
   Address**.
2. Name: `gke-backend-demo-ip`. Network Service Tier: **Premium**. Type: **Global**
   (required for GKE's HTTP(S) load balancer).
3. Click **Reserve**, then copy the IP address shown in the list.
4. At your domain registrar (outside GCP), point an **A record** for your chosen
   hostname at that IP.

> **Why this has to happen before the Ingress works:** `ManagedCertificate` only
> issues a certificate once Google can confirm the domain's DNS already resolves to
> this exact IP. Reserving the IP and pointing DNS at it now means issuance isn't
> blocked later.

## 10. Apply the manifests

This is the step that's genuinely faster in Cloud Shell than the console — five custom
resource types, no wizard for any of them.

```bash
# edit PROJECT_ID / REGION / your domain into 01-serviceaccount.yaml, 02-deployment.yaml,
# 06-managedcertificate.yaml, 09-secretproviderclass.yaml, then:
kubectl apply -f k8s/04-backendconfig.yaml -f k8s/05-frontendconfig.yaml \
  -f k8s/06-managedcertificate.yaml -f k8s/09-secretproviderclass.yaml
kubectl apply -f k8s/03-service.yaml -f k8s/02-deployment.yaml \
  -f k8s/08-hpa.yaml -f k8s/07-ingress.yaml
```

Then check it visually:

1. Kubernetes Engine → **Workloads** shows `gke-backend-demo` spinning up, pod status
   included.
2. Kubernetes Engine → **Gateways, Services & Ingress** shows the Ingress and, once
   ready, its external IP.

> **Why not the Workloads UI's "Deploy" button:** that wizard is built for a plain
> Deployment + Service. This app's manifests lean on five things it has no form for —
> `BackendConfig`, `FrontendConfig`, `ManagedCertificate`, `SecretProviderClass`, and
> an HPA all referencing each other by name. Raw `kubectl apply` is the honest tool
> for this, not a limitation of the console.

## 11. Verify end-to-end

```bash
kubectl get pods -n gke-demo-ns -w
# both containers per pod (app, cloud-sql-proxy) should reach 2/2 Running

kubectl logs -n gke-demo-ns deploy/gke-backend-demo -c cloud-sql-proxy
# look for "Ready for new connections"

kubectl get secret gke-backend-demo-api-key-secret -n gke-demo-ns
# should exist -- created by the CSI sync, not by anything you applied directly

kubectl get managedcertificate -n gke-demo-ns -w
# Provisioning -> Active, can take up to ~60 minutes
```

> **Why the certificate is the slow part:** everything else above resolves in under a
> minute. `ManagedCertificate` issuance is the one step where "nothing's wrong, it's
> just Google's CA doing its thing" is the correct read on a still-`Provisioning`
> status.

## 12. Hit the API

```bash
curl https://YOUR_DOMAIN/actuator/health
curl -X POST https://YOUR_DOMAIN/api/v1/items \
  -H "Content-Type: application/json" -d '{"name":"widget","quantity":5}'
curl https://YOUR_DOMAIN/api/v1/items
```

A green `UP` on `/actuator/health` means the app, the Cloud SQL tunnel, and the load
balancer are all correctly wired — it's the one endpoint that fails if any of the
previous eleven steps didn't quite land.

## 13. Tear it down

Do this the same day you finish — see the gaps section below for why.

```bash
kubectl delete namespace gke-demo-ns
gcloud secrets delete gke-backend-demo-external-api-key
gcloud compute addresses delete gke-backend-demo-ip --global
gcloud sql instances delete gke-backend-demo-db
gcloud container clusters delete gke-backend-demo --region REGION
gcloud artifacts repositories delete gke-backend-demo --location=REGION
```

Or, the one-click version: if this was a dedicated project (step 1), go to **IAM &
Admin** → **Settings** → **Shut down**. Deletes everything in it, cluster and SQL
instance included, no per-resource hunting.

## What people usually miss

- [ ] **No budget alert set.** Billing → Budgets & Alerts → Create Budget, even a $5
  one. It emails you before a forgotten Cloud SQL instance turns into a real bill —
  Cloud SQL and GKE Autopilot both bill continuously whether or not anything's using
  them.
- [ ] **Over-broad IAM roles.** The GSA here only ever needs Cloud SQL Client +
  Secret Manager Secret Accessor. Reaching for Editor or Owner "to make it work" is
  the most common shortcut that turns into a real incident later.
- [ ] **Public cluster control plane.** This walkthrough uses GKE's default public
  endpoint for simplicity. A production cluster restricts control-plane access to
  specific IP ranges (a private cluster, or authorized networks) — this demo skips
  that.
- [ ] **Quota surprises.** New projects have default quotas — global static IPs,
  in-use IP addresses, CPUs per region. If cluster or IP creation fails with a quota
  error, IAM & Admin → Quotas is where to check and request an increase.
- [ ] **Logging costs scale with volume.** Cloud Logging/Monitoring is on by default
  for GKE and has a real free tier, but a chatty app writing verbose logs at scale
  turns into a line item you didn't expect.
- [ ] **No `NetworkPolicy`.** Nothing here stops another pod in the same cluster from
  reaching Postgres directly. See `production-hardening.md` in the sibling
  [secrets-tls-demo-app](../secrets-tls-demo-app/production-hardening.md) project for
  the default-deny pattern — it applies here unchanged.
