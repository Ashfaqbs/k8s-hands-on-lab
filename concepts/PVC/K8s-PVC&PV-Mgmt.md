# **Kubernetes PVC & PV Management Documentation**

### **Overview**

In Kubernetes, Persistent Volumes (PV) and Persistent Volume Claims (PVC) are used to manage storage in a way that is independent of the lifecycle of individual pods. When we create a PVC, Kubernetes will either dynamically provision or bind it to an existing PV. When we delete the PVC, the behavior of the associated PV depends on its `ReclaimPolicy`.

In this document, we will explain how to create and manage PVCs and PVs, how to handle data persistence, and how to clean up resources correctly.

### **PVC and Deployment YAML Example**

The following YAML defines a **PVC** and a **Deployment** for MySQL. The PVC requests storage, and the MySQL Deployment mounts the volume for data persistence.

#### **PersistentVolumeClaim (PVC) Definition**
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pv-claim
  labels:
    app: mysql
    tier: database
spec:
  accessModes:
    - ReadWriteOnce   # Specifies that the volume will be mounted as read-write by a single node
  resources:
    requests:
      storage: 1Gi    # Requesting 1Gi of storage for the MySQL database
```

#### **MySQL Deployment Definition**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  labels:
    app: mysql
    tier: database
spec:
  selector:
    matchLabels:
      app: mysql
      tier: database
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: mysql
        tier: database
    spec:
      containers:
        - image: mysql:5.7
          args:
            - "--ignore-db-dir=lost+found"
          name: mysql
          env:
            - name: MYSQL_ROOT_PASSWORD
              value: root
            - name: MYSQL_DATABASE
              value: tempSchema
          ports:
            - containerPort: 3306
              name: mysql
          volumeMounts:
            - name: mysql-persistent-storage
              mountPath: /var/lib/mysql   # Mount the PVC to store MySQL data
      volumes:
        - name: mysql-persistent-storage
          persistentVolumeClaim:
            claimName: mysql-pv-claim  # Reference the PVC for the volume
```

---

### **How Kubernetes PVCs and PVs Work**

1. **PersistentVolumeClaim (PVC)**:
   - The PVC requests storage resources from Kubernetes. It is like a "claim" for storage, specifying the amount and access mode required (e.g., `ReadWriteOnce`).
   - Kubernetes will bind the PVC to a PV that matches the requested storage size and access mode.

2. **PersistentVolume (PV)**:
   - The PV is a resource that represents actual storage. It can be provisioned manually or dynamically (e.g., using a storage class).
   - When a PVC is created, Kubernetes looks for a PV that satisfies the request (in terms of size, access mode, etc.).
   - PVs have a `ReclaimPolicy` that determines what happens to the volume when the PVC is deleted.

---

### **Understanding `ReclaimPolicy`**

The `ReclaimPolicy` of a PV determines what happens to the underlying storage after the PVC is deleted:

- **Retain**: The PV and the data remain intact after the PVC is deleted. The PV will stay in a "Released" state, and we can manually clean up or reuse it.
- **Delete**: The PV and the underlying storage are deleted when the PVC is deleted.
- **Recycle** (Deprecated): This would delete the data and make the volume available for a new PVC. This policy is no longer commonly used.

---

### **Inspecting and Modifying the PV**

Based on the provided command output, here is the YAML definition of our PV, which has a `ReclaimPolicy` set to `Delete`. This means the PV and its underlying storage will be automatically deleted when the PVC is deleted.

#### **PersistentVolume (PV) Details**

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  annotations:
    hostPathProvisionerIdentity: b269925f-d28e-4465-9787-68fc808cf114
    pv.kubernetes.io/provisioned-by: k8s.io/minikube-hostpath
  creationTimestamp: "2025-03-12T04:28:14Z"
  finalizers:
    - kubernetes.io/pv-protection
  name: pvc-f4e4ec6a-bc00-4d34-869d-2a35952c61eb
spec:
  accessModes:
    - ReadWriteOnce
  capacity:
    storage: 1Gi
  claimRef:
    apiVersion: v1
    kind: PersistentVolumeClaim
    name: mysql-pv-claim
    namespace: default
  hostPath:
    path: /tmp/hostpath-provisioner/default/mysql-pv-claim
    type: ""
  persistentVolumeReclaimPolicy: Delete  # PV will be deleted when PVC is deleted
  storageClassName: standard
  volumeMode: Filesystem
status:
  phase: Released
```

---

### **Steps for Cleanup & Data Management**

1. **When Deleting PVC**:
   - If the `ReclaimPolicy` is set to **Delete**, the PV and its data will be deleted automatically when we delete the PVC.
   - If the `ReclaimPolicy` is set to **Retain**, the PV will remain in the `Released` state, and we need to manually clean up the data if required.

2. **Manually Deleting PV**:
   If we have a `Retain` policy and wish to delete the PV manually, run:
   ```bash
   kubectl delete pv <pv-name>
   ```

3. **ReclaimPolicy Modifications**:
   If we want to change the `ReclaimPolicy` for a PV, use:
   ```bash
   kubectl edit pv <pv-name>
   ```
   Change the `persistentVolumeReclaimPolicy` to either `Retain` or `Delete` based on our needs.

---

### **Conclusion**

In Kubernetes, PVCs and PVs help we manage persistent storage for our applications. Understanding the `ReclaimPolicy` is crucial in controlling how storage is handled when the PVC is deleted. If we want automatic cleanup, set the `ReclaimPolicy` to `Delete`. If we want to keep data even after PVC deletion, use `Retain` and clean it up manually.

---
