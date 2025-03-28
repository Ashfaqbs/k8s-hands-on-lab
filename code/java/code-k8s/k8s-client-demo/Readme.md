# Kubernetes Java Client: Important Classes & Functions
- Note function parameters and return types might be different in actual code.

## 1. **ApiClient** (io.kubernetes.client.openapi.ApiClient)
   - Handles authentication and communication with the Kubernetes API.
   - **Key Methods:**
     - `setBasePath(String path)`: Set API server URL.
     - `setApiKey(String key)`: Authenticate using an API key.
     - `execute()`: Executes API calls.

## 2. **CoreV1Api** (io.kubernetes.client.openapi.apis.CoreV1Api)
   - Manages core K8s resources like **Pods, Services, ConfigMaps, and Secrets**.
   - **Key Methods:**
     - `listNamespacedPod(String namespace)`: Get all Pods in a namespace.
     - `createNamespacedPod(String namespace, V1Pod body)`: Create a Pod.
     - `deleteNamespacedPod(String name, String namespace)`: Delete a Pod.
     - `listNamespacedService(String namespace)`: Get all Services.
     - `createNamespacedService(String namespace, V1Service body)`: Create a Service.
     - `deleteNamespacedService(String name, String namespace)`: Delete a Service.
     - `listNamespacedConfigMap(String namespace)`: Get all ConfigMaps.
     - `listNamespacedSecret(String namespace)`: Get all Secrets.

## 3. **AppsV1Api** (io.kubernetes.client.openapi.apis.AppsV1Api)
   - Manages **Deployments, DaemonSets, and StatefulSets**.
   - **Key Methods:**
     - `listNamespacedDeployment(String namespace)`: Get all Deployments.
     - `createNamespacedDeployment(String namespace, V1Deployment body)`: Create a Deployment.
     - `deleteNamespacedDeployment(String name, String namespace)`: Delete a Deployment.

## 4. **V1Pod** (io.kubernetes.client.openapi.models.V1Pod)
   - Represents a **Pod**.
   - **Key Methods:**
     - `setMetadata(V1ObjectMeta metadata)`: Set Pod name and labels.
     - `setSpec(V1PodSpec spec)`: Set Pod specifications (containers, volumes, etc.).

## 5. **V1Service** (io.kubernetes.client.openapi.models.V1Service)
   - Represents a **Service** (ClusterIP, NodePort, or LoadBalancer).
   - **Key Methods:**
     - `setMetadata(V1ObjectMeta metadata)`: Set Service name and labels.
     - `setSpec(V1ServiceSpec spec)`: Define Service type and ports.

## 6. **V1Deployment** (io.kubernetes.client.openapi.models.V1Deployment)
   - Represents a **Deployment** (ReplicaSet controller for managing Pods).
   - **Key Methods:**
     - `setMetadata(V1ObjectMeta metadata)`: Set Deployment name and labels.
     - `setSpec(V1DeploymentSpec spec)`: Define the Deployment strategy, replicas, and Pod template.

## 7. **V1ConfigMap** (io.kubernetes.client.openapi.models.V1ConfigMap)
   - Represents a **ConfigMap** for storing non-sensitive configuration data.
   - **Key Methods:**
     - `setData(Map<String, String> data)`: Store key-value pairs in the ConfigMap.

## 8. **V1Secret** (io.kubernetes.client.openapi.models.V1Secret)
   - Represents a **Secret** for storing sensitive information.
   - **Key Methods:**
     - `setData(Map<String, byte[]> data)`: Store key-value pairs securely.

## 9. **V1Event** (io.kubernetes.client.openapi.models.V1Event)
   - Represents an **event** in Kubernetes (e.g., Pod started, failed, etc.).
   - **Key Methods:**
     - `getMetadata()`: Get event metadata (name, timestamp, etc.).
     - `getMessage()`: Get event message (e.g., "Pod scheduled on Node-1").

## 10. **Logs and Events Handling**
   - **Logging Pod Output:**
     ```java
     CoreV1Api api = new CoreV1Api();
     String podLogs = api.readNamespacedPodLog("java-created-pod", "default").execute();
     System.out.println(podLogs);
     ```
   - **Retrieving Events for a Pod:**
     ```java
     EventsV1Api eventApi = new EventsV1Api();
     V1EventList events = eventApi.listNamespacedEvent("default").execute();
     for (V1Event event : events.getItems()) {
         System.out.println(event.getMessage());
     }
     ```

## 11. **Deleting Resources**
   - **Delete Pod:** `api.deleteNamespacedPod("pod-name", "default").execute();`
   - **Delete Deployment:** `api.deleteNamespacedDeployment("deployment-name", "default").execute();`
   - **Delete Service:** `api.deleteNamespacedService("service-name", "default").execute();`
   - **Delete ConfigMap:** `api.deleteNamespacedConfigMap("config-name", "default").execute();`
   - **Delete Secret:** `api.deleteNamespacedSecret("secret-name", "default").execute();`

---
### **Summary**
This document covers the most critical classes and methods in the Kubernetes Java Client. It provides a reference for working with Pods, Services, Deployments, ConfigMaps, Secrets, and Events programmatically.





The **Kubernetes Java Client** and **Kubernetes Java Client API** are closely related but serve different purposes:  

1. **Kubernetes Java Client**  
   - A Java library that provides an interface to interact with the Kubernetes API programmatically.  
   - Uses generated Java classes to make API calls to manage resources like Pods, Deployments, Services, etc.  
   - Example usage: Automating K8s operations, building controllers/operators, or monitoring clusters from Java applications.  

2. **Kubernetes Java Client API**  
   - The set of RESTful endpoints exposed by Kubernetes, which the Java client interacts with.  
   - It provides access to cluster resources through HTTP requests (e.g., `GET /api/v1/pods`).  
   - Used when making direct API calls without the Java client library (e.g., via `OkHttpClient` or `RestTemplate`).  

**When to Use?**  
- **Java Client**: When developing Java applications that need seamless Kubernetes integration with type safety and built-in methods.  
- **Java Client API (Direct Calls)**: When needing lower-level control, using different programming languages, or interacting with K8s without adding a Java dependency.