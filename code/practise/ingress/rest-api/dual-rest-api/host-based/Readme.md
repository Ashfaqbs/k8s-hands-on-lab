This project sets up two REST APIs using the same Spring Boot image (darksharkash/simplerestapisb-k8s) in a Minikube Kubernetes cluster. We’re using host-based routing with Ingress to direct traffic to each API via unique hostnames (api1.springboot.local and api2.springboot.local). This README explains the setup, why we chose this style, and how it all works.
Why Host-Based Routing?

    Goal: Route traffic to two API instances based on hostnames instead of paths.
    Reason: 
        Mimics real-world setups where APIs have distinct domains (e.g., api1.company.com).
        Simplifies URL handling—no need to tweak paths to match app endpoints.
    How It Works: 
        NGINX Ingress Controller checks the Host header in HTTP requests (e.g., api1.springboot.local) and sends traffic to the right Service based on Ingress rules.

How Host-Based Routing Works

    Setup: 
        Two Deployments (api1-deployment, api2-deployment) run the Spring Boot app on port 8080.
        Two Services (api1-service, api2-service) expose each Deployment on port 80.
        One Ingress (springboot-host-ingress) defines rules for api1.springboot.local and api2.springboot.local.
    Flow: 
        we call http://api1.springboot.local/helloworld.
        System resolves api1.springboot.local to Minikube’s IP (e.g., 192.168.49.2) via /etc/hosts.
        NGINX sees Host: api1.springboot.local, matches the Ingress rule, sends /helloworld to api1-service:80, which hits a pod on 8080.
        Pod responds with "Hello World". Same deal for api2.springboot.local.

Pros and Cons Compared to Path-Based Routing
Host-Based

    Pros: 
        Clean Separation: Each API gets its own “domain”—feels independent, no path overlap worries.
        Simpler URLs: No rewriting needed—/helloworld goes straight to the app as-is.
        Scales Well: Easy to add more APIs with new hostnames.
    Cons: 
        More /etc/hosts Entries: Need one line per hostname (e.g., two here vs. one for path-based).
        DNS Setup: In a real setup, we’d need actual DNS records, not just local hacks.

Path-Based

    Pros: 
        Single Hostname: One /etc/hosts entry (e.g., springboot.local) covers all paths.
        Less Config Overhead: Fewer hostnames to manage locally.
    Cons: 
        Path Conflicts: If apps use overlapping paths, we’re in trouble.
        Rewrites Needed: Had to use rewrite-target to strip /api1—extra complexity.

Challenges Faced in Path-Based (Avoided Here)

    404 Errors: 
        Path-Based: NGINX sent /api1/helloworld to the pod, but Spring Boot only knew /helloworld—needed rewrite-target: /$2 and regex (/api1(/|$)(.*)) to fix.
        Host-Based: No rewrite—/helloworld matches the app’s endpoint directly, no mismatch.
    Path Type Warnings: 
        Path-Based: pathType: Prefix didn’t like regex, threw warnings—switched to ImplementationSpecific.
        Host-Based: Simple / path with Prefix works fine, no warnings.
    Complexity: 
        Path-Based: Regex and annotations added brain twists.
        Host-Based: Straightforward—hostname rules are cleaner.

Configuration Files
Spring Config

    File: springboot-apis.yaml
    Content: 

    spring config

```


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
---       
#API 1 Service
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
--- 
#API 2 Deployment
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

---        
#API 2 Service
apiVersion: v1
kind: Service
metadata:
  name: api2-service
  namespace: default
spec:
  selector:
    app: api2
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

  

```

        Defines api1-deployment, api1-service, api2-deployment, and api2-service.
        Two Deployments run the image on 8080, Services map 80 to 8080.

Ingress Config

    File: springboot-host-ingress.yaml
    Content: 
```yaml

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
```
        Routes api1.springboot.local to api1-service, api2.springboot.local to api2-service.

Setup Commands

    Deploy:
    bash

    kubectl apply -f springboot-apis.yaml
    kubectl apply -f springboot-host-ingress.yaml

    Verify:
    bash

    kubectl get pods,services,ingress -n default

Updating /etc/hosts

    Remove Old:
    bash

    sudo nano /etc/hosts

        Delete 192.168.49.2 springboot.local (from path-based).
    Add New:
    bash

    minikube ip  # e.g., 192.168.49.2
    sudo nano /etc/hosts

        Add:

        192.168.49.2 api1.springboot.local
        192.168.49.2 api2.springboot.local

    Why It Helps: 
        These fake hostnames aren’t real domains—/etc/hosts tells our system they mean 192.168.49.2 (Minikube’s IP).
        Without it, curl or our browser would say “Can’t find this site” since no DNS knows them.

When we Call the URL

    Example: curl http://api1.springboot.local/helloworld
    What Happens: 
        our System: Checks /etc/hosts, sees api1.springboot.local = 192.168.49.2, sends request to 192.168.49.2:80.
        NGINX: 
            Gets Host: api1.springboot.local from the HTTP header.
            Matches api1.springboot.local rule in Ingress, sends /helloworld to api1-service:80.
        Service: Forwards to a pod (label app: api1) on 8080.
        Spring Boot: Sees /helloworld, responds with "Hello World".
        Back to us: Response travels pod → Service → NGINX → our terminal.
    For api2: Same, but NGINX picks api2-service based on Host: api2.springboot.local.

Summary

    What We Did: 
        Switched from path-based (/api1, /api2) to host-based routing for two Spring Boot APIs.
        Used one Ingress with hostname rules, avoiding rewrites and regex hassles.
    Why It’s Cool: 
        Cleaner, more like real apps, and sidesteps path-based headaches (404s, warnings).
    Key Takeaway: 
        Ingress can route by host or path—host-based shines when we want distinct “domains” without URL tricks.

