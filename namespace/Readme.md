To create and work with pods in a Minikube cluster on a Windows system, follow these steps:

---

### 1. **Set up Minikube**
Ensure that Minikube is installed and running:
1. Open Command Prompt or PowerShell as Administrator.
2. Start Minikube:
   ```sh
   minikube start
   ```

---

### 2. **Create a Pod Without a Namespace**
#### Create a YAML file for the Pod:
Create a file named `pod-without-namespace.yaml` with the following content:
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-without-namespace
spec:
  containers:
    - name: nginx-container
      image: nginx:latest
      ports:
        - containerPort: 80
```

#### Apply the YAML file:
```sh
kubectl apply -f pod-without-namespace.yaml
```

---

### 3. **Create a Pod in a Specific Namespace**
#### Create a Namespace:
```sh
kubectl create namespace my-namespace
```

#### Create a YAML file for the Pod in the namespace:
Create a file named `pod-with-namespace.yaml` with the following content:
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-with-namespace
  namespace: my-namespace
spec:
  containers:
    - name: nginx-container
      image: nginx:latest
      ports:
        - containerPort: 80
```

#### Apply the YAML file:
```sh
kubectl apply -f pod-with-namespace.yaml
```

---

### 4. **Verify Pods**
List the pods:
- Pods in the default namespace:
  ```sh
  kubectl get pods
  ```
- Pods in the `my-namespace` namespace:
  ```sh
  kubectl get pods -n my-namespace
  ```

---

### 5. **Check Logs of the Pods**
- **Logs of the Pod without namespace:**
  ```sh
  kubectl logs pod-without-namespace
  ```
- **Logs of the Pod in the namespace:**
  ```sh
  kubectl logs pod-with-namespace -n my-namespace
  ```

---

### 6. **Clean Up**
Delete the pods and namespace:
```sh
kubectl delete -f pod-without-namespace.yaml
kubectl delete -f pod-with-namespace.yaml
kubectl delete namespace my-namespace
```

---

-- Command line 
```
C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl apply -f pod-without-namespace.yaml
pod/pod-without-namespace created

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl create namespace my-namespace
namespace/my-namespace created

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl apply -f pod-with-namespace.yaml
pod/pod-with-namespace created

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>
C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl get pods
NAME                                            READY   STATUS              RESTARTS   AGE
jvm-9016-7cdf6479b4-m452j                       2/2     Running             0          4h6m
pod-without-namespace                           0/1     ContainerCreating   0          34s
poolmgr-go-default-6415-759c5867f8-5b9kw        2/2     Running             0          4h6m
poolmgr-go-default-6415-759c5867f8-6w2gw        2/2     Running             0          4h6m
poolmgr-go-default-6415-759c5867f8-sgxbn        2/2     Running             0          4h6m
poolmgr-java-default-8036-5fdd8ffd64-2n2x6      2/2     Running             0          4h6m
poolmgr-java-default-8036-5fdd8ffd64-92kll      2/2     Running             0          4h6m
poolmgr-java-default-8036-5fdd8ffd64-mmpsw      2/2     Running             0          4h6m
poolmgr-jvm-default-9016-8b4dcb6bd-75bg8        2/2     Running             0          4h6m
poolmgr-jvm-default-9016-8b4dcb6bd-bmpv4        2/2     Running             0          4h6m
poolmgr-jvm-default-9016-8b4dcb6bd-rvzgv        2/2     Running             0          4h6m
poolmgr-nodejs-default-1043-599f6d7bd4-45ktz    2/2     Running             0          4h6m
poolmgr-nodejs-default-1043-599f6d7bd4-n9cqf    2/2     Running             0          4h6m
poolmgr-nodejs-default-1043-599f6d7bd4-pfjkc    2/2     Running             0          4h6m
poolmgr-py-env-default-11366-785bf8c8c8-2kfnz   2/2     Running             0          3h57m
poolmgr-py-env-default-11366-785bf8c8c8-c8hmj   2/2     Running             0          3h57m
poolmgr-py-env-default-11366-785bf8c8c8-fgpsb   2/2     Running             0          3h55m
poolmgr-python-default-6103-5f745db56-glwh8     2/2     Running             0          4h6m
poolmgr-python-default-6103-5f745db56-nrkp9     2/2     Running             0          4h6m
poolmgr-python-default-6103-5f745db56-qb274     2/2     Running             0          4h6m

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl get pods -n my-namespace
NAME                 READY   STATUS              RESTARTS   AGE
pod-with-namespace   0/1     ContainerCreating   0          30s

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl logs pod-without-namespace
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
2024/12/27 17:18:19 [notice] 1#1: using the "epoll" event method
2024/12/27 17:18:19 [notice] 1#1: nginx/1.27.3
2024/12/27 17:18:19 [notice] 1#1: built by gcc 12.2.0 (Debian 12.2.0-14)
2024/12/27 17:18:19 [notice] 1#1: OS: Linux 5.15.153.1-microsoft-standard-WSL2
2024/12/27 17:18:19 [notice] 1#1: getrlimit(RLIMIT_NOFILE): 1048576:1048576
2024/12/27 17:18:19 [notice] 1#1: start worker processes
2024/12/27 17:18:19 [notice] 1#1: start worker process 29
2024/12/27 17:18:19 [notice] 1#1: start worker process 30
2024/12/27 17:18:19 [notice] 1#1: start worker process 31
2024/12/27 17:18:19 [notice] 1#1: start worker process 32
2024/12/27 17:18:19 [notice] 1#1: start worker process 33
2024/12/27 17:18:19 [notice] 1#1: start worker process 34
2024/12/27 17:18:19 [notice] 1#1: start worker process 35
2024/12/27 17:18:19 [notice] 1#1: start worker process 36
2024/12/27 17:18:19 [notice] 1#1: start worker process 37
2024/12/27 17:18:19 [notice] 1#1: start worker process 38
2024/12/27 17:18:19 [notice] 1#1: start worker process 39
2024/12/27 17:18:19 [notice] 1#1: start worker process 40
2024/12/27 17:18:19 [notice] 1#1: start worker process 41
2024/12/27 17:18:19 [notice] 1#1: start worker process 42
2024/12/27 17:18:19 [notice] 1#1: start worker process 43
2024/12/27 17:18:19 [notice] 1#1: start worker process 44

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl logs pod-with-namespace -n my-namespace
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
2024/12/27 17:18:22 [notice] 1#1: using the "epoll" event method
2024/12/27 17:18:22 [notice] 1#1: nginx/1.27.3
2024/12/27 17:18:22 [notice] 1#1: built by gcc 12.2.0 (Debian 12.2.0-14)
2024/12/27 17:18:22 [notice] 1#1: OS: Linux 5.15.153.1-microsoft-standard-WSL2
2024/12/27 17:18:22 [notice] 1#1: getrlimit(RLIMIT_NOFILE): 1048576:1048576
2024/12/27 17:18:22 [notice] 1#1: start worker processes
2024/12/27 17:18:22 [notice] 1#1: start worker process 29
2024/12/27 17:18:22 [notice] 1#1: start worker process 30
2024/12/27 17:18:22 [notice] 1#1: start worker process 31
2024/12/27 17:18:22 [notice] 1#1: start worker process 32
2024/12/27 17:18:22 [notice] 1#1: start worker process 33
2024/12/27 17:18:22 [notice] 1#1: start worker process 34
2024/12/27 17:18:22 [notice] 1#1: start worker process 35
2024/12/27 17:18:22 [notice] 1#1: start worker process 36
2024/12/27 17:18:22 [notice] 1#1: start worker process 37
2024/12/27 17:18:22 [notice] 1#1: start worker process 38
2024/12/27 17:18:22 [notice] 1#1: start worker process 39
2024/12/27 17:18:22 [notice] 1#1: start worker process 40
2024/12/27 17:18:22 [notice] 1#1: start worker process 41
2024/12/27 17:18:22 [notice] 1#1: start worker process 42
2024/12/27 17:18:22 [notice] 1#1: start worker process 43
2024/12/27 17:18:22 [notice] 1#1: start worker process 44

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl delete -f pod-without-namespace.yaml
pod "pod-without-namespace" deleted

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl delete -f pod-with-namespace.yaml
pod "pod-with-namespace" deleted

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>kubectl delete namespace my-namespace
namespace "my-namespace" deleted

C:\Users\ashfa\OneDrive\Desktop\My-Learning\k8s\kubernetes\namespace>
```