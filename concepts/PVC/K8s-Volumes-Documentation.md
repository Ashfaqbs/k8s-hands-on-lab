# **Kubernetes Volumes Documentation**

### **Overview**
In Kubernetes, volumes provide a way for containers to persist and share data across different pods, just like Docker Volumes. However, Kubernetes Volumes have some unique features and support for many different types of persistent and ephemeral storage solutions.

Kubernetes Volumes are used to:
- **Persist data**: Volumes allow data to persist across container restarts.
- **Share data between containers**: Volumes provide a shared storage between multiple containers within the same pod.

### **Types of Volumes in Kubernetes**
Kubernetes offers several types of volumes, each suited for different use cases. Here are the common ones:

1. **emptyDir**: 
   - A temporary volume that is created when a pod is assigned to a node. It is stored on the node's filesystem and is erased when the pod is deleted.
   - Useful for temporary storage that doesn't need to persist across pod restarts.

2. **hostPath**:
   - Mounts a file or directory from the host node’s filesystem into the pod. This is commonly used for development or testing.
   - Be cautious when using it, as it ties the pod’s lifecycle to the host machine.

3. **persistentVolumeClaim (PVC)**:
   - A volume that is backed by a PersistentVolume (PV), and it represents storage that is either dynamically provisioned or manually assigned.
   - Suitable for long-term storage and data persistence across pod restarts or re-creations.

4. **configMap**:
   - A volume that is populated with data from a Kubernetes ConfigMap. It is used to inject configuration into containers.

5. **secret**:
   - Similar to ConfigMap, but used to store sensitive data such as passwords, tokens, or SSH keys.

6. **nfs**:
   - Mounts an NFS share as a volume, allowing multiple pods to access shared storage.

7. **azureDisk, gcePersistentDisk, awsElasticBlockStore, etc.**:
   - These are cloud provider-specific volumes that allow Kubernetes pods to mount storage from specific cloud storage services like Azure, Google Cloud, or AWS.

8. **csi (Container Storage Interface)**:
   - This is a standardized way to manage storage and provides flexibility for using storage solutions across cloud providers and on-prem environments.

### **Configuring Volumes in Kubernetes**

Volumes are defined in the pod spec, and each container in the pod can reference a volume. The configuration is part of the pod YAML file.

#### **Volume Configuration in a Pod**
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: mysql-pod
spec:
  containers:
    - name: mysql
      image: mysql:5.7
      volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql  # MySQL data directory in the container
  volumes:
    - name: mysql-storage
      persistentVolumeClaim:
        claimName: mysql-pv-claim  # Referring to the PVC for persistent storage
```

In the example above, the pod uses a **PersistentVolumeClaim (PVC)** named `mysql-pv-claim`. This PVC will automatically bind to a corresponding **PersistentVolume (PV)**, which will store the data outside the pod's lifecycle, making it persistent across pod restarts.

### **Using Volumes in a MySQL Deployment**

Here’s how we would define a **MySQL Deployment** using a PVC to manage the database's persistent storage.

#### **MySQL PVC and Deployment Example**

This is the manifest we shared with me, where the volume is specified for the MySQL database.

##### **PersistentVolumeClaim (PVC)**
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
    - ReadWriteOnce  # Only one node can access the volume at a time
  resources:
    requests:
      storage: 1Gi   # Requesting 1GB of storage
```

##### **MySQL Deployment with Volume Mount**
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
              mountPath: /var/lib/mysql  # Mount path in the container for DB data
      volumes:
        - name: mysql-persistent-storage
          persistentVolumeClaim:
            claimName: mysql-pv-claim  # Linking the PVC to the pod's volume
```

In this configuration:
- **PVC (`mysql-pv-claim`)**: The request for storage (1Gi).
- **Volume (`mysql-persistent-storage`)**: Refers to the PVC and binds the storage to the MySQL container.
- **Mount Path (`/var/lib/mysql`)**: The data will be saved in this directory inside the MySQL container.

### **Managing Volumes in Kubernetes**

we can use the following commands to manage volumes in Kubernetes.

#### **1. Get Volumes (PVC & PV) Information**

- To see all PVCs:
  ```bash
  kubectl get pvc
  ```
  
- To see detailed information about a specific PVC:
  ```bash
  kubectl get pvc <pvc-name> -o yaml
  ```

- To see all PVs:
  ```bash
  kubectl get pv
  ```

- To see detailed information about a specific PV:
  ```bash
  kubectl get pv <pv-name> -o yaml
  ```

#### **2. Delete PVC and PV**

- To delete a PVC:
  ```bash
  kubectl delete pvc <pvc-name>
  ```

- To delete a PV:
  ```bash
  kubectl delete pv <pv-name>
  ```

#### **3. Viewing Volume Content**
  
If we want to see the content of a volume, one approach is to create a temporary pod that mounts the volume and then access it. For example:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: volume-inspector
spec:
  containers:
  - name: alpine
    image: alpine
    command: [ "sh", "-c", "while true; do sleep 3600; done" ]
    volumeMounts:
    - name: mysql-persistent-storage
      mountPath: /mnt/data  # Mount the volume at this path
  volumes:
  - name: mysql-persistent-storage
    persistentVolumeClaim:
      claimName: mysql-pv-claim
```

After the pod is created, we can exec into the pod and view the content of `/mnt/data`:

```bash
kubectl exec -it volume-inspector -- /bin/sh
ls /mnt/data  # View contents of the mounted volume
```

#### **4. Storage Size in Kubernetes**

Kubernetes supports a variety of storage backends. When specifying storage in a PVC (e.g., `1Gi`), Kubernetes translates that request into an underlying storage resource based on the storage class and volume provider (e.g., host disk, cloud provider storage).

- **1Gi (Gibibyte)**: Kubernetes uses binary prefixes (Gi) instead of decimal (GB). 1Gi = 1,073,741,824 bytes (1024^3). This is different from the common 1GB (1,000,000,000 bytes) used in consumer systems.

- The capacity specified in the `resources.requests.storage` field will determine how much space our volume is allocated from the underlying storage provider.

---

### **Conclusion**

Kubernetes Volumes are a powerful abstraction for managing storage in a containerized environment. They allow for persistence, sharing data, and managing different types of storage backends (local, cloud, etc.). By using PVCs and PVs, Kubernetes decouples storage management from container lifecycles, ensuring that our data survives pod restarts or recreations.

This guide covered:
- The different types of Kubernetes volumes.
- How to configure and manage volumes in Kubernetes.
- Commands for managing PVCs and PVs.
- How to configure our MySQL DB with persistent storage using volumes.
