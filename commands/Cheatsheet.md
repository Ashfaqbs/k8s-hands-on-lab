# **Kubernetes Commands Cheat Sheet**

### **1. Deployments Commands**

#### **Get Deployments**
```bash
kubectl get deployments
```

#### **Get Deployment (Detailed)**
```bash
kubectl get deployment <deployment-name> -o yaml
```

#### **Create Deployment from YAML Manifest**
```bash
kubectl apply -f <deployment-file>.yaml
```

#### **Update Deployment (Change Image)**
```bash
kubectl set image deployment/<deployment-name> <container-name>=<new-image-name>
```

#### **Scale Deployment**
```bash
kubectl scale deployment <deployment-name> --replicas=<num-replicas>
```

#### **Delete Deployment**
```bash
kubectl delete deployment <deployment-name>
```

---

### **2. Services Commands**

#### **Get Services**
```bash
kubectl get services
```

#### **Get Service (Detailed)**
```bash
kubectl get service <service-name> -o yaml
```

#### **Create Service from YAML Manifest**
```bash
kubectl apply -f <service-file>.yaml
```

#### **Delete Service**
```bash
kubectl delete service <service-name>
```

---

### **3. Secrets Commands**

#### **Get Secrets**
```bash
kubectl get secrets
```

#### **Get Secret (Detailed)**
```bash
kubectl get secret <secret-name> -o yaml
```

#### **Create Secret from YAML Manifest**
```bash
kubectl apply -f <secret-file>.yaml
```

#### **Delete Secret**
```bash
kubectl delete secret <secret-name>
```

---

### **4. ConfigMaps Commands**

#### **Get ConfigMaps**
```bash
kubectl get configmaps
```

#### **Get ConfigMap (Detailed)**
```bash
kubectl get configmap <configmap-name> -o yaml
```

#### **Create ConfigMap from YAML Manifest**
```bash
kubectl apply -f <configmap-file>.yaml
```

#### **Delete ConfigMap**
```bash
kubectl delete configmap <configmap-name>
```

---

### **5. PVCs Commands**

#### **Get PVCs**
```bash
kubectl get pvc
```

#### **Get PVC (Detailed)**
```bash
kubectl get pvc <pvc-name> -o yaml
```

#### **Create PVC from YAML Manifest**
```bash
kubectl apply -f <pvc-file>.yaml
```

#### **Delete PVC**
```bash
kubectl delete pvc <pvc-name>
```

---

### **6. PVs Commands**

#### **Get PVs**
```bash
kubectl get pv
```

#### **Get PV (Detailed)**
```bash
kubectl get pv <pv-name> -o yaml
```

#### **Create PV from YAML Manifest**
```bash
kubectl apply -f <pv-file>.yaml
```

#### **Delete PV**
```bash
kubectl delete pv <pv-name>
```

---

### **7. Pods Commands**

#### **Get Pods**
```bash
kubectl get pods
```

#### **Get Pod (Detailed)**
```bash
kubectl get pod <pod-name> -o yaml
```

#### **Create Pod from YAML Manifest**
```bash
kubectl apply -f <pod-file>.yaml
```

#### **Delete Pod**
```bash
kubectl delete pod <pod-name>
```

#### **Get Pod Logs**
```bash
kubectl logs <pod-name>
```

#### **Exec into Pod**
```bash
kubectl exec -it <pod-name> -- /bin/bash
```

---

### **Additional Useful Commands**

#### **View Cluster Information**
```bash
kubectl cluster-info
```

#### **View Resource Usage**
```bash
kubectl top pods
kubectl top nodes
```

#### **View Events in the Cluster**
```bash
kubectl get events
```
