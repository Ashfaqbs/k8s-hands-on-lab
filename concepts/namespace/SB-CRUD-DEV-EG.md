# **Kubernetes Deployment with Namespaces, ConfigMaps, and Secrets**

## **Overview**

This document outlines the deployment of a Spring Boot application with a MySQL database using Kubernetes. The deployment includes:

- A **Spring Boot Application**
- A **MySQL Database**
- **ConfigMap & Secrets** for configuration management
- Deployment into a **dedicated** `dev` **namespace**

## **1. Creating the `dev` Namespace**

To isolate resources, we create a dedicated `dev` namespace:

```sh
kubectl create namespace dev
```

> **Note:** we can either define the namespace inside each YAML file (using `namespace: dev`) or specify it in the CLI when applying (using `-n dev`). Choose one approach to maintain clarity.

## **2. Setting Up ConfigMaps & Secrets**

### **ConfigMap**

Stores database-related configurations.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: db-config
  namespace: dev  # Assign to 'dev' namespace if using YAML definition

data:
  DB_HOST: "mysql"
  DB_NAME: "tempSchema"
```

### **Secret**

Stores database credentials securely.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-secrets
  namespace: dev

type: Opaque

data:
  username: cm9vdA==  # Base64 for 'root'
  password: cm9vdA==  # Base64 for 'root'
```

Apply them:

```sh
kubectl apply -f db-configmap.yaml -n dev  # Use -n dev if namespace isn't in YAML
kubectl apply -f db-secret.yaml -n dev
```

## **3. Deploying MySQL Database**

### **Persistent Volume Claim**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pv-claim
  namespace: dev
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

### **MySQL Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: dev
spec:
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
              name: mysql-secrets
              key: password
        - name: MYSQL_DATABASE
          valueFrom:
            configMapKeyRef:
              name: db-config
              key: DB_NAME
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pv-claim
```

### **MySQL Service**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: dev
spec:
  ports:
    - port: 3306
      targetPort: 3306
  selector:
    app: mysql
  clusterIP: None  # Used for internal DNS resolution
```

Apply MySQL setup:

```sh
kubectl apply -f mysql-pvc.yaml -n dev
kubectl apply -f mysql-deployment.yaml -n dev
kubectl apply -f mysql-service.yaml -n dev
```

## **4. Deploying the Spring Boot Application**

### **Application Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-crud-deployment
  namespace: dev
spec:
  selector:
    matchLabels:
      app: springboot-k8s-mysql
  replicas: 3
  template:
    metadata:
      labels:
        app: springboot-k8s-mysql
    spec:
      containers:
        - name: springboot-crud-k8s
          image: darksharkash/sb3j21crud-k8s:latest
          ports:
            - containerPort: 8080
          env:
            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: DB_HOST
            - name: DB_NAME
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: DB_NAME
            - name: DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: mysql-secrets
                  key: username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secrets
                  key: password
```

### **Application Service**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: springboot-crud-svc
  namespace: dev
spec:
  selector:
    app: springboot-k8s-mysql
  ports:
    - protocol: "TCP"
      port: 8080
      targetPort: 8080
  type: NodePort
```

Apply Application setup:

```sh
kubectl apply -f app-deployment.yaml -n dev
kubectl apply -f app-service.yaml -n dev
```

## **5. Verifying & Managing Namespaces**

### **Namespace Commands**

| Action                                           | Command                                                |
| ------------------------------------------------ | ------------------------------------------------------ |
| List all namespaces                              | `kubectl get namespaces`                               |
| View details of `dev`                            | `kubectl describe namespace dev`                       |
| Get all resources in `dev`                       | `kubectl get all -n dev`                               |
| Delete `dev` namespace (⚠ deletes all resources) | `kubectl delete namespace dev`                         |
| Set `dev` as default for current session         | `kubectl config set-context --current --namespace=dev` |

## **6. Summary**

- We deployed a **Spring Boot** application with a **MySQL database** inside a dedicated `dev` namespace.
- **ConfigMaps & Secrets** securely store DB credentials and settings.
- **Namespaces** provide resource isolation and better environment management.
- **Kubernetes Services** allow inter-service communication via DNS.

This setup ensures scalability, isolation, and secure configuration management for a production-ready environment.

