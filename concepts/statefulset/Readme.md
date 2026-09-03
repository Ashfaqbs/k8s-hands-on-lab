# **Kubernetes StatefulSets**

### **Overview**
A `StatefulSet` manages pods that need one or more of the following, which a plain `Deployment` does not provide:

1. **Stable, unique network identity** — pods are named predictably (`mysql-0`, `mysql-1`, `mysql-2`, ...) instead of getting a random suffix, and each name is stable across restarts/rescheduling.
2. **Stable, per-pod persistent storage** — each pod gets its *own* PVC, created from a template, that follows that specific pod across rescheduling (pod `mysql-1` always reattaches to the same volume it had before).
3. **Ordered, graceful deployment and scaling** — pods are created/updated/deleted one at a time, in order (`mysql-0` before `mysql-1` before `mysql-2`), and each must be `Running`/`Ready` before the next starts.

This repo's other database examples (see `../PVC/K8s-Volumes-Documentation.md`) run MySQL as a plain `Deployment` with a single shared PVC — that's fine for a single-replica learning example, but it's the wrong tool the moment you want more than one replica of a stateful app, because a `Deployment`'s pods are interchangeable and don't get individually-tracked storage.

### **When to actually use one**
- Databases you're running yourself in-cluster (Postgres, MySQL, MongoDB, Cassandra, Kafka brokers, Elasticsearch/Zookeeper ensembles).
- Anything where each replica needs to know "which one am I" (e.g. replica 0 is the primary/leader by convention).
- If you're not managing your own storage/identity requirements at all — e.g. a stateless API — stick with `Deployment`.

### **Worked example: MySQL as a StatefulSet**
This mirrors the MySQL example already in `../PVC/K8s-Volumes-Documentation.md`, but done the "correct" way for anything beyond a single replica.

#### 1. Headless Service (required)
StatefulSets need a **headless** Service (`clusterIP: None`) to provide the stable DNS identity for each pod: `mysql-0.mysql.dev.svc.cluster.local`, `mysql-1.mysql.dev.svc.cluster.local`, etc.
```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: dev
  labels:
    app: mysql
spec:
  clusterIP: None
  selector:
    app: mysql
  ports:
    - port: 3306
      name: mysql
```

#### 2. StatefulSet with `volumeClaimTemplates`
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: dev
spec:
  serviceName: mysql          # must match the headless Service above
  replicas: 3
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:5.7
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: root-password
          ports:
            - containerPort: 3306
              name: mysql
          volumeMounts:
            - name: mysql-storage
              mountPath: /var/lib/mysql
  volumeClaimTemplates:       # unlike a Deployment, each pod gets its OWN PVC from this template
    - metadata:
        name: mysql-storage
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 1Gi
```

Apply:
```sh
kubectl apply -f mysql-headless-service.yaml -f mysql-statefulset.yaml
```

Expected result: pods come up **one at a time**, in order — `mysql-0` reaches `Running`/`Ready` before `mysql-1` is even created. Each gets its own PVC, auto-named `mysql-storage-mysql-0`, `mysql-storage-mysql-1`, `mysql-storage-mysql-2`:
```sh
kubectl get pods -n dev -l app=mysql
kubectl get pvc -n dev
```

Any single pod is individually addressable via its stable DNS name, useful for e.g. always talking to the primary at `mysql-0.mysql`:
```sh
kubectl run -it --rm debug --image=busybox -n dev -- nslookup mysql-0.mysql.dev.svc.cluster.local
```

### **Scaling and deleting**
```sh
kubectl scale statefulset mysql -n dev --replicas=1
```
Scaling down removes pods **in reverse order** (highest ordinal first: `mysql-2`, then `mysql-1`), but — important — **the PVCs are not deleted**. This is intentional (protects data from an accidental scale-down), but it means you must clean them up manually if you actually want the storage gone:
```sh
kubectl delete statefulset mysql -n dev
kubectl delete pvc -n dev -l app=mysql
kubectl delete service mysql -n dev
```

### **Gotchas**
- `serviceName` in the StatefulSet spec must exactly match the headless Service's name, or pod DNS registration silently doesn't work as expected.
- A `StatefulSet` alone does **not** give you replication/clustering logic — putting 3 replicas of stock `mysql:5.7` in a StatefulSet gives you 3 *independent* MySQL instances with 3 separate databases, not a replicated cluster. Real multi-node database clustering needs either an operator (e.g. a Postgres/MySQL operator) or manual replication configuration in an init container/entrypoint script.
- Pod deletion doesn't cascade to PVC deletion — a common source of "why is my old data still there after I deleted everything" confusion.
