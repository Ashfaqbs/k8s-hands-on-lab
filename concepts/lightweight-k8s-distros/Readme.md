# **Lightweight Kubernetes Distributions: K3s & k0s**

### **Overview**
Every other doc in this repo assumes "full" Kubernetes — the kind `kubeadm` builds, or what Minikube/Docker Desktop/Rancher Desktop run locally for you (see `../../Install-k8s.md`). That's a heavy stack: a multi-binary control plane, etcd, and historically a fair amount of legacy/cloud-provider code baked in. **K3s** and **k0s** are both real, CNCF-conformance-certified Kubernetes distributions — same API, same YAML, same `kubectl` — just repackaged to run in a fraction of the footprint. They exist for edge devices, IoT, Raspberry Pi clusters, CI runners, small VPS boxes, homelabs, and increasingly as a lighter alternative to Minikube for local dev too.

"Certified conformant" is the key phrase: neither is a fork with a different API. A Deployment, Service, Ingress, or any manifest from the rest of this repo applies identically once either is running.

### **K3s (Rancher / SUSE)**
- Ships as a **single binary under 100MB** that bundles containerd, so there's nothing else to install on the host.
- **Batteries included** by default: Traefik as the Ingress controller, `ServiceLB` (a simple embedded load balancer) for `type: LoadBalancer` Services without needing a cloud provider, `local-path-provisioner` for dynamic PersistentVolumes on local disk, CoreDNS, and metrics-server — all bundled and working out of the box.
- Uses **SQLite** as the default datastore for a single-node cluster (swap to embedded etcd with `--cluster-init`, or an external Postgres/MySQL, for a real multi-node HA control plane).
- Strips out in-tree legacy cloud-provider code and some alpha/rarely-used features specifically to shrink the binary and attack surface.
- Best fit: edge/IoT, ARM devices (Raspberry Pi clusters are the classic K3s use case), ephemeral CI clusters, small production clusters where you want a working default without assembling one yourself.

**Getting started (single node):**
```sh
curl -sfL https://get.k3s.io | sh -

# k3s bundles its own kubectl wrapper
sudo k3s kubectl get nodes
```
Get a kubeconfig you can point your normal `kubectl`/tools at:
```sh
sudo cat /etc/rancher/k3s/k3s.yaml
# swap "server: https://127.0.0.1:6443" for the node's real IP if connecting remotely
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
kubectl get pods -A
```
Adding a second node as an agent (worker):
```sh
# on the server, grab the join token
sudo cat /var/lib/rancher/k3s/server/node-token

# on the new node
curl -sfL https://get.k3s.io | K3S_URL=https://<server-ip>:6443 K3S_TOKEN=<token> sh -
```

### **k0s (Mirantis)**
- Also a **single static binary**, but taken further: **zero host OS dependencies** — it doesn't even require a container runtime pre-installed, since it bundles containerd itself. Nothing to apt-get beforehand.
- Deliberately **unopinionated**: no bundled Ingress controller, no bundled LoadBalancer implementation. You choose and install what you want (e.g. ingress-nginx + MetalLB), rather than inheriting K3s's Traefik/ServiceLB defaults.
- Closer to "just upstream Kubernetes, minimally packaged" — the tradeoff for that flexibility is more assembly required to get a fully working cluster (ingress, storage class, etc. are all BYO).
- Ships with **`k0sctl`**, a separate CLI that provisions and manages multi-node clusters declaratively over SSH from a single YAML spec — this is the actual "manifest to get started" most people mean when they say k0s.
- Best fit: when you want a minimal, unopinionated base and plan to choose your own ingress/storage/CNI stack rather than accept bundled defaults.

**Getting started (single node, controller+worker combined):**
```sh
curl -sSLf https://get.k0s.sh | sudo sh

sudo k0s install controller --single
sudo k0s start

sudo k0s kubectl get nodes
```
Get a kubeconfig:
```sh
sudo k0s kubeconfig admin > kubeconfig
export KUBECONFIG=$(pwd)/kubeconfig
kubectl get pods -A
```

**`k0sctl` cluster manifest** — a declarative, multi-node cluster definition (this is the closest thing k0s has to a "getting started manifest" beyond a single install command):
```yaml
apiVersion: k0sctl.k0sproject.io/v1beta1
kind: Cluster
metadata:
  name: k0s-lab-cluster
spec:
  hosts:
    - role: controller
      ssh:
        address: 10.0.0.10
        user: root
        keyPath: ~/.ssh/id_rsa
    - role: worker
      ssh:
        address: 10.0.0.11
        user: root
        keyPath: ~/.ssh/id_rsa
    - role: worker
      ssh:
        address: 10.0.0.12
        user: root
        keyPath: ~/.ssh/id_rsa
```
```sh
k0sctl apply --config k0sctl.yaml
k0sctl kubeconfig --config k0sctl.yaml > kubeconfig
```
`k0sctl apply` SSHes into each host, installs the right k0s role (controller/worker), and wires the cluster together — genuinely comparable to running `kubeadm init` + `kubeadm join` by hand, but declared once as a spec instead of run as a sequence of imperative commands.

### **Where these fit against everything else in this repo**
| | Footprint | Opinionation | Meant for |
|---|---|---|---|
| **kubeadm** (vanilla K8s) | Heaviest | Bring-your-own everything | Real production, what managed services (EKS/AKS/GKE) build on conceptually |
| **K3s** | Light (<100MB binary) | Batteries included (Traefik, ServiceLB, local-path) | Edge/IoT, ARM, small prod, "just works" |
| **k0s** | Light (single static binary) | Minimal/unopinionated, pluggable | Same footprint goals as K3s, but you assemble the stack yourself |
| **Minikube / Docker Desktop / Rancher Desktop** (see `../../Install-k8s.md`) | Local-machine only | Varies | Local dev/test loop, not meant to run real workloads or scale to multiple physical nodes |

Once any of these is up, every other doc in this repo — RBAC, NetworkPolicy, StatefulSets, Helm charts, HPA, Ingress — applies exactly the same way, since the API underneath is identical.

### **Gotchas**
- K3s's default SQLite datastore only supports a **single** control-plane node. Going HA requires `--cluster-init` (embedded etcd) or pointing multiple servers at an external database — don't assume SQLite scales past one node.
- k0s gives you **no working Ingress or LoadBalancer** until you install one yourself. Deploying an Ingress resource on a fresh k0s cluster with nothing else installed will just sit there unrouted — this is the single most common "why can't I reach my app" moment with k0s, unlike K3s where Traefik answers immediately.
- Neither replaces Minikube/kind for local dev-loop use cases (fast create/destroy, tight IDE integration) — they're aimed at real (if small) persistent clusters, edge or otherwise.
