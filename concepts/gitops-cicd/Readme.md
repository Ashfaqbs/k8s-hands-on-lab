# **GitOps & CI/CD for Kubernetes**

### **Overview**
There are two broad ways to get changes from a git repo onto a Kubernetes cluster:

1. **Push-based CI/CD**: a pipeline (GitHub Actions, GitLab CI, Jenkins) builds an image, then actively pushes the change into the cluster by running `kubectl apply` / `helm upgrade` from inside the pipeline job. The pipeline needs cluster credentials.
2. **Pull-based GitOps**: a controller running *inside* the cluster (ArgoCD, Flux) continuously watches a git repo and reconciles the cluster's actual state to match what's declared there. The cluster pulls from git; nothing outside the cluster needs cluster credentials.

Both approaches use git as the record of "what should be deployed" — the difference is which side initiates the change.

### **Push-based example: GitHub Actions building and Helm-upgrading**
This repo already has working Helm charts under `../../helm/` and `../../code/practise/helm/`. A minimal pipeline that builds an image and deploys one of them on every push to `main`:

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build and push image
        run: |
          docker build -t ghcr.io/${{ github.repository }}/sb-rest-api:${{ github.sha }} .
          echo "${{ secrets.GHCR_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}/sb-rest-api:${{ github.sha }}

      - name: Configure kubeconfig
        run: |
          mkdir -p ~/.kube
          echo "${{ secrets.KUBECONFIG_BASE64 }}" | base64 -d > ~/.kube/config

      - name: Helm upgrade
        run: |
          helm upgrade --install sb-rest-api ./helm/sb/sb-rest-api \
            --namespace dev \
            --set image.tag=${{ github.sha }}
```

Notice what this pipeline needs: a container registry token *and* a kubeconfig secret, both stored in GitHub. That's the tradeoff of push-based delivery — your CI system holds credentials that can change your production cluster.

### **Pull-based example: the same result via ArgoCD**
With GitOps, the pipeline's job shrinks to "build the image and update a tag in git" — nothing in CI ever touches the cluster directly. An `Application` custom resource (see `../crd-operators`) tells ArgoCD what to watch and where to sync it:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: sb-rest-api
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<you>/kubernetes.git
    targetRevision: main
    path: helm/sb/sb-rest-api
    helm:
      valueFiles:
        - values.yaml
  destination:
    server: https://kubernetes.default.svc
    namespace: dev
  syncPolicy:
    automated:
      prune: true      # remove resources deleted from git
      selfHeal: true   # revert manual kubectl changes back to match git
```

Once this `Application` is applied (`kubectl apply -f application.yaml`, with ArgoCD itself installed in the cluster), ArgoCD polls the repo, and any commit that changes `helm/sb/sb-rest-api/values.yaml` (e.g. a bumped `image.tag`) gets synced to the cluster automatically — no pipeline step needed to actually reach the cluster. The CI pipeline's only remaining job is: build the image, then commit the new tag back to git (or to a separate "deploy" repo, in the common split-repo GitOps pattern).

`selfHeal: true` is the detail worth sitting with: if someone runs `kubectl edit` directly against a GitOps-managed resource, ArgoCD reverts it back to match git on the next sync. Git becomes the actual source of truth, enforced continuously, not just at deploy time.

### **Which to use**
- **Push-based CI/CD** is simpler to set up for a single small project and is what most tutorials (and this repo's own Helm docs) demonstrate implicitly via `helm install`/`helm upgrade` run by hand or in a basic pipeline.
- **GitOps** pays off once you have multiple environments/clusters and want drift-detection, audit history, and easy rollback (`git revert` = cluster rollback) as first-class features, not pipeline scripting.

### **Gotchas**
- GitOps controllers need *read* access to your git repo but should never need cluster-admin-equivalent CI credentials sitting in a CI system — that's the actual security win, not just convenience.
- `selfHeal` is powerful but means manual `kubectl` hotfixes against a GitOps-managed namespace get silently reverted — good for enforcing discipline, surprising if you don't know it's on.
- Secrets don't belong in a plain-text git repo either way (push or pull) — pair GitOps with something like Sealed Secrets or External Secrets Operator rather than committing raw `Secret` manifests.
