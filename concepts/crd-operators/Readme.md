# **CustomResourceDefinitions (CRDs) & Operators**

### **Overview**
Kubernetes ships with a fixed set of built-in resource kinds — `Pod`, `Deployment`, `Service`, `ConfigMap`, and so on. A **CustomResourceDefinition (CRD)** lets you register a *new* kind, so the API server accepts, stores, and serves it exactly like a built-in resource (`kubectl get`, `kubectl apply`, RBAC rules — all of it works identically). A CRD alone is just schema + storage — a place to declare desired state. It does nothing on its own.

An **Operator** is the other half: a controller (usually just a pod, or a Deployment of one) that watches instances of a CRD and does the real work to make the world match what's declared — the same **watch → diff → act** control loop that every built-in Kubernetes controller already uses (e.g. the Deployment controller watching `Deployment` objects and creating/deleting `ReplicaSet`s to match). An Operator is that pattern applied to a problem domain-specific enough that it isn't built into Kubernetes itself — running a Postgres cluster with automated failover, renewing TLS certs, managing Kafka topics, etc.

**You've already used one in this repo**: `kube-prometheus-stack` (see `../observability`) installs the **Prometheus Operator**, which watches `Prometheus`, `ServiceMonitor`, and `Alertmanager` CRD instances and translates them into actual running Prometheus configuration. And **Fission** (`../../observations/rancher-desktop/fission`) is built the same way — a Fission `Function` is a CRD, and Fission's controllers watch it to spin up the actual pool-manager pods that execute your code. Every "install this thing via `kubectl apply -f its-crds.yaml` then `helm install its-operator`" pattern you've run in this repo is this exact mechanism.

### **A minimal CRD, from scratch**
The classic teaching example — a `CronTab` kind that doesn't do anything real, just to see the mechanics:

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: crontabs.example.com          # must be <plural>.<group>
spec:
  group: example.com
  names:
    kind: CronTab
    plural: crontabs
    singular: crontab
    shortNames: ["ct"]
  scope: Namespaced
  versions:
    - name: v1
      served: true
      storage: true
      schema:
        openAPIV3Schema:
          type: object
          properties:
            spec:
              type: object
              properties:
                schedule:
                  type: string
                image:
                  type: string
```
```sh
kubectl apply -f crontab-crd.yaml
```

The moment this is applied, the API server understands the new kind — before writing a single line of controller code:
```sh
kubectl get crontabs
kubectl get ct        # the shortName works immediately too
```

A **custom resource** — an *instance* of this new kind — is just YAML like any built-in object:
```yaml
apiVersion: example.com/v1
kind: CronTab
metadata:
  name: my-nightly-job
spec:
  schedule: "0 2 * * *"
  image: my-batch-job:latest
```
```sh
kubectl apply -f my-crontab.yaml
kubectl get crontab my-nightly-job -o yaml
```

At this point, `kubectl` will happily create, list, and delete `CronTab` objects — but nothing actually reads `spec.schedule` and creates a real `CronJob` from it. That's the missing half: the controller.

### **The control loop (what an Operator actually does)**
Conceptually, whether hand-written or generated:
```
loop forever:
    watch CronTab objects for create/update/delete events
    for each CronTab:
        read its desired spec (schedule, image)
        compare to the real world (does a matching CronJob exist? does it match?)
        if not matching: create/update/delete the real CronJob to reconcile
        write status back onto the CronTab (e.g. lastScheduleTime)
```
This is identical in shape to how the built-in Deployment controller reconciles ReplicaSets, or how the Prometheus Operator reconciles a `ServiceMonitor` into real scrape config.

### **How you'd actually build one**
Nobody hand-writes the watch/informer/work-queue machinery from scratch anymore. The standard toolchain:
- **client-go**: the low-level Go Kubernetes client and informer/work-queue primitives everything else is built on.
- **Kubebuilder** / **Operator SDK**: scaffolding tools that generate the CRD YAML from Go struct tags, generate the controller boilerplate, and leave you to fill in just the `Reconcile()` function with your domain logic.

A Kubebuilder-generated reconcile loop for the `CronTab` example above would, in a few dozen lines, fetch the `CronTab`, check for an owned `batch/v1 CronJob`, and create/update/delete it to match — exactly the pseudocode above, made concrete.

### **Gotchas**
- A CRD's schema (`openAPIV3Schema`) is enforced by the API server on `kubectl apply` — malformed custom resources are rejected before they're even stored, same as a malformed built-in resource.
- Deleting a CRD deletes **every instance of that custom resource** across the whole cluster, in every namespace — there's no soft-delete or per-namespace CRD scoping (a CRD registration is always cluster-wide, even when `scope: Namespaced` governs its instances).
- An Operator is only as safe as its RBAC — since it needs permission to create/modify whatever real resources it manages, a buggy or malicious Operator effectively holds those same permissions (see `../rbac`).

### **Cleanup**
```sh
kubectl delete crontab my-nightly-job
kubectl delete crd crontabs.example.com
```
