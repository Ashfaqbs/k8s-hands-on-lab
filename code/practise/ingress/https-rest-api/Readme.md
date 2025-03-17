A
HTTPS Setup for Single REST API in Kubernetes
Overview
This document outlines the process of securing a single REST API (darksharkash/simplerestapisb-k8s) with HTTPS in a Minikube Kubernetes cluster. We deployed one instance of the API, exposed it via a Service, and configured an Ingress with TLS to enable HTTPS access at https://springboot.local/helloworld. The focus is on why and how we added HTTPS, the creation of Secrets for certificates, and how Ingress uses them.
What We Did
We set up a simple host-based routing scenario with HTTPS:

    Deployed one Spring Boot API instance.
    Exposed it internally with a Service.
    Generated a self-signed certificate and key.
    Stored them in a Kubernetes Secret (explored both command and manifest methods).
    Configured an Ingress to use the Secret for HTTPS.
    Tested the secure endpoint.

Why We Did It

    Security: HTTPS encrypts traffic between the client (e.g., curl, browser) and the Ingress, protecting data from eavesdropping—crucial for real-world apps.
    Learning: Understand how Kubernetes Ingress handles TLS and integrates with Secrets for secure routing.
    Simplicity: Used a single API with host-based routing (springboot.local) to focus on HTTPS mechanics without path-based complexity.

How We Did It
Step 1: Deployed the API

    File: springboot-single.yaml
    yaml
    ````
    # Deployment Definition
apiVersion: apps/v1  # The API version for Deployment resource
kind: Deployment  # This specifies that we're defining a Deployment resource
metadata:
  name: springboot-deployment  # Name of the Deployment
  namespace: default  # The Kubernetes namespace where the Deployment will be created
spec:
  replicas: 1  # Number of pod replicas to create (1 pod for simplicity)
  selector:
    matchLabels:
      app: springboot  # Match pods with the label 'app=springboot' to manage them
  template:  # The template for the pod that will be created
    metadata:
      labels:
        app: springboot  # Label for the pod to identify it
    spec:
      containers:  # List of containers in the pod
      - name: springboot-api  # Name of the container
        image: darksharkash/simplerestapisb-k8s  # Docker image for the container
        ports:
        - containerPort: 8080  # Port the container will listen to

     # Service Definition
---
apiVersion: v1  # The API version for the Service resource
kind: Service  # This specifies that we're defining a Service resource
metadata:
  name: springboot-service  # Name of the Service
  namespace: default  # The namespace where the Service will be created
spec:
  selector:
    app: springboot  # Select pods with the label 'app=springboot' to route traffic to
  ports:
    - port: 80  # The port on which the service will be exposed inside the cluster
      targetPort: 8080  # The port on the container that the service will route traffic to
  type: ClusterIP  # This type of service exposes the service only within the cluster (default option)


    ````

    # Deployment and Service config

    Purpose: 
        One Deployment (springboot-deployment) with replicas: 1, running the API on port 8080.
        One Service (springboot-service) mapping port 80 to pod’s 8080.
    Commands: 
    bash

    kubectl apply -f springboot-single.yaml

    Verification: 
    bash

    kubectl get pods,services -n default

        Confirmed pod running and Service with a ClusterIP.

Step 2: Generated Self-Signed Certificate

    Command: 
    bash

    openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/CN=springboot.local"

    Why: 
        Needed a certificate and key for HTTPS—self-signed since this is local (Minikube).
        CN=springboot.local matches our hostname.
    Result: Created key.pem (private key) and cert.pem (certificate).

Step 3: Created Kubernetes Secret
We explored two methods to store the cert and key in a Secret named springboot-tls:
Option 1: Command-Line

    Command: 
    bash

    kubectl create secret tls springboot-tls --key key.pem --cert cert.pem -n default

    How: 
        Took key.pem and cert.pem, bundled them into a tls Secret.
    Why: 
        Quick setup—no extra files, ideal for one-time use.
    Output: 

    secret/springboot-tls created

Option 2: Manifest File (Chosen Method)

    File: springboot-tls-secret.yaml
    yaml

    apiVersion: v1
    kind: Secret
    metadata:
      name: springboot-tls
      namespace: default
    type: kubernetes.io/tls
    data:
      tls.key: <base64-encoded-key>
      tls.crt: <base64-encoded-cert>

    Steps: 
        Converted files to base64:
        bash

        cat key.pem | base64 | tr -d '\n'  # For tls.key
        cat cert.pem | base64 | tr -d '\n' # For tls.crt

        Inserted base64 strings into the YAML.
        Applied:
        bash

        kubectl apply -f springboot-tls-secret.yaml

    Why Chosen: 
        YAML is repeatable, versionable, and aligns with our other configs.
    Output: 

    secret/springboot-tls created

    Verification: 
    bash

    kubectl get secret springboot-tls -n default

        Showed TYPE: kubernetes.io/tls with 2 data entries (tls.key, tls.crt).

Step 4: Configured Ingress with HTTPS

    File: springboot-https-ingress.yaml
    yaml

    apiVersion: networking.k8s.io/v1
    kind: Ingress
    metadata:
      name: springboot-https-ingress
      namespace: default
    spec:
      ingressClassName: nginx
      tls:
      - hosts:
        - springboot.local
        secretName: springboot-tls
      rules:
      - host: "springboot.local"
        http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: springboot-service
                port:
                  number: 80

    How: 
        tls section links to springboot-tls Secret for springboot.local.
        rules routes springboot.local to springboot-service.
    Commands: 
    bash

    kubectl apply -f springboot-https-ingress.yaml

    Verification: 
    bash

    kubectl get ingress -n default

        Showed PORTS: 80, 443—NGINX now handles HTTPS.

Step 5: Updated /etc/hosts

    Command: 
    bash

    minikube ip  # e.g., 192.168.49.2
    sudo nano /etc/hosts

    Added: 

    192.168.49.2 springboot.local

    Why: Resolves springboot.local to Minikube’s IP locally.

Step 6: Tested HTTPS

    Command: 
    bash

    curl --insecure https://springboot.local/helloworld

    Output: 

    Hello World

    Why --insecure: Self-signed cert isn’t trusted—bypasses validation for testing.

Why We Added Certs in a Secret

    Purpose: The Secret (springboot-tls) holds the private key (tls.key) and certificate (tls.crt) needed for HTTPS encryption.
    Reason: 
        Kubernetes Secrets securely store sensitive data (like certs) and make them available to resources like Ingress.
        Keeps certs out of plaintext YAML—base64-encoded and managed by the cluster.
    Alternative: Without a Secret, we’d have to bake certs into the Ingress (messy) or mount them manually (complex).

How Ingress Reads and Applies the Data

    Config: In springboot-https-ingress.yaml:
    yaml

    spec:
      tls:
      - hosts:
        - springboot.local
        secretName: springboot-tls

    Process: 
        Ingress Controller (NGINX): 
            Sees the tls section, looks up springboot-tls Secret in default namespace.
            Loads tls.key and tls.crt from the Secret.
        TLS Setup: 
            NGINX uses the key and cert to enable HTTPS on port 443 for springboot.local.
        Request Handling: 
            When https://springboot.local/helloworld hits 192.168.49.2:443, NGINX:
                Encrypts/decrypts traffic using the cert/key.
                Matches springboot.local rule, forwards to springboot-service:80.
    Flow: 
        Client → NGINX (encrypted) → Service (unencrypted) → Pod → Response (encrypted back).

How We Created Secrets

    Command-Line: 
        Used kubectl create secret tls to directly package key.pem and cert.pem.
        Fast, no YAML needed.
    Manifest File (Chosen): 
        Generated base64 strings from key.pem and cert.pem.
        Wrote springboot-tls-secret.yaml with type: kubernetes.io/tls.
        Applied with kubectl apply.
        Preferred for consistency and repeatability.

Summary
We secured our single REST API with HTTPS by:

    Deploying it simply with one pod and Service.
    Creating a self-signed cert, storing it in a Secret (via manifest for control).
    Configuring Ingress to use the Secret for TLS on springboot.local.
    Testing with HTTPS, proving encryption works.
    This setup mimics production HTTPS (minus real certs) and sets us up for scaling next.

