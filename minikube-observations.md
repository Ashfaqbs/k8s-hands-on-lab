# Docker images changes pre and post starting minikube.

```
C:\Users\ashfa>docker images
REPOSITORY                                TAG                                                                           IMAGE ID       CREATED         SIZE
darksharkash/vite-react-app               v1                                                                            eae90fc8d71d   5 hours ago     205MB
darksharkash/sb3j21crud-k8s               latest                                                                        60971bf44089   23 hours ago    263MB
darksharkash/simplerestapisb-k8s          latest                                                                        af010efbe212   3 days ago      204MB
docker/desktop-kubernetes                 kubernetes-v1.30.5-cni-v1.4.0-critools-v1.29.0-cri-dockerd-v0.3.11-1-debian   a60dd8dae2ad   2 months ago    435MB
gvenzl/oracle-xe                          latest                                                                        0ae0009858dd   2 months ago    2.91GB
postgres                                  latest                                                                        f0dfc903a663   2 months ago    434MB
mongo                                     latest                                                                        72576a3db032   3 months ago    855MB
registry.k8s.io/kube-apiserver            v1.30.5                                                                       e9adc5c075a8   3 months ago    117MB
registry.k8s.io/kube-scheduler            v1.30.5                                                                       25903461e65c   3 months ago    62MB
registry.k8s.io/kube-controller-manager   v1.30.5                                                                       38406042cf08   3 months ago    111MB
registry.k8s.io/kube-proxy                v1.30.5                                                                       71161e05b9bb   3 months ago    84.7MB
gcr.io/k8s-minikube/kicbase               v0.0.45                                                                       aeed0e1d4642   3 months ago    1.28GB
openzipkin/zipkin                         latest                                                                        88de2581a6c0   4 months ago    183MB
registry.k8s.io/coredns/coredns           v1.11.3                                                                       c69fa2e9cbf5   4 months ago    61.8MB
redis                                     latest                                                                        7e49ed81b42b   4 months ago    117MB
registry.k8s.io/etcd                      3.5.15-0                                                                      2e96e5913fc0   5 months ago    148MB
mysql                                     latest                                                                        c757d623b190   5 months ago    586MB
dixtdf/image-tools                        1.0.2                                                                         702b0e760211   9 months ago    13.1MB
registry.k8s.io/etcd                      3.5.12-0                                                                      3861cfcd7c04   10 months ago   149MB
busybox                                   latest                                                                        6fd955f66c23   19 months ago   4.26MB
docker/desktop-vpnkit-controller          dc331cb22850be0cdd97c84a9cfecaf44a1afb6e                                      556098075b3d   19 months ago   36.2MB
registry.k8s.io/pause                     3.9                                                                           e6f181688397   2 years ago     744kB
rabbitmq                                  3.11.0                                                                        bf56ebb3b006   2 years ago     229MB
docker/desktop-storage-provisioner        v2.0                                                                          99f89471f470   3 years ago     41.9MB

C:\Users\ashfa>minikube start
😄  minikube v1.34.0 on Microsoft Windows 11 Home Single Language 10.0.26100.2605 Build 26100.2605
✨  Using the docker driver based on existing profile
👍  Starting "minikube" primary control-plane node in "minikube" cluster
🚜  Pulling base image v0.0.45 ...
🏃  Updating the running docker "minikube" container ...
❗  Failing to connect to https://registry.k8s.io/ from inside the minikube container
💡  To pull new external images, you may need to configure a proxy: https://minikube.sigs.k8s.io/docs/reference/networking/proxy/
🐳  Preparing Kubernetes v1.31.0 on Docker 27.2.0 ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
    ▪ Using image docker.io/kubernetesui/dashboard:v2.7.0
    ▪ Using image docker.io/kubernetesui/metrics-scraper:v1.0.8
💡  Some dashboard features require the metrics-server addon. To enable all features please run:

        minikube addons enable metrics-server

🌟  Enabled addons: default-storageclass, storage-provisioner, dashboard
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

C:\Users\ashfa>minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured


C:\Users\ashfa>minikube docker-env
SET DOCKER_TLS_VERIFY=1
SET DOCKER_HOST=tcp://127.0.0.1:60442
SET DOCKER_CERT_PATH=C:\Users\ashfa\.minikube\certs
SET MINIKUBE_ACTIVE_DOCKERD=minikube
REM To point your shell to minikube's docker-daemon, run:
REM @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

C:\Users\ashfa>@FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

C:\Users\ashfa>docker images
REPOSITORY                                TAG        IMAGE ID       CREATED         SIZE
reactapp                                  latest     d11f5f02b654   4 hours ago     205MB
darksharkash/sb3j21crud-k8s               latest     60971bf44089   23 hours ago    263MB
darksharkash/sb3crud-k8s                  latest     dc6e8646222b   26 hours ago    238MB
darksharkash/simplerestapisb-k8s          latest     af010efbe212   3 days ago      204MB
mysql                                     latest     56a8c14e1404   2 months ago    603MB
registry.k8s.io/kube-scheduler            v1.31.0    1766f54c897f   4 months ago    67.4MB
registry.k8s.io/kube-apiserver            v1.31.0    604f5db92eaa   4 months ago    94.2MB
registry.k8s.io/kube-controller-manager   v1.31.0    045733566833   4 months ago    88.4MB
registry.k8s.io/kube-proxy                v1.31.0    ad83b2ca7b09   4 months ago    91.5MB
registry.k8s.io/etcd                      3.5.15-0   2e96e5913fc0   5 months ago    148MB
registry.k8s.io/pause                     3.10       873ed7510279   7 months ago    736kB
mysql                                     8.0.36     f5f171121fa3   9 months ago    603MB
mysql                                     5.7        5107333e08a8   12 months ago   501MB
registry.k8s.io/coredns/coredns           v1.11.1    cbb01a7bd410   16 months ago   59.8MB
kubernetesui/dashboard                    <none>     07655ddf2eeb   2 years ago     246MB
kubernetesui/metrics-scraper              <none>     115053965e86   2 years ago     43.8MB
gcr.io/k8s-minikube/storage-provisioner   v5         6e38f40d628d   3 years ago     31.5MB

C:\Users\ashfa>
```

Here’s a detailed observation of the changes in Docker images pre- and post-starting Minikube, along with the reasoning behind it and the steps needed to make your images available to Minikube.

---

### **Pre-Minukube Start: Initial Observation**
Before starting Minikube, you ran the `docker images` command and observed the following list of images:

- **Notable Images**:
  - `darksharkash/vite-react-app:v1` (created 5 hours ago)
  - `darksharkash/sb3j21crud-k8s:latest` (created 23 hours ago)
  - Other application-specific and general-purpose images (`mysql`, `mongo`, `postgres`, etc.).

This shows the images available in the **host’s Docker daemon**. These images were built, pulled, or created outside of Minikube.

---

### **Post-Minukube Start: Changes in Docker Images**
After starting Minikube and switching to its Docker environment using the command:
```cmd
@FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i
```
When you ran `docker images` again, the list of images changed:

- **Notable Changes**:
  - A new image, `reactapp:latest`, appeared (likely built in Minikube).
  - Only a subset of the previous images, such as `darksharkash/sb3j21crud-k8s:latest`, remained.
  - Several Kubernetes-related images (`kube-scheduler`, `kube-apiserver`, etc.) appeared.

This happens because you switched from the **host Docker daemon** to the **Minikube Docker daemon**. Minikube runs its own Docker environment, and by configuring your shell with `minikube docker-env`, all subsequent Docker commands interact with Minikube's Docker daemon instead of the host's.

---

### **Why the Change Happened**
1. **Isolated Docker Environments:**
   - Your host machine's Docker daemon and Minikube's Docker daemon are isolated. Images available in one are not visible or accessible in the other unless explicitly shared or transferred.
   - By default, Minikube uses its own Docker daemon to maintain a consistent and isolated environment for Kubernetes workloads.

2. **Minikube-Specific Images:**
   - Minikube pulls Kubernetes-related images into its environment during initialization (`kube-scheduler`, `kube-apiserver`, etc.).
   - Any images built or pulled post-activating the Minikube Docker daemon (like `reactapp:latest`) will only exist in Minikube's Docker environment.

3. **Images Not Automatically Available:**
   - Images you build or pull on the host (e.g., `darksharkash/vite-react-app:v1`) do not automatically propagate to Minikube. This is why you didn’t see certain images post-switching to Minikube’s environment.

---

### **To Use Custom Images in Minikube**
If you want your custom images (e.g., `darksharkash/vite-react-app:v1`) to be accessible in Minikube for your deployments, you have two options:

#### **Option 1: Build or Pull Images Directly in Minikube**
After starting Minikube, activate its Docker environment:
```cmd
@FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i
```
Then, build your image directly in Minikube’s environment:
```cmd
docker build -t darksharkash/vite-react-app:v1 .
```

#### **Option 2: Transfer Host Images to Minikube**
If the image is already built on your host, you can transfer it to Minikube using:
```cmd
minikube image load darksharkash/vite-react-app:v1
```
This command copies the image from the host Docker daemon to Minikube's Docker daemon, making it available for Kubernetes deployments.

---

### **Deployment Object Consideration**
When you define a Kubernetes Deployment object, you specify the image in the `spec.template.spec.containers.image` field. For Minikube to recognize and pull the image:

1. The image must exist in Minikube’s Docker environment.
2. Alternatively, the image must be available on an external registry (e.g., Docker Hub or a private registry) and accessible from Minikube.

If you are not using an external registry, you need to either build the image directly in Minikube or transfer it using the steps above.

---

### **Conclusion**
- **Why the images list changed:** Switching to Minikube's Docker daemon isolated the view to images available only within Minikube's environment.
- **To provide images for Minikube deployments:** You must either:
  - Build or pull the image inside Minikube after starting it.
  - Transfer the image using `minikube image load`.

By following these steps, we can ensure our images are accessible for our Kubernetes workloads. 