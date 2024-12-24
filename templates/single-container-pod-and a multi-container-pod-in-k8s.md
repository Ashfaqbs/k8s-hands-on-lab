To create a single container pod and a multi-container pod in Kubernetes, you can use YAML configuration files. Here’s how to set up both scenarios:

## Creating a Single Container Pod

A single container pod can be created using a simple YAML file. Below is an example configuration for a pod running a Spring Boot application.

1. **Create a YAML file for the single container pod** (e.g., `single-container-pod.yaml`):

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: spring-boot-app
spec:
  containers:
  - name: spring-boot-app
    image: <your-docker-id>/spring-boot-app:latest
    ports:
    - containerPort: 8080
```

2. **Deploy the pod** using the following command:

```bash
kubectl apply -f single-container-pod.yaml
```

3. **Check the status of the pod**:

```bash
kubectl get pods
```

This will create a pod named `spring-boot-app` that runs your Spring Boot application in a single container.

## Creating a Multi-Container Pod

For a multi-container pod, you can define multiple containers within the same YAML file. Below is an example configuration that includes two containers: one for the Spring Boot application and another for a logging agent.

1. **Create a YAML file for the multi-container pod** (e.g., `multi-container-pod.yaml`):

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-container-app
spec:
  containers:
  - name: spring-boot-app
    image: <your-docker-id>/spring-boot-app:latest
    ports:
    - containerPort: 8080

  - name: logging-agent
    image: <your-docker-id>/logging-agent:latest
```

2. **Deploy the multi-container pod** using the following command:

```bash
kubectl apply -f multi-container-pod.yaml
```

3. **Check the status of the pod**:

```bash
kubectl get pods
```

### Key Points

- In the **single container pod**, only one instance of your application runs, making it straightforward and easy to manage.
  
- In the **multi-container pod**, both containers share the same network namespace and can communicate with each other using `localhost`. This setup is beneficial for applications that require close interaction, such as an API server and a logging service.

By following these steps, you can effectively create both single and multi-container pods in Kubernetes, allowing you to manage your applications according to your architectural needs.

Citations:
[1] https://www.civo.com/academy/kubernetes-objects/introduction-to-kubernetes-pods
[2] https://k21academy.com/docker-kubernetes/multi-container-pods/
[3] https://jamesdefabia.github.io/docs/user-guide/pods/single-container/
[4] https://www.geeksforgeeks.org/kubernetes-creating-multiple-container-in-a-pod/
[5] https://www.fairwinds.com/blog/how-to-create-view-and-destroy-a-pod-in-kubernetes
[6] https://www.mirantis.com/blog/kubernetes-pod-vs-container-multi-container-pods-and-container-communication/
[7] https://kubernetes.io/docs/concepts/workloads/pods/
[8] https://k21academy.com/docker-kubernetes/kubernetes-pods-for-beginners/
[9] https://stackoverflow.com/questions/41668750/single-container-pod-yaml
[10] https://spacelift.io/blog/kubernetes-sidecar-container
