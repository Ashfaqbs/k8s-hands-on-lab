
Auto-Scaling HTTPS REST API in Kubernetes

Overview
This document details how we added auto-scaling to our existing HTTPS-secured REST API project. We used the same https-rest-api setup (a single Spring Boot API with HTTPS via Ingress) and extended it with Horizontal Pod Autoscaling (HPA) to dynamically adjust pod count based on traffic load (CPU usage). Below, we outline the process, explain ReplicaSets and HPA, and show how we tested and monitored the scaling.
Base Setup: HTTPS REST API Project
We started with our https-rest-api project, which secures darksharkash/simplerestapisb-k8s at https://springboot.local/helloworld. Here’s the quick flow we reused:

    Code YAML: springboot-single.yaml
        Deployment (springboot-deployment) and Service (springboot-service).
    Secret with HTTPS YAML: springboot-tls-secret.yaml
        Stored self-signed cert and key for springboot.local (created via manifest).
    Ingress YAML: springboot-https-ingress.yaml
        Configured HTTPS with TLS, routing springboot.local to springboot-service.
    Applied:
    bash

    kubectl apply -f springboot-single.yaml
    kubectl apply -f springboot-tls-secret.yaml
    kubectl apply -f springboot-https-ingress.yaml

    Updated /etc/hosts:
    bash

    # Added: 192.168.49.2 springboot.local

This gave us a single pod with HTTPS—our foundation for scaling.
What is a ReplicaSet?

    Definition: A ReplicaSet is a Kubernetes resource that ensures a set number of pods (replicas) are running at all times. It’s managed by a Deployment.
    In Our Case: 
        springboot-deployment uses a ReplicaSet to keep pods alive.
        Originally replicas: 1—one pod. We could manually set it to 3, but that’s static scaling.
    Why It Matters: Ensures reliability—if a pod dies, the ReplicaSet spins up a replacement. But it doesn’t adjust based on load—that’s where HPA comes in.

What is HPA (Horizontal Pod Autoscaler)?

    Definition: HPA automatically scales the number of pods in a Deployment based on resource usage (e.g., CPU) or custom metrics.
    In Our Case: 
        We added HPA to springboot-deployment to scale pods from 1 to 5 when CPU hits 50% of the limit.
    Why We Used It: 
        Handles traffic spikes dynamically—more pods when busy, fewer when quiet.
        Saves resources—no need to over-provision pods manually.

What HPA Needs: Metrics Server

    What: Metrics Server collects CPU/memory usage from pods and feeds it to HPA.
    How We Enabled It: 
    bash

    minikube addons enable metrics-server

        Output: 

        * The 'metrics-server' addon is enabled

        Verified: 
        bash

        kubectl get pods -n kube-system -l k8s-app=metrics-server

            Saw it running (e.g., metrics-server-5f9f7c7df-abcde).
    How It Helps: 
        Provides real-time metrics—HPA uses this to decide when to scale.
        Without it, HPA’s blind—no data, no scaling.

How We Set It Up
Step 1: Added Resource Limits to Deployment

    Why: HPA needs CPU targets to work—limits/requests give it something to measure.
    How: Updated springboot-single.yaml:
    yaml

    spec:
      template:
        spec:
          containers:
          - name: springboot-api
            resources:
              requests:
                cpu: "100m"  # Asks for 0.1 CPU
              limits:
                cpu: "200m"  # Caps at 0.2 CPU

        Applied: 
        bash

        kubectl apply -f springboot-single.yaml

Step 2: Created HPA Resource

    Why: Defines the auto-scaling rules—when and how many pods.
    How: Created springboot-hpa.yaml:
    yaml

    apiVersion: autoscaling/v1
    kind: HorizontalPodAutoscaler
    metadata:
      name: springboot-hpa
      namespace: default
    spec:
      scaleTargetRef:
        apiVersion: apps/v1
        kind: Deployment
        name: springboot-deployment
      minReplicas: 1
      maxReplicas: 5
      targetCPUUtilizationPercentage: 50

        Applied: 
        bash

        kubectl apply -f springboot-hpa.yaml

        Details: 
            Targets springboot-deployment.
            Scales between 1-5 pods.
            Triggers at 50% CPU (100m out of 200m).

How We Tested It
Step 1: Simulated Traffic

    Why: Needed to spike CPU usage to trigger HPA scaling.
    How: Used hey for load:
    bash

    sudo apt install hey
    hey -n 10000 -c 10 -m GET https://springboot.local/helloworld --insecure

        10,000 requests, 10 concurrent clients—enough to push CPU over 50%.

Step 2: Watched Scaling

    Why: Confirmed HPA reacted to load.
    How: 
        Monitored HPA:
        bash

        kubectl get hpa -n default --watch

            Output (Before Load): 

            NAME            REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
            springboot-hpa  Deployment/springboot-deployment  0%/50%    1         5         1         2m

            Output (During Load): 

            NAME            REFERENCE                    TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
            springboot-hpa  Deployment/springboot-deployment  120%/50%   1         5         3         5m

            Saw replicas jump (e.g., 1 → 3).
        Checked Pods:
        bash

        kubectl get pods -n default -l app=springboot

            Output: 

            NAME                                  READY   STATUS    RESTARTS   AGE
            springboot-deployment-xyz-12345       1/1     Running   0          5m
            springboot-deployment-xyz-67890       1/1     Running   0          1m
            springboot-deployment-xyz-abcde       1/1     Running   0          1m

    Details: CPU hit 120m (over 50%), HPA scaled to 3 pods.

Step 3: Tested Load Balancing with HTTPS

    Why: Verified Service spread traffic across new pods, HTTPS stayed solid.
    How: 
    bash

    for i in {1..10}; do curl --insecure https://springboot.local/helloworld; echo; done

        Output: 

        Hello World
        Hello World
        ...

        Endpoints: 
        bash

        kubectl get endpoints springboot-service -n default

            Output: 

            NAME               ENDPOINTS                           AGE
            springboot-service 10.244.0.5:8080,10.244.0.6:8080,10.244.0.7:8080   10m

    Details: 10 requests hit different pods—Service balanced, HTTPS worked.

How It Works

    HPA: 
        Watches CPU via Metrics Server.
        Scales springboot-deployment when CPU exceeds 50% (e.g., 100m+ out of 200m).
    Service: 
        Updates endpoints as pods scale—load balances automatically.
    Ingress: 
        Routes HTTPS to Service—unaware of pod count, just forwards.
    HTTPS: 
        NGINX encrypts at 443—scaling doesn’t affect it.

Summary
We took our HTTPS REST API project and added HPA to auto-scale pods based on traffic (CPU load). Starting with 1 pod, we spiked usage with hey, watched HPA scale to 3 (max 5), and confirmed load balancing over HTTPS. Metrics Server powered the magic, and our setup stayed secure and responsive—ready for real traffic swings.
