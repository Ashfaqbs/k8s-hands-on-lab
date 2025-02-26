from kubernetes import client, config

# Load kubeconfig
config.load_kube_config()

# Create an instance of the API class
v1 = client.CoreV1Api()

# Define the pod name and namespace
pod_name = "example-pod"
namespace = "dev"

# Delete the pod
v1.delete_namespaced_pod(pod_name, namespace)

print(f"Pod '{pod_name}' deleted from the 'dev' namespace.")

# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/Delete-pod.py
# Pod 'example-pod' deleted from the 'dev' namespace.
# C:\Users\ashfa>kubectl get pods -n dev
# No resources found in dev namespace.