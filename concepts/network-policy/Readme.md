# **Kubernetes NetworkPolicy**

### **Overview**
By default, Kubernetes networking is **flat and all-allow**: any pod can reach any other pod in the cluster (and any namespace), on any port, with no restrictions. `NetworkPolicy` is how you lock that down — it's a namespaced resource that describes which traffic is allowed to/from a set of pods, selected by labels.

### **Important caveat: policies need an enforcing CNI**
A `NetworkPolicy` object is inert unless the cluster's CNI (Container Network Interface) plugin actually enforces it. Calico, Cilium, and Weave Net enforce NetworkPolicies. The default CNI on plain **Docker Desktop's Kubernetes** and some minimal **kind**/**Minikube** setups may **not** enforce them out of the box — you can apply the YAML and `kubectl get networkpolicy` will show it, but traffic won't actually be blocked. If you're testing this in Minikube, enable the Calico addon or start with `--cni=calico`:
```sh
minikube start --cni=calico
```

### **Default posture: deny all ingress**
The most common starting point in a namespace is a policy that matches every pod (`podSelector: {}`) and allows nothing in:
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-ingress
  namespace: dev
spec:
  podSelector: {}       # selects every pod in the namespace
  policyTypes:
    - Ingress
  # no "ingress:" rules block = nothing is allowed in
```

Once this is applied, every pod in `dev` rejects all incoming traffic — including from other pods in the same namespace. You then add specific `NetworkPolicy` objects to open exactly the paths you need.

### **Worked example: allow only the frontend to reach the backend**
Say you have pods labelled `app: backend` that should only accept traffic from pods labelled `app: frontend`, and only on port `8080`:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-frontend-to-backend
  namespace: dev
spec:
  podSelector:
    matchLabels:
      app: backend       # this policy applies to backend pods
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: frontend   # only allow traffic from pods labelled app=frontend
      ports:
        - protocol: TCP
          port: 8080
```

Apply both policies together:
```sh
kubectl apply -f default-deny-ingress.yaml -f allow-frontend-to-backend.yaml
```

Net effect: `backend` pods accept traffic only from `frontend` pods on port 8080. Every other pod in `dev` (including anything else you deploy later) is still fully locked down by the default-deny policy unless you write a matching allow rule for it.

### **Egress works the same way**
Everything above covers `Ingress` (traffic coming *into* a pod). `Egress` rules control traffic *leaving* a pod, using the same `podSelector`/`namespaceSelector`/`ipBlock` matching under an `egress:` block and `policyTypes: [Egress]`. A common egress use case: allow a pod to reach the cluster's internal DNS (`kube-dns`/`coredns`) and a specific external API, but nothing else.

### **Selecting by namespace, not just pod labels**
`from`/`to` can also match by namespace, useful for "allow anything in the `monitoring` namespace to scrape my pods":
```yaml
ingress:
  - from:
      - namespaceSelector:
          matchLabels:
            kubernetes.io/metadata.name: monitoring
    ports:
      - protocol: TCP
        port: 9090
```

### **Gotchas**
- `NetworkPolicy` is **allow-list only** — you cannot write an explicit "deny" rule for a specific source; you achieve deny by simply *not* including it in any allow rule (combined with a default-deny baseline).
- Policies are additive: if two policies both select the same pod, the pod gets the **union** of everything allowed by either.
- An empty `ingress: []` with `policyTypes: [Ingress]` means "deny all ingress." Omitting `policyTypes` but including an `ingress` block still implies `Ingress` is a controlled type — be explicit to avoid confusion.
- This only governs pod-to-pod/pod-to-external traffic at L3/L4. It knows nothing about HTTP paths, mTLS identity, etc. — that's the job of a service mesh (Istio/Linkerd), a separate concern.

### **Cleanup**
```sh
kubectl delete networkpolicy allow-frontend-to-backend -n dev
kubectl delete networkpolicy default-deny-ingress -n dev
```
