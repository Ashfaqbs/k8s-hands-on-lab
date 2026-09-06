# Secrets Management Patterns in Kubernetes

Three approaches you'll actually run into in real environments, from simplest to most
involved. All three ultimately answer the same question — "how does a pod get its DB
password without it being hardcoded in a YAML file committed to git" — but they trade off
differently on visibility, coupling, and blast radius.

## 1. Plain ConfigMap + Secret

The baseline every cluster supports with zero extra tooling: non-sensitive config goes in a
`ConfigMap`, sensitive values go in a `Secret` (base64-encoded, not encrypted, unless you've
enabled encryption-at-rest on the cluster's etcd).

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: myapp-config
data:
  LOG_LEVEL: "INFO"
  FEATURE_FLAG_X: "true"
---
apiVersion: v1
kind: Secret
metadata:
  name: myapp-db-creds
type: Opaque
stringData:            # stringData accepts plaintext; K8s base64-encodes it for you
  DB_USERNAME: "app"
  DB_PASSWORD: "changeme"
```

```yaml
# consumed by a Deployment
envFrom:
  - configMapRef: { name: myapp-config }
  - secretRef: { name: myapp-db-creds }
```

**Where the real secret value lives:** someone (a person, or a CI pipeline) has to `kubectl
apply` or `helm install` it from somewhere — a `.env` file, a CI secret store, a password
manager. Kubernetes itself is just holding it, not managing its lifecycle, rotation, or
source of truth.

- ✅ Zero extra infrastructure, works on any cluster.
- ✅ Simple mental model — `kubectl get secret` shows you exactly what a pod can see.
- ❌ No rotation. Changing a password means someone re-applies the Secret and restarts pods.
- ❌ No audit trail of who read/wrote it beyond normal K8s RBAC on the Secret object.
- ❌ Anyone with `get secrets` RBAC in the namespace can read it back out (`kubectl get
  secret myapp-db-creds -o jsonpath='{.data.DB_PASSWORD}' | base64 -d`).

Good for: local dev, small clusters, anything where you don't yet have a secrets manager and
don't want to block on setting one up.

## 2. External Secrets Operator (ESO) — Vault synced into a K8s Secret

A controller (ESO) runs in-cluster, watches `ExternalSecret` custom resources, and syncs
values from an external secrets manager (Vault, AWS Secrets Manager, Azure Key Vault, GCP
Secret Manager, ...) into a real K8s `Secret` object — which your Deployment then consumes
exactly like pattern 1, unchanged.

```yaml
apiVersion: external-secrets.io/v1
kind: ExternalSecret
metadata:
  name: myapp-external-secret
spec:
  secretStoreRef: { name: vault-backend, kind: ClusterSecretStore }
  refreshInterval: 1h
  target: { name: myapp-db-creds }   # the K8s Secret ESO creates/updates
  dataFrom:
    - extract: { key: secret/myapp/creds }   # path inside Vault
```

A full working example — including the `ClusterSecretStore` wiring to Vault — is in
[secrets-tls-demo-app](../../code/java/k8s-with-springboot/secrets-tls-demo-app) (see its
`helm/demo-app/templates/externalsecret.yaml` and `vault-secretstore.yaml`).

- ✅ Source of truth stays in Vault (rotation, audit logging, access policies all live there,
  where they belong).
- ✅ The app itself needs zero Vault awareness — it just reads env vars / mounted files like
  always, because ESO already turned them into a normal Secret.
- ✅ `refreshInterval` gives you automatic re-sync when the value changes in Vault (a pod
  restart is still needed to pick up new env vars, though — env vars don't hot-reload).
- ❌ The value still lands in a real K8s `Secret` object — anyone who could read it in
  pattern 1 can still read it here. ESO moves *where the value is authored*, not *who can see
  it once it's in the cluster*.
- ❌ One more controller to run and keep healthy; ESO being down means secrets stop
  refreshing (they don't disappear, but they go stale).

Good for: most real production setups. You get centralized secret management without
changing how the application consumes config.

## 3. Vault Agent Sidecar Injector — no K8s Secret object at all

A mutating webhook (the Vault Agent Injector) watches for pods with specific annotations and
injects a Vault Agent **sidecar container** into the pod at creation time. That sidecar
authenticates to Vault itself (usually via the pod's own ServiceAccount token, using Vault's
Kubernetes auth method — no static token stored anywhere), fetches the secret, and writes it
to a shared `emptyDir` volume as a file the main container reads on startup. The secret never
becomes a Kubernetes API object.

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    metadata:
      annotations:
        vault.hashicorp.com/agent-inject: "true"
        vault.hashicorp.com/role: "myapp"
        vault.hashicorp.com/agent-inject-secret-db-creds: "secret/data/myapp/creds"
        # Renders the fetched secret into this path inside the sidecar's shared volume:
        vault.hashicorp.com/agent-inject-template-db-creds: |
          {{- with secret "secret/data/myapp/creds" -}}
          DB_USERNAME={{ .Data.data.username }}
          DB_PASSWORD={{ .Data.data.password }}
          {{- end -}}
    spec:
      serviceAccountName: myapp   # bound to a Vault role via Vault's k8s auth method
      containers:
        - name: myapp
          image: myapp:1.0
          # app reads /vault/secrets/db-creds on startup — a plain file, no K8s Secret involved
```

No `secretStoreRef`, no `ExternalSecret`, no target `Secret` — the injector webhook does all
of the work purely through pod annotations, and the value only ever exists as a file inside
that pod's own filesystem.

- ✅ **Smallest blast radius of the three.** There's no `kubectl get secret` that leaks it —
  the value never touches the K8s API server or etcd at all.
- ✅ Short-lived Vault leases work naturally — the sidecar can renew/re-fetch on its own
  schedule without anyone touching a K8s object.
- ❌ Pod startup is now hard-coupled to Vault being reachable — if Vault is down, new pods
  can't start (ESO's synced Secret, by contrast, keeps serving the last-known-good value even
  if Vault is temporarily unreachable).
- ❌ The app has to read a file at a known path (or the sidecar restarts it) instead of just
  reading env vars — a bigger app-side change than patterns 1 or 2.
- ❌ More moving parts to operate: the injector webhook, Vault's Kubernetes auth method
  configuration, per-app Vault roles/policies.

Good for: environments that specifically want to avoid ever materializing a plaintext secret
as a K8s API object — regulated workloads, or teams that already run Vault everywhere and
want the tightest coupling between "who's asking" (the pod's ServiceAccount) and "what they
get."

## Choosing between them

| | Plain Secret | ESO | Vault Agent Injector |
|---|---|---|---|
| Extra infra required | None | ESO controller + Vault | Vault + injector webhook |
| Secret visible via `kubectl get secret`? | Yes | Yes (the synced copy) | **No** |
| App code changes needed | None | None | Read from a file instead of env vars |
| Behavior if secret backend is down | N/A (no backend) | Serves last-synced value | New pods fail to start |
| Rotation | Manual | Automatic (on `refreshInterval`) | Automatic (Vault lease renewal) |

In practice, plenty of real platforms run **1 and 2 side by side** — ESO for most services,
plain Secrets for things that don't warrant a Vault entry — and reach for **3** only when a
specific compliance or security requirement says a value must never exist as a K8s object.
