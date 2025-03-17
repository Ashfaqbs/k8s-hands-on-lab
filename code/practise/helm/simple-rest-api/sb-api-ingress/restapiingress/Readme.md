Hey boss! Absolutely—let’s document this in a clean README-style format for your new restapiingress chart. I’ll lay it out with steps, the updated files (with "why" comments), and commands—covering creation, updates, /etc/hosts, deployment, testing, and cleanup. It’ll be tight, practical, and ready for your repo or notes. Here we go!
README: Deploying a Spring Boot REST API with Ingress on Minikube
This guide walks through creating a Helm chart (restapiingress) to deploy a Spring Boot REST API (darksharkash/simplerestapisb-k8s) on Minikube, exposing it via Ingress with the hostname springboot.local. No HTTPS yet—that’s next!
Prerequisites

    Minikube: Running locally.
    Helm: Installed.
    kubectl: Configured for Minikube.
    Image: darksharkash/simplerestapisb-k8s:latest (Spring Boot app on port 8080).

Steps
1. Create the Helm Chart
bash

cd ~/Desktop/code/k8s/helm/simple-rest-api
helm create restapiingress

    Why: Generates a Helm chart scaffold with default templates and values.yaml.

2. Update Chart.yaml
File: restapiingress/Chart.yaml
yaml

apiVersion: v2
name: restapiingress
description: A Helm chart for Spring Boot REST API with Ingress
type: application
version: 0.1.0  # Initial version
appVersion: "1.16.0"  # Matches Spring Boot app version

    Why: Customizes the chart name and description for clarity; appVersion aligns with the image.

3. Update values.yaml
File: restapiingress/values.yaml
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
    - host: springboot.local  # Custom hostname for the app
      paths:
        - path: /  # Route all paths (including /helloworld)
          pathType: Prefix  # Match any subpath
  tls: []  # No TLS yet—HTTP only for now

livenessProbe:
  httpGet:
    path: /helloworld  # Endpoint Spring Boot serves
    port: 8080  # Matches app port
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
        service.port: 8080—aligns with Spring Boot’s port for direct routing.
        ClusterIP—Ingress exposes it, no need for NodePort.
        ingress.enabled: true—sets up springboot.local.
        Probes—ensure app health, delayed to avoid startup failures.

4. Update templates/deployment.yaml
File: restapiingress/templates/deployment.yaml
yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "restapiingress.fullname" . }}
  labels:
    {{- include "restapiingress.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "restapiingress.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "restapiingress.selectorLabels" . | nindent 8 }}
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

5. Update templates/service.yaml
File: restapiingress/templates/service.yaml
yaml

apiVersion: v1
kind: Service
metadata:
  name: {{ include "restapiingress.fullname" . }}
  labels:
    {{- include "restapiingress.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}  # ClusterIP from values.yaml
  ports:
    - port: {{ .Values.service.port }}  # 8080
      targetPort: 8080  # Maps to container port
      protocol: TCP
      name: http
  selector:
    {{- include "restapiingress.selectorLabels" . | nindent 4 }}

    Why: 
        port: 8080—exposed by Service.
        targetPort: 8080—routes to container.

6. Add templates/ingress.yaml
File: restapiingress/templates/ingress.yaml
yaml

{{- if .Values.ingress.enabled }}
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "restapiingress.fullname" . }}
  labels:
    {{- include "restapiingress.labels" . | nindent 4 }}
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
                name: {{ include "restapiingress.fullname" $ }}
                port:
                  number: {{ $.Values.service.port }}  # 8080
        {{- end }}
  {{- end }}
  {{- if .Values.ingress.tls }}
  tls:
  {{- toYaml .Values.ingress.tls | nindent 4 }}
  {{- end }}
{{- end }}

    Why: 
        Routes springboot.local to the Service on 8080.
        No TLS yet—HTTP only.

7. Update /etc/hosts
bash

minikube ip  # Get IP, e.g., 192.168.49.2
echo "192.168.49.2 springboot.local" | sudo tee -a /etc/hosts

    Why: Maps springboot.local to Minikube’s IP—resolves locally for testing.

8. Install the Chart
bash

cd ~/Desktop/code/k8s/helm/simple-rest-api
helm install restapiingress ./restapiingress

    Why: Deploys the app with Ingress.

9. Check Pods, Services, and Ingress
bash

kubectl get pods,svc,ing

    Expected Output:

    NAME                              READY   STATUS    RESTARTS   AGE
    pod/restapiingress-...            1/1     Running   0          Xs

    NAME                    TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
    service/restapiingress  ClusterIP   10.x.x.x     <none>        8080/TCP   Xs

    NAME                        CLASS   HOSTS             ADDRESS       PORTS   AGE
    ingress/restapiingress      nginx   springboot.local  192.168.49.2  80      Xs

    Why: Confirms everything’s up—pod running, Service on 8080, Ingress routing springboot.local.

10. Call the URL
bash

curl http://springboot.local/helloworld

    Expected Output: "helloworld"
    Why: Tests Ingress routing to the REST API endpoint.

11. Delete the Resources
bash

helm uninstall restapiingress
kubectl get all  # Should only show kubernetes service

    Why: Cleans up for a fresh slate—removes all deployed resources.

Notes

    Port Choice: 8080 keeps it simple—matches Spring Boot directly. Could use 80 with targetPort: 8080, but unnecessary here.
    Ingress: Minikube’s NGINX controller routes springboot.local—no external IP needed with /etc/hosts.
    Next: Add HTTPS by updating ingress.tls with a TLS Secret.

