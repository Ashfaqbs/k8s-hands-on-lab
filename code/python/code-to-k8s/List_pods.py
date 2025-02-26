from kubernetes import client, config

# Load kubeconfig
config.load_kube_config()

# Create an instance of the API class
v1 = client.CoreV1Api()

# List pods in the 'dev' namespace
namespace = "dev"
pods = v1.list_namespaced_pod(namespace)

print("Listing pods in the 'dev' namespace:")
for pod in pods.items:
    print(f"{pod.metadata.name}")


# Output:
# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/List_pods.py
# Listing pods in the 'dev' namespace:
# example-pod