from kubernetes import client, config, watch

# Load kubeconfig
config.load_kube_config()

# Create an instance of the API class
v1 = client.CoreV1Api()

# Define the namespace
namespace = "dev"

# Watch pods in the 'dev' namespace
w = watch.Watch()
for event in w.stream(v1.list_namespaced_pod, namespace):
    print(f"Event: {event['type']} {event['object'].metadata.name}")


# Output:

# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/create-pod.py
# Pod created in the 'dev' namespace.

# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/Delete-pod.py
# Pod 'example-pod' deleted from the 'dev' namespace.

# C:\tmp\sb-k8s-client>C:/Users/ashfa/AppData/Local/Programs/Python/Python312/python.exe c:/tmp/sb-k8s-client/python/Watch-Pods.py
# Event: ADDED example-pod
# Event: MODIFIED example-pod
# Event: MODIFIED example-pod
# Event: MODIFIED example-pod
# Event: DELETED example-pod