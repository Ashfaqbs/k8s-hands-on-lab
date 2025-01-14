

### 1. **Configuration** (Class)
   - **Package:** `io.kubernetes.client.util`
   - **Use Case:** The `Configuration` class is responsible for setting up and configuring the Kubernetes client. It enables you to load the configuration from a kubeconfig file (typically located at `~/.kube/config`) or use in-cluster configuration when running your application inside a Kubernetes cluster. This class also manages the default `ApiClient`.
   - **Example:**
     ```java
     Configuration.setDefaultApiClient(Config.defaultClient());
     ```
   - **When to Use:** Use this class when you need to configure the Kubernetes client, either by loading the default kubeconfig or using in-cluster configuration. This is typically done during the initialization phase of your Spring Boot application.

---

### 2. **ApiClient** (Class)
   - **Package:** `io.kubernetes.client.openapi`
   - **Use Case:** `ApiClient` is the core class responsible for handling API requests to the Kubernetes cluster. It manages authentication, request/response processing, timeouts, retries, and other HTTP connection details.
   - **Example:**
     ```java
     ApiClient client = Config.defaultClient();
     Configuration.setDefaultApiClient(client);
     ```
   - **When to Use:** You typically don’t need to directly interact with this class unless you need to customize the client (e.g., for advanced configurations like handling timeouts, authentication, or SSL settings).

---

### 3. **KubernetesClient** (Interface)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** The `KubernetesClient` interface provides a high-level abstraction for interacting with the Kubernetes API. It includes methods for working with various Kubernetes resources like Pods, Deployments, Services, etc.
   - **Example:**
     ```java
     KubernetesClient client = new DefaultKubernetesClient();
     client.pods().inNamespace("default").list();
     ```

   - **When to Use:** Use `KubernetesClient` when you need to interact with Kubernetes resources programmatically (e.g., for creating, reading, updating, or deleting Pods, Services, Deployments, etc.).

---

### 4. **DefaultKubernetesClient** (Class)
   - **Package:** `io.kubernetes.client`
   - **Use Case:** This is the default implementation of the `KubernetesClient` interface. It is used to interact with the Kubernetes API server. It supports all CRUD (Create, Read, Update, Delete) operations for managing Kubernetes resources.
   - **Example:**
     ```java
     DefaultKubernetesClient client = new DefaultKubernetesClient();
     client.services().inNamespace("default").createOrReplace(service);
     ```
   - **When to Use:** Use `DefaultKubernetesClient` to interact with Kubernetes resources if you need a simple, high-level interface for Kubernetes resource management.

---

### 5. **CoreV1Api** (Class)
   - **Package:** `io.kubernetes.client.openapi.apis`
   - **Use Case:** `CoreV1Api` is used to interact with the core Kubernetes resources like Pods, Services, ConfigMaps, and PersistentVolumeClaims. It provides methods for listing, creating, updating, and deleting these resources.
   - **Example:**
     ```java
     CoreV1Api api = new CoreV1Api();
     V1PodList podList = api.listNamespacedPod("default", null, null, null, null, null, null, null, null, null);
     ```
   - **When to Use:** Use `CoreV1Api` when managing core Kubernetes resources (e.g., Pods, Services, ConfigMaps) in your Spring Boot application.

---

### 6. **AppsV1Api** (Class)
   - **Package:** `io.kubernetes.client.openapi.apis`
   - **Use Case:** `AppsV1Api` provides methods to manage higher-level Kubernetes resources like Deployments, StatefulSets, and ReplicaSets. These resources are used to manage and scale application workloads.
   - **Example:**
     ```java
     AppsV1Api api = new AppsV1Api();
     V1DeploymentList deploymentList = api.listNamespacedDeployment("default", null, null, null, null, null, null, null, null, null);
     ```
   - **When to Use:** Use `AppsV1Api` when working with workloads like Deployments, StatefulSets, or ReplicaSets.

---

### 7. **BatchV1Api** (Class)
   - **Package:** `io.kubernetes.client.openapi.apis`
   - **Use Case:** This class is used for interacting with batch resources such as Jobs and CronJobs in Kubernetes. These resources allow you to run batch or scheduled tasks in the cluster.
   - **Example:**
     ```java
     BatchV1Api api = new BatchV1Api();
     V1JobList jobList = api.listNamespacedJob("default", null, null, null, null, null, null, null, null, null);
     ```
   - **When to Use:** Use `BatchV1Api` when managing batch workloads such as Jobs and CronJobs in your Kubernetes cluster.

---

### 8. **NetworkingV1Api** (Class)
   - **Package:** `io.kubernetes.client.openapi.apis`
   - **Use Case:** `NetworkingV1Api` is used to manage networking resources in Kubernetes, such as NetworkPolicies and Ingress resources. These resources control traffic routing and security policies for network communication.
   - **Example:**
     ```java
     NetworkingV1Api api = new NetworkingV1Api();
     V1IngressList ingressList = api.listNamespacedIngress("default", null, null, null, null, null, null, null, null, null);
     ```
   - **When to Use:** Use `NetworkingV1Api` for networking-related resources like Ingress and NetworkPolicies.

---

### 9. **CustomObjectsApi** (Class)
   - **Package:** `io.kubernetes.client.openapi.apis`
   - **Use Case:** This class is used to interact with Custom Resource Definitions (CRDs) in Kubernetes. CRDs allow you to define your own custom resources in Kubernetes.
   - **Example:**
     ```java
     CustomObjectsApi api = new CustomObjectsApi();
     Object customResource = api.getNamespacedCustomObject("my-group", "v1", "default", "my-custom-resource", "my-resource-name");
     ```
   - **When to Use:** Use `CustomObjectsApi` when dealing with custom resources that you’ve defined in your Kubernetes cluster (e.g., CRDs).

---

### 10. **V1Pod** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1Pod` represents a Kubernetes Pod, which is the smallest deployable unit in Kubernetes. A Pod can contain one or more containers and is typically used to run a single application.
   - **Example:**
     ```java
     V1Pod pod = new V1Pod();
     pod.setMetadata(new V1ObjectMeta().name("my-pod"));
     pod.setSpec(new V1PodSpec().addContainersItem(new V1Container().name("nginx").image("nginx")));
     ```
   - **When to Use:** Use `V1Pod` when creating, modifying, or retrieving Pod resources in Kubernetes.

---

### 11. **V1Deployment** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1Deployment` represents a Kubernetes Deployment, which is used for declarative updates to Pods and ReplicaSets, and helps manage the scaling and rollout of application updates.
   - **Example:**
     ```java
     V1Deployment deployment = new V1Deployment();
     deployment.setMetadata(new V1ObjectMeta().name("my-deployment"));
     deployment.setSpec(new V1DeploymentSpec()
         .replicas(3)
         .template(new V1PodTemplateSpec()
             .spec(new V1PodSpec().addContainersItem(new V1Container().name("nginx").image("nginx")))));
     ```
   - **When to Use:** Use `V1Deployment` for managing Deployment resources in Kubernetes, especially for scaling and updating application workloads.

---

### 12. **V1Service** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1Service` represents a Kubernetes Service, which is used to expose a set of Pods to network traffic. Services provide stable networking and load balancing for Pods.
   - **Example:**
     ```java
     V1Service service = new V1Service();
     service.setMetadata(new V1ObjectMeta().name("my-service"));
     service.setSpec(new V1ServiceSpec().type("ClusterIP")
         .ports(Arrays.asList(new V1ServicePort().port(8080)))
         .selector(Collections.singletonMap("app", "nginx")));
     ```
   - **When to Use:** Use `V1Service` when creating or managing services to expose Pods for networking in your Kubernetes cluster.

---

### 13. **V1ObjectMeta** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1ObjectMeta` defines the metadata for Kubernetes resources. It includes fields like `name`, `namespace`, `labels`, and `annotations`, which are common to almost all Kubernetes objects.
   - **Example:**
    

 ```java
     V1ObjectMeta metadata = new V1ObjectMeta();
     metadata.setName("my-resource");
     metadata.setLabels(Collections.singletonMap("env", "dev"));
     ```
   - **When to Use:** `V1ObjectMeta` is used as part of every Kubernetes resource, including Pods, Deployments, and Services, to specify the metadata such as name, namespace, and labels.

---

### 14. **V1PodSpec** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1PodSpec` defines the specification for a Kubernetes Pod, including the list of containers, volumes, networking, and resource requirements.
   - **Example:**
     ```java
     V1PodSpec podSpec = new V1PodSpec();
     podSpec.addContainersItem(new V1Container().name("nginx").image("nginx"));
     ```
   - **When to Use:** Use `V1PodSpec` when defining the internal structure of a Pod, including containers, volume mounts, and resource requests/limits.

---

### 15. **V1Container** (Class)
   - **Package:** `io.kubernetes.client.openapi.models`
   - **Use Case:** `V1Container` represents a single container within a Pod. It contains configuration such as the container image, ports, environment variables, and volume mounts.
   - **Example:**
     ```java
     V1Container container = new V1Container();
     container.setName("nginx-container");
     container.setImage("nginx");
     ```
   - **When to Use:** Use `V1Container` when defining or modifying the containers inside a Pod, specifying configurations like image, ports, and environment variables.

---

### Example: Integrating Kubernetes Client in Spring Boot

Here’s an example of how to integrate some of these classes into a Spring Boot service that fetches information about Kubernetes Pods:

```java
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.ApiClient;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    private final CoreV1Api coreV1Api;

    public KubernetesService() throws Exception {
        ApiClient client = Config.defaultClient();
        this.coreV1Api = new CoreV1Api(client);
    }

    public V1PodList getPods(String namespace) throws Exception {
        return coreV1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
    }

    public V1Pod createPod(String namespace) throws Exception {
        V1Pod pod = new V1Pod();
        pod.setMetadata(new V1ObjectMeta().name("my-pod").namespace(namespace));
        pod.setSpec(new V1PodSpec().addContainersItem(new V1Container().name("nginx").image("nginx")));
        return coreV1Api.createNamespacedPod(namespace, pod, null, null, null);
    }
}
```

---

### When to Use Each Class:
- **`Configuration`** and **`ApiClient`**: Use these when setting up your connection to the Kubernetes cluster (typically at the application startup).
- **`V1Pod`, `V1Deployment`, `V1Service`**: Use these classes to interact with the respective Kubernetes resources (create, read, update, delete).
- **`V1ObjectMeta`**: Used for configuring metadata such as name, namespace, and labels for Kubernetes objects.
- **`V1PodSpec`, `V1Container`**: Used when creating or modifying the configuration of Pods and their containers.

These are the fundamental classes you will use to interact with Kubernetes resources programmatically in a Spring Boot application, enabling you to automate and manage Kubernetes workloads effectively.