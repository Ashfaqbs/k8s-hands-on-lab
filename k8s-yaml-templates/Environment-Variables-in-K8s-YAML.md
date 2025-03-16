### Defining Environment Variables in Kubernetes YAML

In Kubernetes, environment variables can be defined at different levels depending on the scope of the variables and how they should be applied. Below are the common methods for defining environment variables in a Kubernetes YAML file.

---

#### **1. Defining Environment Variables at the Container Level**

Environment variables are defined specifically for a container in a pod or deployment. This method is the most common and ensures the environment variables are only available to the container in which they are defined.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      containers:
        - name: springboot-container
          image: your-docker-image:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "production"
            - name: DATABASE_URL
              valueFrom:
                secretKeyRef:
                  name: my-database-secret
                  key: db-url
            - name: API_KEY
              value: "your-api-key"
```

- **Scope**: These environment variables will only be available to the specified container.
- **Use Case**: Define environment variables specific to the configuration of a single container, like service URLs, API keys, etc.

---

#### **2. Using a ConfigMap for Shared Environment Variables**

If there is a need to share environment variables across multiple containers or manage them separately from the deployment file, a ConfigMap can be used. A ConfigMap allows the configuration of environment variables outside of the container definition, making it more manageable and reusable.

##### Step 1: Create a ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  SPRING_PROFILES_ACTIVE: "production"
  API_KEY: "your-api-key"
```

##### Step 2: Reference the ConfigMap in the Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      containers:
        - name: springboot-container
          image: your-docker-image:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: app-config
```

- **Scope**: Environment variables defined in the ConfigMap will be available to all containers referencing the ConfigMap.
- **Use Case**: Share common configuration values across multiple containers in a pod, or manage configuration separately from the YAML.

---

#### **3. Using a Secret for Sensitive Data**

Sensitive data, such as passwords or API keys, can be securely stored in Kubernetes Secrets. These secrets can then be used as environment variables in the container definition.

##### Step 1: Create a Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  DATABASE_PASSWORD: cGFzc3dvcmQxMjM=  # Base64 encoded 'password123'
```

##### Step 2: Reference the Secret in the Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      containers:
        - name: springboot-container
          image: your-docker-image:latest
          ports:
            - containerPort: 8080
          env:
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: DATABASE_PASSWORD
```

- **Scope**: The secret is available only to the container that references it.
- **Use Case**: Manage sensitive data like passwords, certificates, and tokens securely.

---

#### **4. Defining Environment Variables at the Pod Level (Available to All Containers)**

Environment variables can be defined at the pod level, making them available to all containers within that pod. This is useful when multiple containers need access to the same set of environment variables.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: multi-container-pod
spec:
  containers:
    - name: container1
      image: image1
    - name: container2
      image: image2
  env:
    - name: GLOBAL_VAR
      value: "common-value"
```

- **Scope**: These environment variables are available to all containers within the pod.
- **Use Case**: Share common environment variables across containers in the same pod, such as a global configuration setting.

---

### Summary of Methods to Define Environment Variables:

1. **Container-Level**: Define environment variables specifically for one container.
2. **ConfigMap**: Share environment variables across containers or manage them externally.
3. **Secret**: Store and manage sensitive data securely as environment variables.
4. **Pod-Level**: Define environment variables available to all containers in a pod.

By choosing the appropriate method for defining environment variables, Kubernetes configurations can be made more modular, secure, and easier to manage across different environments.
