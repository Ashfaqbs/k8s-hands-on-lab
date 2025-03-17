Alright, man! Let’s document this whole journey from planning the dual REST API setup to getting it running smoothly. I’ll cover why we picked this approach, what files we created, how we tested, the Ingress setup, issues we hit, fixes we applied, /etc/hosts updates, and wrap it up with a bit on host-based vs. path-based routing. I’ll add context, explain tricky bits, and keep it chill so you can follow along or revisit later. Here we go!
Dual REST API Project Documentation
Plan: Why Dual REST APIs?

    Goal: Set up two REST APIs using the same Spring Boot image (darksharkash/simplerestapisb-k8s) to learn how Ingress routes traffic to multiple services in Kubernetes.
    Why: 
        Ingress Mastery: Shows how Ingress can handle multiple APIs with one controller, using rules to split traffic—key for real-world apps with many endpoints.
        Same Image: Keeps it simple—same code, different instances—so we focus on Kubernetes, not app changes.
        Path-Based Routing: Picked this over port differences to see how Ingress uses URL paths (e.g., /api1 vs. /api2) instead of tweaking app ports (harder with a fixed image).
    What: 
        Two Deployments (separate pods).
        Two Services (internal endpoints).
        One Ingress (routes /api1 and /api2 to each Service).

Files We Created
1. springboot-apis.yaml

    Purpose: Defines two Deployments and Services for the APIs.
    Content:
    ```yaml
    # API 1 Deployment
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: api1-deployment
      namespace: default
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: api1
      template:
        metadata:
          labels:
            app: api1
        spec:
          containers:
          - name: springboot-api
            image: darksharkash/simplerestapisb-k8s
            ports:
            - containerPort: 8080
    API 1 Service
    apiVersion: v1
    kind: Service
    metadata:
      name: api1-service
      namespace: default
    spec:
      selector:
        app: api1
      ports:
      - port: 80
        targetPort: 8080
      type: ClusterIP
    API 2 Deployment
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: api2-deployment
      namespace: default
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: api2
      template:
        metadata:
          labels:
            app: api2
        spec:
          containers:
          - name: springboot-api
            image: darksharkash/simplerestapisb-k8s
            ports:
            - containerPort: 8080
    API 2 Service
    apiVersion: v1
    kind: Service
    metadata:
      name: api2-service
      namespace: default
    spec:
      selector:
    app: api2
      ports:
        port: 80
        targetPort: 8080
          type: ClusterIP
```
    What: 
        Deployments: api1-deployment and api2-deployment run one pod each, both on port 8080.
        Services: api1-service and api2-service map port 80 (internal) to pod’s 8080, using labels (app: api1, app: api2) to target pods.

2. springboot-multi-ingress.yaml (Final Version)

    Purpose: Sets up Ingress to route traffic to both APIs based on paths.
    Content:
    
````yaml

    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: springboot-multi-ingress
      namespace: default
      annotations:
        nginx.ingress.kubernetes.io/rewrite-target: /$2
    spec:
      ingressClassName: nginx
      rules:
      - host: "springboot.local"
        http:
          paths:
          - path: /api1(/|$)(.*)
            pathType: ImplementationSpecific
            backend:
              service:
                name: api1-service
                port:
                  number: 80
          - path: /api2(/|$)(.*)
            pathType: ImplementationSpecific
            backend:
              service:
                name: api2-service
                port:
                  number: 80

````
    What: 
        Routes springboot.local/api1/* to api1-service and springboot.local/api2/* to api2-service.
        Annotation: rewrite-target: /$2 strips /api1 or /api2, sending just /helloworld to the pods.
        Path Type: ImplementationSpecific supports NGINX regex paths.

Setup and Verification
1. Ensure Ingress Controller

    Command: 
    bash

    minikube addons enable ingress

        Why: Activates Minikube’s NGINX Ingress Controller—handles all Ingress routing.
        Output: 

        * The 'ingress' addon is enabled

    Verify: 
    bash

    kubectl get pods -n ingress-nginx

        Output: 

        NAME                                       READY   STATUS    RESTARTS   AGE
        ingress-nginx-controller-d8c96cf68-spr5n   1/1     Running   0          5m

2. Deploy APIs

    Command: 
    bash

    kubectl apply -f springboot-apis.yaml

        Output: 

        deployment.apps/api1-deployment created
        service/api1-service created
        deployment.apps/api2-deployment created
        service/api2-service created

    Verify: 
    bash

    kubectl get pods,services -n default

        Output: 

        NAME                                  READY   STATUS    RESTARTS   AGE
        pod/api1-deployment-xyz-12345         1/1     Running   0          2m
        pod/api2-deployment-abc-67890         1/1     Running   0          2m
        NAME                    TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
        service/api1-service    ClusterIP   10.96.x.x      <none>        80/TCP    2m
        service/api2-service    ClusterIP   10.96.y.y      <none>        80/TCP    2m

3. Apply Ingress

    Command: 
    bash

    kubectl apply -f springboot-multi-ingress.yaml

        Output (Final): 

        ingress.networking.k8s.io/springboot-multi-ingress configured

Issues We Faced and Fixes
Issue 1: YAML Syntax Error in springboot-apis.yaml

    Problem: 
        Error: error converting YAML to JSON: yaml: line 140: mapping values are not allowed in this context.
        Cause: Missing - in ports list for api2-service and no --- separators.
    Fix: 
        Added --- between resources.
        Corrected ports:
        yaml

        ports:
        - port: 80
          targetPort: 8080

    Result: Applied cleanly.

Issue 2: 404 Error on API Call

    Problem: 
        curl http://springboot.local/api1/helloworld → 404 Not Found, path: "/api1/helloworld".
        Cause: NGINX sent /api1/helloworld to the pod, but Spring Boot only knows /helloworld.
    Fix: 
        Added nginx.ingress.kubernetes.io/rewrite-target: /$2 annotation.
        Changed paths to /api1(/|$)(.*) and /api2(/|$)(.*) to capture and rewrite.
        Hard Concept: 
            Rewrite: NGINX rewrites /api1/helloworld to /helloworld using regex groups ($2 = (.*) part). Tricky because it’s NGINX-specific, not pure Kubernetes.
    Result: APIs responded with "Hello World".

Issue 3: Path Type Warning

    Problem: 
        Warning: path /api1(/|$)(.*) cannot be used with pathType Prefix.
        Cause: Prefix doesn’t support regex; our paths needed NGINX’s custom handling.
    Fix: 
        Changed pathType: Prefix to pathType: ImplementationSpecific.
        Hard Concept: 
            pathType: Prefix is for simple paths; ImplementationSpecific delegates to NGINX for regex, aligning Kubernetes spec with controller behavior.
    Result: No warnings, still works.

Updating /etc/hosts

    Command: 
    bash

    minikube ip  # e.g., 192.168.49.2
    sudo nano /etc/hosts

    Added: 

    192.168.49.2 springboot.local

    Why: Maps springboot.local to Minikube’s IP so your system resolves it locally (no real DNS for this fake domain).
    Context: Only needed once unless Minikube’s IP changes (e.g., after minikube delete).

Testing

    Commands: 
    bash

    curl http://springboot.local/api1/helloworld
    curl http://springboot.local/api2/helloworld

    Output: 

    Hello World  # From api1
    Hello World  # From api2

    What: 
        NGINX routes /api1/helloworld to api1-service, /api2/helloworld to api2-service.
        Rewrite strips prefixes, pods see /helloworld, respond correctly.

Host-Based vs. Path-Based Forwarding
Path-Based (What We Did)

    How: One hostname (springboot.local), different paths (/api1, /api2).
    Ingress: 
    yaml

    rules:
    - host: "springboot.local"
      http:
        paths:
        - path: /api1(/|$)(.*)
        - path: /api2(/|$)(.*)

    Pros: 
        Simple—one /etc/hosts entry.
        Good for apps under one domain.
    Cons: 
        Path conflicts if apps use overlapping routes.
    Flow: springboot.local/api1/helloworld → NGINX → api1-service → pod.

Host-Based (Alternative)

    How: Different hostnames (api1.springboot.local, api2.springboot.local), same or different paths.
    Example Ingress: 
    yaml

    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: springboot-host-ingress
      namespace: default
    spec:
      ingressClassName: nginx
      rules:
      - host: "api1.springboot.local"
        http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api1-service
                port:
                  number: 80
      - host: "api2.springboot.local"
        http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api2-service
                port:
                  number: 80

    /etc/hosts:

    192.168.49.2 api1.springboot.local
    192.168.49.2 api2.springboot.local

    Test: 
    bash

    curl http://api1.springboot.local/helloworld
    curl http://api2.springboot.local/helloworld

    Pros: 
        Clean separation—each API gets its own “domain.”
        No rewrite needed if paths match app endpoints.
    Cons: 
        More /etc/hosts entries.
    Flow: api1.springboot.local/helloworld → NGINX → api1-service → pod.
    Hard Concept: 
        Hostname Routing: NGINX uses the Host header (e.g., api1.springboot.local) to pick the right rule. Common in production with real DNS, simulated here with /etc/hosts.

Why Path-Based First?

    Simpler for learning—fewer moving parts. Host-based adds hostname complexity, great for next-level practice.

Wrap-Up

    What We Learned: 
        Deployed two APIs with one image.
        Used Ingress to route via paths, fixed 404s with rewrites, and cleaned up pathType warnings.
        /etc/hosts ties it to Minikube.

