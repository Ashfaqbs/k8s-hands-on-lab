
### 🔍 **Basic Commands**  
```sh
kubectl version                     # Check kubectl version  
kubectl config current-context       # Show current context  
kubectl config use-context <context> # Switch context  
kubectl cluster-info                 # Display cluster info  
kubectl get nodes                    # List all nodes in the cluster  
kubectl get pods -A                  # List all pods in all namespaces  
```

---

### 🛠 **Pod Management**  
```sh
kubectl get pods                     # List all pods in current namespace  
kubectl get pods -n <namespace>       # List pods in a specific namespace  
kubectl describe pod <pod-name>       # Show details of a pod  
kubectl logs <pod-name>               # View logs of a pod  
kubectl logs -f <pod-name>            # Stream pod logs (follow mode)  
kubectl exec -it <pod-name> -- sh     # Open shell inside a pod (if it has sh/bash)  
```

---

### 📦 **Deployment & Service Management**  
```sh
kubectl get deployments              # List deployments  
kubectl describe deployment <name>   # Describe a deployment  
kubectl scale deployment <name> --replicas=3  # Scale a deployment  
kubectl delete deployment <name>     # Delete a deployment  
```
```sh
kubectl get services                 # List services  
kubectl describe service <name>      # Describe a service  
kubectl delete service <name>        # Delete a service  
```

---

### 📜 **YAML-based Resource Management**  
```sh
kubectl apply -f <file>.yaml         # Apply a YAML config  
kubectl create -f <file>.yaml        # Create resources from YAML  
kubectl delete -f <file>.yaml        # Delete resources from YAML  
```

---

### 🔄 **Namespace & Context Management**  
```sh
kubectl get namespaces               # List all namespaces  
kubectl create namespace <name>      # Create a new namespace  
kubectl delete namespace <name>      # Delete a namespace  
kubectl config set-context --current --namespace=<name>  # Set default namespace  
```

---

### 🔥 **Deleting Resources**  
```sh
kubectl delete pod <pod-name>        # Delete a pod  
kubectl delete deployment <name>     # Delete a deployment  
kubectl delete service <name>        # Delete a service  
kubectl delete namespace <name>      # Delete a namespace  
kubectl delete all --all             # Delete all resources in the current namespace  
```

---

### 🚀 **Port Forwarding & Exposure**  
```sh
kubectl port-forward pod/<pod-name> 8080:80  # Forward port 8080 to pod's port 80  
kubectl expose deployment <name> --type=NodePort --port=80  # Expose a deployment as a service  
```

---

### 📊 **Troubleshooting & Debugging**  
```sh
kubectl get events --sort-by=.metadata.creationTimestamp  # View events sorted by time  
kubectl top pods                        # Show resource usage of pods  
kubectl top nodes                        # Show resource usage of nodes  
kubectl describe pod <pod-name>          # Detailed pod info  
kubectl logs <pod-name>                  # Fetch logs  
kubectl logs -f <pod-name>               # Stream logs in real-time  
kubectl exec -it <pod-name> -- bash      # Access a running pod  
kubectl get pod <pod-name> -o yaml       # View pod YAML  
kubectl get pod <pod-name> -o wide       # Get extra pod info (Node, IP, etc.)  
```

---
