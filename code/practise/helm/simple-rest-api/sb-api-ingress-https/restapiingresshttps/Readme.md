
This Helm chart (restapiingresshttps) deploys a Spring Boot REST API (darksharkash/simplerestapisb-k8s) on Minikube, exposed via HTTPS Ingress on springboot.local. It uses self-signed certificates stored in a Kubernetes Secret.
Prerequisites

    Minikube: Running locally.
    Helm: Installed.
    kubectl: Configured for Minikube.
    OpenSSL: For generating self-signed certs.
    Image: darksharkash/simplerestapisb-k8s:latest (Spring Boot app on port 8080).

Steps
1. Create the Helm Chart
bash

cd ~/Desktop/code/k8s/helm/simple-rest-api
helm create restapiingresshttps

    Why: Generates a Helm chart scaffold with default templates and values.yaml.

2. Update Chart.yaml
File: restapiingresshttps/Chart.yaml
yaml

apiVersion: v2
name: restapiingresshttps
description: A Helm chart for Spring Boot REST API with HTTPS Ingress
type: application
version: 0.1.0  # Initial version
appVersion: "1.16.0"  # Matches Spring Boot app version

    Why: Customizes chart name and description for HTTPS focus; appVersion aligns with the image.

3. Generate Self-Signed Certificates
bash

mkdir -p restapiingresshttps/certs
cd restapiingresshttps/certs
openssl req -x509 -newkey rsa:4096 -keyout tls.key -out tls.crt -days 365 -nodes -subj "/CN=springboot.local"

    Why: Creates tls.crt (certificate) and tls.key (private key) for springboot.local—self-signed for testing.
    Note: certs/ is placed in the chart root directory (restapiingresshttps/certs/).

4. Update values.yaml
File: restapiingresshttps/values.yaml
yaml

replicaCount: 1  # Single pod for simplicity

image:
  repository: darksharkash/simplerestapisb-k8s  # Our Spring Boot image
  pullPolicy: IfNotPresent  # Pull only if not cached
  tag: "latest"  # Use latest version

service:
  type: ClusterIP  # Internal service—Ingress handles external access
  port: 8080  # Matches Spring Boot’s listening port

ingress:
  enabled: true  # Enable Ingress for hostname routing
  className: "nginx"  # Minikube’s Ingress controller
  annotations: {}  # No extra annotations needed yet
  hosts:
    - host: springboot.local  # Custom hostname for HTTPS
      paths:
        - path: /  # Route all paths (including /helloworld)
          pathType: Prefix  # Match any subpath
  tls:
    - secretName: restapi-tls  # Secret holding TLS certs
      hosts:
        - springboot.local  # Applies TLS to this host

livenessProbe:
  httpGet:
    path: /helloworld  # Endpoint Spring Boot serves
    port: 8080  # App port
  initialDelaySeconds: 10  # Wait 10s for app to start (~5s startup time)
  timeoutSeconds: 5  # Allow 5s for response
  periodSeconds: 10  # Check every 10s
readinessProbe:
  httpGet:
    path: /helloworld  # Same endpoint
    port: 8080  # Same port
  initialDelaySeconds: 10  # Delay for startup
  timeoutSeconds: 5  # Response timeout
  periodSeconds: 10  # Check interval

# Keep defaults below—optional to trim for simplicity
serviceAccount:
  create: true
  automount: true
  annotations: {}
  name: ""
podAnnotations: {}
podLabels: {}
podSecurityContext: {}
securityContext: {}
resources: {}
autoscaling:
  enabled: false
  minReplicas: 1
  maxReplicas: 100
  targetCPUUtilizationPercentage: 80
volumes: []
volumeMounts: []
nodeSelector: {}
tolerations: []
affinity: {}

    Why: 
        service.port: 8080—aligns with Spring Boot’s port.
        ClusterIP—Ingress exposes it externally.
        ingress.tls—enables HTTPS with the restapi-tls Secret.
        Probes—ensure app health with startup delay.

5. Update templates/deployment.yaml
File: restapiingresshttps/templates/deployment.yaml
yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "restapiingresshttps.fullname" . }}
  labels:
    {{- include "restapiingresshttps.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "restapiingresshttps.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "restapiingresshttps.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          ports:
            - name: http
              containerPort: 8080  # Spring Boot listens here
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /helloworld  # Our endpoint
              port: 8080  # Match app port
            initialDelaySeconds: 10  # Wait for startup
            timeoutSeconds: 5  # Response timeout
            periodSeconds: 10  # Check interval
          readinessProbe:
            httpGet:
              path: /helloworld  # Same endpoint
              port: 8080  # Match port
            initialDelaySeconds: 10  # Wait for startup
            timeoutSeconds: 5  # Response timeout
            periodSeconds: 10  # Check interval

    Why: 
        containerPort: 8080—matches Spring Boot.
        Probes—override defaults with correct path/port and delay.

6. Update templates/service.yaml
File: restapiingresshttps/templates/service.yaml
yaml

apiVersion: v1
kind: Service
metadata:
  name: {{ include "restapiingresshttps.fullname" . }}
  labels:
    {{- include "restapiingresshttps.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}  # ClusterIP from values.yaml
  ports:
    - port: {{ .Values.service.port }}  # 8080
      targetPort: 8080  # Maps to container port
      protocol: TCP
      name: http
  selector:
    {{- include "restapiingresshttps.selectorLabels" . | nindent 4 }}

    Why: 
        port: 8080—exposed by Service.
        targetPort: 8080—routes to container.

7. Add templates/ingress.yaml
File: restapiingresshttps/templates/ingress.yaml
yaml

{{- if .Values.ingress.enabled }}
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "restapiingresshttps.fullname" . }}
  labels:
    {{- include "restapiingresshttps.labels" . | nindent 4 }}
  {{- with .Values.ingress.annotations }}
  annotations:
    {{- toYaml . | nindent 4 }}
  {{- end }}
spec:
  ingressClassName: {{ .Values.ingress.className }}  # nginx for Minikube
  rules:
  {{- range .Values.ingress.hosts }}
    - host: {{ .host }}  # springboot.local
      http:
        paths:
        {{- range .paths }}
          - path: {{ .path }}  # / for all paths
            pathType: {{ .pathType }}  # Prefix
            backend:
              service:
                name: {{ include "restapiingresshttps.fullname" $ }}
                port:
                  number: {{ $.Values.service.port }}  # 8080
        {{- end }}
  {{- end }}
  tls:
  {{- toYaml .Values.ingress.tls | nindent 4 }}  # Enables HTTPS with Secret
{{- end }}

    Why: 
        Routes springboot.local to the Service on 8080.
        tls—applies HTTPS using restapi-tls Secret.

8. Add templates/tls-secret.yaml
File: restapiingresshttps/templates/tls-secret.yaml
yaml

apiVersion: v1
kind: Secret
metadata:
  name: restapi-tls  # Matches ingress.tls.secretName
  labels:
    {{- include "restapiingresshttps.labels" . | nindent 4 }}
type: kubernetes.io/tls
data:
  tls.crt: {{ .Files.Get "certs/tls.crt" | b64enc }}  # Loads cert from chart root/certs/
  tls.key: {{ .Files.Get "certs/tls.key" | b64enc }}  # Loads key from chart root/certs/

    Why: Stores TLS certs in a Secret for Ingress to use.
    Doubt Answered: 
        Question: "My certs folder is in the root of my chart, and the Secret template is inside the templates/ folder—any issues?"
        Answer: No issues! Helm’s .Files.Get looks for files relative to the chart root (restapiingresshttps/), not the templates/ folder. So, "certs/tls.crt" correctly finds restapiingresshttps/certs/tls.crt, matching our structure (ls shows certs/ in root). This is a standard Helm practice—certs in root, templates in templates/.

9. Update /etc/hosts
bash

minikube ip  # Get IP, e.g., 192.168.49.2
echo "192.168.49.2 springboot.local" | sudo tee -a /etc/hosts

    Why: Maps springboot.local to Minikube’s IP—resolves locally for HTTPS testing.

10. Enable Minikube Ingress
bash

minikube addons enable ingress
kubectl get pods -n ingress-nginx  # Verify controller is running

    Why: Runs NGINX Ingress controller to handle HTTPS routing.

11. Install the Chart
bash

cd ~/Desktop/code/k8s/helm/simple-rest-api
helm install restapiingresshttps ./restapiingresshttps

    Why: Deploys the app, Secret, and HTTPS Ingress.

12. Check Pods, Services, Ingress, and Secrets
bash

kubectl get pods,svc,ing,secret

    Expected Output:

    NAME                              READY   STATUS    RESTARTS   AGE
    pod/restapiingresshttps-...       1/1     Running   0          Xs

    NAME                    TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
    service/restapiingresshttps  ClusterIP   10.x.x.x     <none>        8080/TCP   Xs

    NAME                        CLASS   HOSTS             ADDRESS       PORTS     AGE
    ingress/restapiingresshttps  nginx   springboot.local  192.168.49.2  80, 443   Xs

    NAME                    TYPE                DATA   AGE
    secret/restapi-tls      kubernetes.io/tls   2      Xs

    Why: Confirms pod, Service, Ingress (with HTTPS on 443), and Secret are up.

13. Call the HTTPS URL
bash

curl --insecure https://springboot.local/helloworld

    Expected Output: "helloworld"
    Why: 
        --insecure—bypasses self-signed cert warning (for testing).
        Verifies HTTPS routing to the REST API.

14. Delete the Resources
bash

helm uninstall restapiingresshttps
kubectl get all,secret  # Should only show defaults (e.g., kubernetes service)

    Why: Cleans up all resources—leaves a fresh slate.

Notes

    HTTPS: Uses self-signed certs in restapi-tls Secret—production would use a trusted CA or cert-manager.
    Port: 8080—matches Spring Boot internally; Ingress exposes 443 externally.
    Certs Location: certs/ in chart root is intentional—Helm’s .Files.Get expects this, not templates/.
