# kubernetes

### **Introduction to Kubernetes (K8s)**  

#### **What is Kubernetes?**  
Open source container Management tool automates the deployment, scaling, and management of containerized applications.

### Management means Deploying, Scaling, Loadbalancing, High availability, Self healing, Dashboard and more  ###

Kubernetes (often abbreviated as **K8s**) is an **open-source container orchestration platform**. It helps automate the deployment, scaling, and management of containerized applications. It is widely used to manage workloads in **cloud-native environments**, providing features like fault tolerance, auto-scaling, load balancing, and rolling updates.  

#### **Brief History of Kubernetes**  
1. **Origin**: Developed by Google with Golang, Kubernetes is based on Google’s experience running billions of containers daily using their internal system, **Borg**.  
2. **Launch**: Released as an open-source project in 2014.  
3. **Current Management**: Now maintained by the **Cloud Native Computing Foundation (CNCF)**.  
4. **Why the Name K8s?**: Kubernetes comes from a Greek word meaning "helmsman" or "pilot." The abbreviation "K8s" represents the 8 letters between "K" and "s."  

---

### **Why Kubernetes?**  
Kubernetes is essential for modern applications because:  
1. **Scalability**: Easily scale applications up or down based on demand.  
2. **Portability**: Works across different environments—on-premises, cloud, or hybrid.  
3. **Resilience**: Ensures applications stay up and running with features like self-healing and automated failover.  
4. **Efficiency**: Optimizes resource utilization by packing workloads efficiently onto nodes.  
5. **Automation**: Reduces manual overhead with features like automated deployment and scaling.  

---

### **When to Use Kubernetes (Scenarios)**  
1. **Microservices Architecture**: Managing hundreds of small, interconnected services.  
   - **Example**: Netflix’s recommendation engine.  
2. **Dynamic Scaling Needs**: Applications with fluctuating traffic, like e-commerce sites during sales.  
   - **Example**: Amazon scaling up services on Black Friday.  
3. **Hybrid/Multicloud Deployments**: Running workloads across multiple cloud providers.  
   - **Example**: A financial institution leveraging both AWS and on-premises data centers.  
4. **Dev/Test Environments**: Quickly spinning up environments for development teams.  
   - **Example**: A startup testing new features in isolated Kubernetes clusters.  

---

### **Features of Kubernetes (Scenario-Based)**  
1. **Automated Scaling (Horizontal Pod Autoscaler)**  
   - **Scenario**: A food delivery app handling sudden surges during dinner hours.  
   - K8s automatically adds pods to handle the increased load.  

2. **Self-Healing**  
   - **Scenario**: A database service crashes unexpectedly.  
   - K8s detects the failure and restarts the pod automatically.  

3. **Load Balancing**  
   - **Scenario**: A web app deployed in multiple regions.  
   - K8s distributes traffic evenly across available pods, ensuring no server gets overwhelmed.  

4. **Rolling Updates**  
   - **Scenario**: Updating a shopping app without downtime.  
   - K8s rolls out the new version incrementally while keeping the app live.  

5. **Resource Management**  
   - **Scenario**: Multiple teams sharing the same cluster for development.  
   - K8s allocates resources (CPU, memory) to prevent one team from monopolizing resources.  

6. **Storage Orchestration**  
   - **Scenario**: A media service requiring large volumes of persistent storage.  
   - K8s dynamically provisions storage based on the app's needs.  

---

### **Supported Platforms and Ecosystem**  
1. **Cloud Providers**:  
   - AWS (EKS), Azure (AKS), Google Cloud (GKE).  
2. **On-Premise**:  
   - Can run on bare metal or with tools like OpenShift.  
3. **Container Runtimes**:  
   - Docker, containerd, CRI-O.  
4. **Networking Plugins**:  
   - Calico, Flannel, Cilium.  
5. **Storage Plugins**:  
   - Ceph, AWS EBS, Azure Disks.  

---

### **Alternatives to Kubernetes**  
While Kubernetes is the most popular, alternatives include:  
1. **Docker Swarm**  
   - Simpler than Kubernetes but less feature-rich.  
   - Ideal for small-scale setups.  
2. **Amazon ECS**  
   - Fully managed container service on AWS.  
   - Limited to the AWS ecosystem.  
3. **Nomad** (by HashiCorp)  
   - Lightweight and easier to use than Kubernetes.  
   - Suitable for simpler orchestration needs.  
4. **Red Hat OpenShift**  
   - Enterprise-focused Kubernetes platform.  
   - Adds features like built-in CI/CD pipelines.  

Also we have Marathon, Docker Compose ......

---

### **Repo Index**

#### Getting Started
- [Install-k8s.md](./getting-started/Install-k8s.md)
- [K8s-Architecture.md](./getting-started/K8s-Architecture.md)
- [Components.md](./getting-started/Components.md)

#### Core Concepts
- [Namespaces](./concepts/namespace/Readme.md)
- [Kubeconfig](./concepts/kubeconfig/Readme.md)
- [Volumes / PV & PVC](./concepts/PVC/K8s-Volumes-Documentation.md) ([management notes](./concepts/PVC/K8s-PVC&PV-Mgmt.md))
- [Eventing](./concepts/k8s-eventing/Readme.md)
- [Networking: TCP vs UDP](./concepts/networks/tcp-vs-udp.md)
- [Lightweight K8s Distributions: K3s & k0s](./concepts/lightweight-k8s-distros/Readme.md) — what they are, why they exist, and how to get a cluster running

#### Concept Guides (newly added)
- [RBAC (Role-Based Access Control)](./concepts/rbac/Readme.md) — Roles, ClusterRoles, ServiceAccounts, worked example + `kubectl auth can-i`
- [NetworkPolicy](./concepts/network-policy/Readme.md) — default-deny and selective-allow pod-to-pod traffic rules
- [StatefulSets](./concepts/statefulset/Readme.md) — stable identity/storage for stateful workloads, done right vs. a plain Deployment
- [ResourceQuota & LimitRange](./concepts/resource-quota/Readme.md) — namespace-wide resource caps and defaults
- [Observability (Prometheus & Grafana)](./concepts/observability/Readme.md) — metrics, dashboards, `ServiceMonitor`
- [GitOps & CI/CD](./concepts/gitops-cicd/Readme.md) — push-based pipelines vs. pull-based GitOps (ArgoCD/Flux)
- [CRDs & Operators](./concepts/crd-operators/Readme.md) — extending the K8s API, the watch/diff/act control loop
- [Secrets Management Patterns](./concepts/secrets-management/Readme.md) — ConfigMap+Secret vs. External Secrets Operator vs. Vault Agent sidecar injection, compared

#### Commands & Templates
- [kubectl Cheatsheet](./commands/Cheatsheet.md) / [kubectl Reference](./commands/kubectl-reference.md)
- [Helm Commands Cheatsheet](./commands/helm-cheatsheet.md)
- [K8s YAML Templates](./k8s-yaml-templates/) — env vars, Deployment, Service, single vs. multi-container pod

#### Helm
- [Helm overview & installation](./helm/Readme.md)
- [`helm template` & multi-env `values` files](./helm/helm-template-and-multi-env-values.md) — local validation, `-f`/`--set` precedence, `lint` vs `template` vs `--dry-run`, diffing dev/stg/prd
- [`values.yaml` → rendered manifest walkthrough](./helm/values-to-templates-walkthrough.md) — Deployment/Service/Ingress/HPA field-by-field, plus a real dev/stg/prod render with two gotchas caught by actually reading the output

#### Hands-on Code Examples
- [`code/`](./code/) — Java, Python, and JS apps deployed to K8s (CRUD, cron jobs, scaling, ingress, Helm)
- [`code/java/k8s-with-springboot/secrets-tls-demo-app`](./code/java/k8s-with-springboot/secrets-tls-demo-app) — Spring Boot + Postgres, secrets via Vault/External Secrets Operator, TLS via cert-manager, optional ArgoCD GitOps source; see its [production-hardening.md](./code/java/k8s-with-springboot/secrets-tls-demo-app/production-hardening.md) for the local-vs-real-prod diff
- [`code/java/k8s-with-springboot/gke-backend-demo`](./code/java/k8s-with-springboot/gke-backend-demo) — Spring Boot + Cloud SQL, deployed to GKE with plain manifests (no Helm): Workload Identity, Cloud SQL Auth Proxy sidecar (no DB password), GKE Ingress with `BackendConfig`/`FrontendConfig`/`ManagedCertificate`; see its [gke-specific-features.md](./code/java/k8s-with-springboot/gke-backend-demo/gke-specific-features.md) for what's unique to GKE vs. portable Kubernetes
- [`Ingress/`](./Ingress/) — NGINX Ingress walkthrough with a multi-service demo project

---

