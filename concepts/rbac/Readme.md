# **Kubernetes RBAC (Role-Based Access Control)**

### **Overview**
RBAC controls **who can do what** against the Kubernetes API. "Who" is a user, group, or `ServiceAccount`. "What" is a set of verbs (`get`, `list`, `watch`, `create`, `update`, `delete`, ...) against a set of resources (`pods`, `deployments`, `secrets`, ...). Without RBAC rules granting access, the default posture is deny.

There are four RBAC objects, split along two axes — **namespaced vs cluster-wide**, and **the permission set vs the grant of that permission**:

| | Defines permissions | Grants permissions to a subject |
|---|---|---|
| **Namespaced** | `Role` | `RoleBinding` |
| **Cluster-wide** | `ClusterRole` | `ClusterRoleBinding` |

- **Role**: a set of permissions scoped to a single namespace (e.g. "can read pods in `dev`").
- **ClusterRole**: the same idea but cluster-wide, or for cluster-scoped resources (nodes, namespaces themselves, PVs) that have no namespace to scope to.
- **RoleBinding**: grants a `Role` (or even a `ClusterRole`, restricted to one namespace) to a subject.
- **ClusterRoleBinding**: grants a `ClusterRole` to a subject across the whole cluster.

### **Why it matters**
Every pod that talks to the K8s API (via a client library, `kubectl` from inside a pod, an operator, a CI/CD job) does so as a `ServiceAccount`. If you don't create one, it uses the namespace's `default` ServiceAccount, which by default has almost no permissions — which is the right instinct. RBAC is how you grant that identity exactly the permissions it needs and nothing more (principle of least privilege).

### **Worked Example**
Goal: a ServiceAccount `pod-reader-sa` in namespace `dev` that can only `get`/`list`/`watch` Pods in `dev` — nothing else, no other namespace.

#### 1. ServiceAccount
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: pod-reader-sa
  namespace: dev
```

#### 2. Role (the permission set, scoped to `dev`)
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader-role
  namespace: dev
rules:
  - apiGroups: [""]          # "" = core API group (pods, services, etc.)
    resources: ["pods"]
    verbs: ["get", "list", "watch"]
```

#### 3. RoleBinding (the grant)
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: pod-reader-binding
  namespace: dev
subjects:
  - kind: ServiceAccount
    name: pod-reader-sa
    namespace: dev
roleRef:
  kind: Role
  name: pod-reader-role
  apiGroup: rbac.authorization.k8s.io
```

Apply all three:
```sh
kubectl apply -f serviceaccount.yaml -f role.yaml -f rolebinding.yaml
```

### **Verifying the grant**
`kubectl auth can-i` lets you impersonate the ServiceAccount and check a specific verb/resource without actually needing a pod running as that identity:

```sh
# Should print "yes"
kubectl auth can-i get pods --as=system:serviceaccount:dev:pod-reader-sa -n dev

# Should print "no" — wrong namespace
kubectl auth can-i get pods --as=system:serviceaccount:dev:pod-reader-sa -n default

# Should print "no" — verb not granted
kubectl auth can-i delete pods --as=system:serviceaccount:dev:pod-reader-sa -n dev
```

A pod actually using this identity just needs `serviceAccountName: pod-reader-sa` added to its pod spec:
```yaml
spec:
  serviceAccountName: pod-reader-sa
  containers:
    - name: app
      image: my-app:latest
```

### **ClusterRole / ClusterRoleBinding variant**
If the same ServiceAccount needed to read pods in *every* namespace, swap `Role`→`ClusterRole` and `RoleBinding`→`ClusterRoleBinding` (dropping `namespace` from the RoleBinding-equivalent, since the grant is now cluster-wide):

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: pod-reader-clusterrole
rules:
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: pod-reader-clusterbinding
subjects:
  - kind: ServiceAccount
    name: pod-reader-sa
    namespace: dev
roleRef:
  kind: ClusterRole
  name: pod-reader-clusterrole
  apiGroup: rbac.authorization.k8s.io
```

### **Gotchas**
- A `RoleBinding` *can* reference a `ClusterRole` (common pattern: define reusable ClusterRoles like `view`/`edit`/`admin`, then bind them per-namespace with RoleBindings). But a `Role` can never be referenced cluster-wide — it's namespace-bound by definition.
- `apiGroups: [""]` is the core group (pods, services, configmaps, secrets...). Resources like Deployments live under `apps`, Ingresses under `networking.k8s.io` — get the `apiGroup` wrong and the rule silently matches nothing.
- RBAC only controls the Kubernetes API. It says nothing about network access between pods — that's what `NetworkPolicy` is for (see `../network-policy`).

### **Cleanup**
```sh
kubectl delete rolebinding pod-reader-binding -n dev
kubectl delete role pod-reader-role -n dev
kubectl delete serviceaccount pod-reader-sa -n dev
```
