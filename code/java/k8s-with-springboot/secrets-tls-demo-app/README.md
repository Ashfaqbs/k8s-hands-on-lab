# Local minikube demo: secrets + TLS (Spring Boot + JPA + Postgres)

A Spring Boot + Postgres app deployed via Helm to minikube, wired up to demonstrate two
things most tutorials skip: **secret injection from Vault via External Secrets Operator**,
and **TLS termination via cert-manager**. See
[../../../../concepts/secrets-management/Readme.md](../../../../concepts/secrets-management/Readme.md)
for how this fits alongside the other secret-handling patterns (plain ConfigMap/Secret, Vault
Agent sidecar injection), and
[production-hardening.md](./production-hardening.md) for what changes when this
stops being a local-only chart.

## Optional: GitOps via ArgoCD, driven from a local folder

`argocd-application.yaml` shows the ArgoCD `Application` CRD pointed at a `file://` source —
useful if you want to practice the GitOps pull-based flow without pushing to GitHub first.
Point it at any locally mounted path (e.g. via `minikube mount <this-folder>:/mnt/host-repo`
in its own terminal window, kept running) and set `spec.source.repoURL` to that mount path.
Any commit to `main` is picked up automatically (`syncPolicy.automated` with `selfHeal:
true`) as long as the mount is alive when ArgoCD reads it. If you'd rather skip ArgoCD
entirely, just run the `helm install` in step 10 directly — everything else in this README
works the same either way.

## 1. Install Helm (if not already present)
```powershell
winget install Helm.Helm
```

## 2. Start minikube
```powershell
minikube start
minikube status
```

## 3. Build the image directly into minikube's Docker daemon
```powershell
# Point your shell's docker client at minikube's daemon
minikube docker-env | Invoke-Expression

cd app
docker build -t demo-app:0.1.0 .

# Confirm it landed inside minikube
minikube image ls | Select-String demo-app
```
No push/registry needed — `image.pullPolicy: IfNotPresent` plus the tag already present in minikube's Docker daemon means the kubelet won't try to pull.

## 4. Create the namespace
```powershell
kubectl create namespace demo-ns
```

## 5. Install cert-manager (for the Certificate/ClusterIssuer templates)
```powershell
helm repo add jetstack https://charts.jetstack.io
helm repo update
helm install cert-manager jetstack/cert-manager `
  -n cert-manager --create-namespace `
  --set crds.enabled=true
kubectl get pods -n cert-manager -w   # wait for all 3 pods Running
```

## 6. Install External Secrets Operator (ESO)
```powershell
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
helm install external-secrets external-secrets/external-secrets `
  -n external-secrets-system --create-namespace
kubectl get pods -n external-secrets-system -w   # wait for Running
```

## 7. Run Vault in dev mode
Dev mode auto-unseals and gives you a fixed root token (`root`) — never do this in a real environment, but it's the fastest way to learn the ESO <-> Vault wiring.
```powershell
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update
helm install vault hashicorp/vault `
  -n vault-ns --create-namespace `
  --set "server.dev.enabled=true" `
  --set "server.dev.devRootToken=root"
kubectl get pods -n vault-ns -w   # wait for vault-0 Running
```
This matches `values.yaml`'s `vault.address: http://vault.vault-ns.svc.cluster.local:8200`. If you install into a different namespace, update that value to match.

## 8. Write the DB creds into Vault
The keys must match what `application.yml` expects as env vars (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) — `dataFrom.extract` in `externalsecret.yaml` copies every key at the path as-is into the K8s Secret.
```powershell
kubectl exec -n vault-ns vault-0 -- vault kv put secret/demo/demo-app/creds `
  DB_HOST=demo-app-postgres `
  DB_PORT=5432 `
  DB_NAME=demo `
  DB_USERNAME=demo `
  DB_PASSWORD=demo
```

## 9. Enable the minikube ingress addon + hosts entry
```powershell
minikube addons enable ingress
kubectl get pods -n ingress-nginx -w   # wait for controller Running

# Map the ingress host to minikube's IP
minikube ip
# Add to C:\Windows\System32\drivers\etc\hosts (as Administrator):
#   <minikube-ip>  demo-app.local
```

## 10. Install the chart
```powershell
cd helm
helm install demo-app .\demo-app -n demo-ns
```
This creates, in order you can watch: the self-signed `ClusterIssuer` -> `Certificate` (issues `demo-app-tls-secret`) -> `ClusterSecretStore` pointed at Vault -> `ExternalSecret` (pulls from Vault, creates `demo-app-db-creds`) -> Postgres -> the app Deployment/Service/Ingress.

## 11. Verify each piece
```powershell
kubectl get pods -n demo-ns -w

# Certificate issued?
kubectl get certificate -n demo-ns
kubectl describe certificate demo-app-tls-secret -n demo-ns

# ExternalSecret synced from Vault?
kubectl get externalsecret -n demo-ns
kubectl get secret demo-app-db-creds -n demo-ns -o jsonpath="{.data.DB_HOST}" | base64 -d
```
`SecretSynced=True` on the ExternalSecret and a populated `demo-app-db-creds` Secret confirm the Vault wiring is working end-to-end.

## 12. Hit the API
```powershell
# Via ingress (TLS is self-signed, so -k skips cert validation)
curl -k https://demo-app.local/actuator/health

# Or just port-forward, bypassing ingress entirely
kubectl port-forward -n demo-ns svc/demo-app 8080:8080
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/api/v1/items -H "Content-Type: application/json" -d '{\"name\":\"widget\",\"quantity\":5}'
curl http://localhost:8080/api/v1/items
```

## 13. Iterate
After code changes:
```powershell
minikube docker-env | Invoke-Expression
docker build -t demo-app:0.1.0 .\app
kubectl rollout restart deployment/demo-app -n demo-ns
```

## Notes
- Postgres data persists via a PVC (`demo-app-postgres-pvc`) using minikube's default storage class — fine for local testing, not for anything you care about long-term.
- Toggle `vault.enabled: false` in `values.yaml` to fall back to the plain `Secret` in `secret.yaml` if you want to test without standing up Vault/ESO — see the [secrets-management doc](../../../../concepts/secrets-management/Readme.md) for when you'd pick each approach.
- Toggle `tls.enabled: false` / `ingress.enabled: false` to skip the cert-manager/ingress pieces entirely.
- To tear down the app: `helm uninstall demo-app -n demo-ns && kubectl delete namespace demo-ns`.
- To tear down everything: also `helm uninstall vault -n vault-ns`, `helm uninstall external-secrets -n external-secrets-system`, `helm uninstall cert-manager -n cert-manager`.
- Everything above uses **self-signed TLS and a Vault dev-mode root token** — both are "so you can see the wiring," never how a real environment does it. See [production-hardening.md](./production-hardening.md) for the real-CA / real-auth equivalents.
