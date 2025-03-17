
Rough Flow with Context
1. Install NGINX for Ingress and Verify Its Components Are Up and Running

    Command: 
    bash

    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

    Why: We need an Ingress Controller to handle external traffic and route it based on our Ingress rules. NGINX is a popular choice—it’s a reverse proxy that listens for HTTP/HTTPS requests and directs them to our services.
    How: This command pulls a YAML file from the official NGINX Ingress repo and deploys a bunch of resources into our cluster (in the ingress-nginx namespace).
    What Components:
        Namespace (ingress-nginx): Isolates Ingress resources from our app’s namespace (default).
        Deployment (ingress-nginx-controller): Runs the NGINX pod(s) that process traffic.
        Service (ingress-nginx-controller): Exposes NGINX to the outside world (in Minikube, via NodePort).
        ConfigMap: Holds NGINX configuration settings.
        RBAC (Roles, RoleBindings, etc.): Grants NGINX permissions to watch Ingress resources.
        Admission Webhooks: Validates Ingress configs (optional, but part of the setup).
    Purpose of Components:
        Deployment/Pods: The actual NGINX server doing the routing.
        Service: Gives NGINX an IP and port (e.g., 80:31234) to receive traffic.
        RBAC: Ensures NGINX can read our Ingress rules and talk to the Kubernetes API.
    Verify:
    bash

    kubectl get pods -n ingress-nginx

        Output: 

        NAME                                       READY   STATUS    RESTARTS   AGE
        ingress-nginx-controller-d8c96cf68-spr5n   1/1     Running   0          5m

            1/1 Running = NGINX is up.
    bash

    kubectl get service -n ingress-nginx

        Output: 

        NAME                        TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
        ingress-nginx-controller    NodePort   10.96.x.x       <none>        80:31234/TCP,443:32456/TCP   5m

            Shows NGINX is exposed via NodePort (e.g., 31234).

2. Enable NGINX from Minikube Side

    Command: 
    bash

    minikube addons enable ingress

    Why: Minikube has a built-in NGINX Ingress Controller tuned for its environment. Enabling it ensures seamless integration with Minikube’s networking (e.g., binding to 192.168.49.2).
    What: Deploys a simplified version of the NGINX Ingress Controller in ingress-nginx. It’s an alternative to the manual install above—less config hassle for Minikube users.
    How: Minikube manages the deployment, service, and networking setup automatically. It’s preconfigured to work with Minikube’s IP and ports.
    Why This vs. Manual Install: 
        Manual install gives more control (e.g., custom versions), but Minikube’s addon fixes common issues (like our earlier curl failure) by aligning with Minikube’s quirks.
    Verify:
    bash

    minikube addons list | grep ingress

        Output: 

        | ingress              | enabled    | ingress               |

            enabled = it’s active.

3. Create Deployment and Service Object for Spring Boot API and Test with Port-Forwarding

    Command: 
    bash

    kubectl apply -f springboot-app.yaml

    File (springboot-app.yaml):
    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: springboot-api
      namespace: default
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: springboot-api
      template:
        metadata:
          labels:
            app: springboot-api
        spec:
          containers:
          - name: springboot-api
            image: darksharkash/simplerestapisb-k8s
            ports:
            - containerPort: 8080
    apiVersion: v1
    kind: Service
    metadata:
      name: springboot-api-service
      namespace: default
    spec:
      selector:
    app: springboot-api
      ports:
        port: 80
        targetPort: 8080
          type: ClusterIP

    Why: 
        Deployment: Runs our Spring Boot app pod(s) with our Docker image.
        Service: Creates an internal endpoint to talk to those pods.
    How: 
        Deployment spins up a pod with our image, exposing port 8080 (Spring Boot default).
        Service maps port 80 (internal) to pod’s 8080, making it accessible within the cluster.
    What: 
        Pods are the app instances; Service is their network glue.
    Test with Port-Forwarding:
    bash

    kubectl port-forward service/springboot-api-service 8080:80 -n default

        Why: Bypasses Ingress to test the app directly from our local machine.
        How: Forwards Service’s port 80 to our localhost 8080.
        Output: 

        Forwarding from 127.0.0.1:8080 -> 80

        Test: 
        bash

        curl http://localhost:8080/helloworld

            Output: "Hello World" (or our API’s response).

4. Create Ingress for Our Code, Map Service, Apply, and Check Pods/Services

    Command: 
    bash

    kubectl apply -f springboot-ingress.yaml

    File (springboot-ingress.yaml):
    yaml

    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: springboot-ingress
      namespace: default
    spec:
      ingressClassName: nginx
      rules:
      - host: "springboot.local"
        http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: springboot-api-service
                port:
                  number: 80

    Why: Defines how external traffic (e.g., springboot.local) reaches our Service.
    How: 
        Maps springboot.local requests to springboot-api-service:80.
        NGINX Ingress Controller reads this and sets up routing.
    What: Links our app to the outside world via hostname rules.
    Apply Output: 

    ingress.networking.k8s.io/springboot-ingress created

    Check Pods/Services:
    bash

    kubectl get pods,services -n default

        Output: 

        NAME                             READY   STATUS    RESTARTS   AGE
        pod/springboot-api-abcdef-12345  1/1     Running   0          5m
        NAME                         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
        service/springboot-api-service ClusterIP   10.96.x.x      <none>        80/TCP    5m

    bash

    kubectl get ingress -n default

        Output: 

        NAME                 CLASS    HOSTS              ADDRESS        PORTS   AGE
        springboot-ingress   <none>   springboot.local   192.168.49.2   80      2m

5. Call the API to Test

    Setup: Add to /etc/hosts:

    192.168.49.2 springboot.local

    Command: 
    bash

    curl http://springboot.local/helloworld

    Why: Confirms Ingress routes external traffic to our API.
    How: 
        springboot.local resolves to Minikube IP (192.168.49.2).
        NGINX sees the host, matches the Ingress rule, forwards to Service, then pod.
    What: End-to-end test of our setup.
    Output: 

    Hello World

        Success! If not, debug with kubectl describe ingress or logs.

Why This Flow Works

    NGINX Install: Sets up the traffic handler.
    Minikube Enable: Simplifies networking for Minikube.
    Deployment/Service: Runs our app and makes it cluster-accessible.
    Ingress: Exposes it externally with hostname rules.
    Testing: Validates each layer (port-forward for app, curl for Ingress).