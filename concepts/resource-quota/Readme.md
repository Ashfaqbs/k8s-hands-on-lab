# **Kubernetes ResourceQuota & LimitRange**

### **Overview**
This repo's HPA docs already cover **per-pod** `resources.requests`/`resources.limits` (how much CPU/memory a single container is allowed to use). `ResourceQuota` and `LimitRange` operate one level up, at the **namespace** level:

- **`ResourceQuota`**: caps the *total* resource consumption (and/or object counts) across an entire namespace — e.g. "the `dev` namespace may use at most 4 CPU and 8Gi memory in total, across every pod combined."
- **`LimitRange`**: sets *default* requests/limits (and min/max bounds) for individual containers in a namespace, applied automatically when a pod doesn't specify its own.

They're complementary: `LimitRange` makes sure every container has *some* request/limit set (which `ResourceQuota` for CPU/memory actually requires — see gotcha below), and `ResourceQuota` caps the sum across all of them.

### **Why it matters**
On a shared cluster (multiple teams/apps in different namespaces, or multiple learning experiments in the same Minikube VM), a single runaway Deployment with no limits can consume all node resources and starve everything else. Namespace quotas are the guardrail.

### **Worked example: ResourceQuota**
Cap the `dev` namespace to a total of 2 CPU / 4Gi memory requested, 4 CPU / 8Gi memory limited, and at most 10 pods:
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: dev-quota
  namespace: dev
spec:
  hard:
    requests.cpu: "2"
    requests.memory: 4Gi
    limits.cpu: "4"
    limits.memory: 8Gi
    pods: "10"
```
```sh
kubectl apply -f dev-quota.yaml
kubectl describe resourcequota dev-quota -n dev   # shows Used vs Hard
```

Once this is applied, attempting to schedule a pod that would push the namespace over any of these limits fails at admission time with an error like:
```
Error from server (Forbidden): error when creating "pod.yaml": pods "extra-pod" is forbidden:
exceeded quota: dev-quota, requested: requests.cpu=1, used: requests.cpu=2, limited: requests.cpu=2
```
The pod is rejected outright — it never gets created, so there's no partial/broken state to clean up.

### **Worked example: LimitRange**
Give every container in `dev` a sane default if it doesn't specify its own, and enforce a min/max bound:
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: dev-limit-range
  namespace: dev
spec:
  limits:
    - type: Container
      default:               # applied as the LIMIT if a container doesn't set one
        cpu: "500m"
        memory: 256Mi
      defaultRequest:         # applied as the REQUEST if a container doesn't set one
        cpu: "250m"
        memory: 128Mi
      min:                    # reject containers requesting below this
        cpu: "100m"
        memory: 64Mi
      max:                    # reject containers requesting above this
        cpu: "1"
        memory: 512Mi
```
```sh
kubectl apply -f dev-limit-range.yaml
```

After this, deploy a pod with **no** `resources:` block at all — `kubectl describe pod` will show it was auto-assigned `requests: cpu=250m, memory=128Mi` and `limits: cpu=500m, memory=256Mi` from the `LimitRange` defaults.

### **How they interact**
1. A pod is submitted.
2. `LimitRange` admission runs first — fills in any missing `requests`/`limits` with its defaults, and rejects the pod outright if any container's (explicit or defaulted) values fall outside the configured `min`/`max`.
3. `ResourceQuota` admission runs next — checks whether the namespace's running total (including this new pod) would exceed `spec.hard`. Rejects if so.

### **Gotchas**
- **If a `ResourceQuota` sets a `requests.cpu`/`requests.memory`/`limits.cpu`/`limits.memory` hard limit, every pod created in that namespace *must* explicitly specify those requests/limits** — otherwise the pod is rejected outright, since the quota controller can't compute the running total without them. This is exactly why `LimitRange` (which auto-fills defaults) is usually deployed alongside a compute `ResourceQuota`, not as a nice-to-have.
- `ResourceQuota` can also cap object counts, not just compute: `pods`, `services`, `persistentvolumeclaims`, `services.loadbalancers`, `count/deployments.apps`, etc. — useful for keeping a shared/free-tier-style namespace from sprawling.
- Both objects are namespace-scoped — there's no cluster-wide quota object; for genuine multi-tenant clusters this is usually paired with per-namespace `ResourceQuota`+`LimitRange` templates applied by policy tooling (e.g. Kyverno/OPA Gatekeeper) rather than by hand.

### **Cleanup**
```sh
kubectl delete resourcequota dev-quota -n dev
kubectl delete limitrange dev-limit-range -n dev
```
