C:\Users\ashfa>minikube status
minikube
type: Control Plane
host: Stopped
kubelet: Stopped
apiserver: Stopped
kubeconfig: Stopped


C:\Users\ashfa>minikube reset
Error: unknown command "reset" for "minikube"
Run 'minikube --help' for usage.

C:\Users\ashfa>minikube help
minikube provisions and manages local Kubernetes clusters optimized for development workflows.

Basic Commands:
  start            Starts a local Kubernetes cluster
  status           Gets the status of a local Kubernetes cluster
  stop             Stops a running local Kubernetes cluster
  delete           Deletes a local Kubernetes cluster
  dashboard        Access the Kubernetes dashboard running within the minikube cluster
  pause            pause Kubernetes
  unpause          unpause Kubernetes

Images Commands:
  docker-env       Provides instructions to point your terminal's docker-cli to the Docker Engine inside minikube.
(Useful for building docker images directly inside minikube)
  podman-env       Configure environment to use minikube's Podman service
  cache            Manage cache for images
  image            Manage images

Configuration and Management Commands:
  addons           Enable or disable a minikube addon
  config           Modify persistent configuration values
  profile          Get or list the current profiles (clusters)
  update-context   Update kubeconfig in case of an IP or port change

Networking and Connectivity Commands:
  service          Returns a URL to connect to a service
  tunnel           Connect to LoadBalancer services

Advanced Commands:
  mount            Mounts the specified directory into minikube
  ssh              Log into the minikube environment (for debugging)
  kubectl          Run a kubectl binary matching the cluster version
  node             Add, remove, or list additional nodes
  cp               Copy the specified file into minikube

Troubleshooting Commands:
  ssh-key          Retrieve the ssh identity key path of the specified node
  ssh-host         Retrieve the ssh host key of the specified node
  ip               Retrieves the IP address of the specified node
  logs             Returns logs to debug a local Kubernetes cluster
  update-check     Print current and latest version number
  version          Print the version of minikube
  options          Show a list of global command-line options (applies to all commands).

Other Commands:
  completion       Generate command completion for a shell
  license          Outputs the licenses of dependencies to a directory

Use "minikube <command> --help" for more information about a given command.

C:\Users\ashfa>minikube delete
🔥  Deleting "minikube" in docker ...
🔥  Deleting container "minikube" ...
🔥  Removing C:\Users\ashfa\.minikube\machines\minikube ...
💀  Removed all traces of the "minikube" cluster.

C:\Users\ashfa>minikube start
😄  minikube v1.34.0 on Microsoft Windows 11 Home Single Language 10.0.26100.3476 Build 26100.3476
🎉  minikube 1.35.0 is available! Download it: https://github.com/kubernetes/minikube/releases/tag/v1.35.0
💡  To disable this notice, run: 'minikube config set WantUpdateNotification false'

✨  Automatically selected the docker driver. Other choices: virtualbox, ssh
📌  Using Docker Desktop driver with root privileges
👍  Starting "minikube" primary control-plane node in "minikube" cluster
🚜  Pulling base image v0.0.45 ...
🔥  Creating docker container (CPUs=2, Memory=4000MB) ...
❗  Failing to connect to https://registry.k8s.io/ from inside the minikube container
💡  To pull new external images, we may need to configure a proxy: https://minikube.sigs.k8s.io/docs/reference/networking/proxy/
🐳  Preparing Kubernetes v1.31.0 on Docker 27.2.0 ...
    ▪ Generating certificates and keys ...
    ▪ Booting up control plane ...
    ▪ Configuring RBAC rules ...
🔗  Configuring bridge CNI (Container Networking Interface) ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: storage-provisioner, default-storageclass
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

C:\Users\ashfa>kubectl config use-context minikube
Switched to context "minikube".

C:\Users\ashfa>minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured


C:\Users\ashfa>minikube docker-ev
Error: unknown command "docker-ev" for "minikube"

Did we mean this?
        docker-env

Run 'minikube --help' for usage.

C:\Users\ashfa>minikube docker-env
SET DOCKER_TLS_VERIFY=1
SET DOCKER_HOST=tcp://127.0.0.1:51230
SET DOCKER_CERT_PATH=C:\Users\ashfa\.minikube\certs
SET MINIKUBE_ACTIVE_DOCKERD=minikube
REM To point your shell to minikube's docker-daemon, run:
REM @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

C:\Users\ashfa> @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i

C:\Users\ashfa>minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
docker-env: in-use


C:\Users\ashfa>




Note we have K8s  supported from docker desktop as well 

Minikube runs its own isolated Kubernetes cluster inside a VM or container runtime (Docker, Hyper-V, WSL2, etc.), so **disabling Kubernetes in Docker Desktop** won't affect Minikube. Since Minikube comes with its own control plane, it doesn't rely on Docker Desktop's Kubernetes.  
Docker has its own K8s single node cluster called Kubeadm which we can enable from settings

### ✅ we can work fine with Minikube and `kubectl`  
However, just keep these points in mind:  
1. **Ensure Minikube is Running**  
   - Run `minikube status` to check if the cluster is active.  
   - If it’s not running, start it with `minikube start`.  

2. **Check our kubectl Context**  
   - Run `kubectl config current-context` to ensure it points to `minikube`.  
   - If not, switch it using:  
     ```sh
     kubectl config use-context minikube
     ```

3. **Docker CLI & Minikube's Docker Daemon**  
   - If we need to use Docker images inside Minikube, run:  
     ```sh
     eval $(minikube docker-env) mac or  @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i windows
     ```
   - This allows we to build Docker images inside Minikube without pushing them to an external registry.  

4. **Networking Considerations**  
   - Since Minikube runs inside a VM (or container), its networking may differ from Docker Desktop’s built-in Kubernetes.  
   - If using `minikube tunnel` for LoadBalancer services, ensure it’s running in a separate terminal.  

### No Major Issues, But...  
If we previously used Docker Desktop’s Kubernetes, we might have some stale configurations. If we ever face conflicts, resetting your `kubectl` config (`rm ~/.kube/config` on Linux/macOS or `del %USERPROFILE%\.kube\config` on Windows) and restarting Minikube should help.  
