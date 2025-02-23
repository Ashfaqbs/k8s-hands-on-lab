## 1. Zookeeper

### In Docker Compose

```yaml
zookeeper:
  image: wurstmeister/zookeeper:latest
  ports:
    - "2181:2181"
```

### In Kubernetes YAML

**Deployment (`zookeeper-deployment.yaml`):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zookeeper
  template:
    metadata:
      labels:
        app: zookeeper
    spec:
      containers:
      - name: zookeeper
        image: wurstmeister/zookeeper:latest
        ports:
        - containerPort: 2181
```

**Service (`zookeeper-service.yaml`):**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: zookeeper
spec:
  ports:
  - port: 2181
    targetPort: 2181
  selector:
    app: zookeeper
```

**Mapping Details:**

- **Image:** The Compose file specifies the Zookeeper image (`wurstmeister/zookeeper:latest`), which is directly used in the Deployment.
- **Ports:** The Compose file maps port 2181 on the host to 2181 in the container. In Kubernetes, this is achieved by:
  - Defining `containerPort: 2181` in the Deployment.
  - Creating a Service that exposes port 2181 (mapping the service port to the same targetPort).

---

## 2. Kafka

### In Docker Compose

```yaml
kafka:
  image: wurstmeister/kafka:latest
  ports:
    - "9092:9092"
  expose:
    - "9093"
  environment:
    KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093,OUTSIDE://localhost:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
    KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
    KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_CREATE_TOPICS: "my-topic:1:1"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
```

### In Kubernetes YAML

**Deployment (`kafka-deployment.yaml`):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: wurstmeister/kafka:latest
        ports:
        - containerPort: 9093
        env:
          # Override the auto-injected PORT variable from Kubernetes with a proper numeric value.
          - name: PORT
            value: "9093"
          - name: KAFKA_ADVERTISED_LISTENERS
            value: "INSIDE://kafka:9093,OUTSIDE://localhost:9092"
          - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
            value: "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT"
          - name: KAFKA_LISTENERS
            value: "INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092"
          - name: KAFKA_INTER_BROKER_LISTENER_NAME
            value: "INSIDE"
          - name: KAFKA_ZOOKEEPER_CONNECT
            value: "zookeeper:2181"
          - name: KAFKA_CREATE_TOPICS
            value: "my-topic:1:1"
```

**Service (`kafka-service.yaml`):**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: kafka
spec:
  ports:
  - port: 9093
    targetPort: 9093
  selector:
    app: kafka
```

**Mapping Details:**

- **Image:** Uses `wurstmeister/kafka:latest` directly as in the Compose file.
- **Ports:**  
  - The Compose file maps host port 9092 to container port 9092 and exposes 9093. In Kubernetes, internal communication between pods uses the container’s internal port (9093 in this case) since services are discovered by name.
  - We define `containerPort: 9093` in the Deployment and expose port 9093 via the Service.
- **Environment Variables:**  
  All environment variables set in Compose for Kafka are carried over to the Deployment’s `env` section.
- **Volumes:**  
  The volume mounting for Docker socket was used in Docker Compose for dynamic topic creation. In Kubernetes, this is generally omitted unless specifically required. If needed, we would add a volume and volumeMount to the container spec.

- **Additional Fix:**  
  The Kubernetes environment sometimes injects a variable named `PORT` which can conflict. We explicitly set `PORT` to `"9093"` so that Kafka gets the numeric port it expects.

---

## 3. Spring Boot Application

### In Docker Compose

```yaml
springboot-app:
  image: darksharkash/springboot-kafka-app:latest
  environment:
    - KAFKA_BOOTSTRAP_SERVERS=kafka:9093
  ports:
    - "8080:8080"
  depends_on:
    - kafka
```

### In Kubernetes YAML

**Deployment (`springboot-deployment.yaml`):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-kafka-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-kafka-app
  template:
    metadata:
      labels:
        app: springboot-kafka-app
    spec:
      containers:
      - name: springboot-kafka-app
        image: darksharkash/springboot-kafka-app:latest
        ports:
        - containerPort: 8080
        env:
          - name: KAFKA_BOOTSTRAP_SERVERS
            value: "kafka:9093"
```

**Service (`springboot-service.yaml`):**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: springboot-kafka-app
spec:
  type: NodePort
  selector:
    app: springboot-kafka-app
  ports:
  - port: 80
    targetPort: 8080
```

**Mapping Details:**

- **Image:** The same image `darksharkash/springboot-kafka-app:latest` is used.
- **Environment Variable:**  
  The Compose file sets `KAFKA_BOOTSTRAP_SERVERS` to `kafka:9093` which is directly transferred to the Deployment’s environment variable.
- **Ports:**  
  In Compose, port mapping `"8080:8080"` means the container’s 8080 is exposed on the host. In Kubernetes, we:
  - Specify `containerPort: 8080` in the Deployment.
  - Create a Service that maps an external port (here, NodePort type with port 80) to the container port 8080.
- **Dependency:**  
  While Docker Compose uses `depends_on` to ensure Kafka starts first, Kubernetes does not have a direct equivalent in Deployments. Instead, we must rely on proper readiness checks or manage startup ordering through other means.

---

## Summary of the Translation Process

- **Identify Services:**  
  we had three services in Compose: Zookeeper, Kafka, and the Spring Boot app. Each service was translated into its own Deployment and Service.
  
- **Images & Ports:**  
  The images specified in Compose were directly used in the Kubernetes container specs. Port mappings in Compose are translated into `containerPort` entries in Deployments and into Service definitions for exposing ports.
  
- **Environment Variables:**  
  Environment configurations provided in the Compose file were transferred to the `env` section of the respective container specs in the Deployment files.
  
- **Additional Adjustments:**  
  - The Docker Compose `depends_on` feature isn’t directly available in Kubernetes, so inter-service dependencies are managed differently (via readiness probes or manual ordering).
  - The Kubernetes environment may inject extra variables (like `PORT`), so we explicitly override them to avoid conflicts (as seen in the Kafka deployment).

This step-by-step translation ensures that our  containerized application behaves similarly in Kubernetes as it did in Docker Compose.

---