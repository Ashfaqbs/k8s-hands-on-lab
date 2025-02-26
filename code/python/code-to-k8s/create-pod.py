# pip install kubernetes
from kubernetes import client, config

# Load kubeconfig
config.load_kube_config()

# Create an instance of the API class
v1 = client.CoreV1Api()

# Define the pod specification
pod = client.V1Pod(
    api_version="v1",
    kind="Pod",
    metadata=client.V1ObjectMeta(name="example-pod"),
    spec=client.V1PodSpec(
        containers=[
            client.V1Container(
                name="example-container",
                image="nginx",
            )
        ]
    )
)

# Create the pod in the 'dev' namespace
namespace = "dev"
v1.create_namespaced_pod(namespace, pod)

print("Pod created in the 'dev' namespace.")

# Output:
# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/create-pod.py
# Pod created in the 'dev' namespace.


# C:\Users\ashfa>kubectl get pods -n dev
# NAME          READY   STATUS              RESTARTS   AGE
# example-pod   0/1     ContainerCreating   0          30s