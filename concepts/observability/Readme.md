# **Kubernetes Observability: Prometheus & Grafana**

### **Overview**
This repo's HPA docs already use `metrics-server` — a lightweight, in-memory metrics pipeline that powers `kubectl top` and the HPA's scaling decisions, but keeps **no history**. Ask it "what was CPU usage 10 minutes ago" and it has no answer.

Real observability needs three things `metrics-server` doesn't give you:
1. **Time-series storage** — metrics history, not just a live snapshot.
2. **Custom application metrics** — request counts, error rates, queue depth, business metrics — not just CPU/memory.
3. **Dashboards and alerting** — a way to visualize trends and get paged when something's wrong.

**Prometheus** solves (1) and (2): it periodically *scrapes* an HTTP `/metrics` endpoint from every target it's configured for (or auto-discovers via K8s service discovery), and stores the results as time series. **Grafana** solves (3): it queries Prometheus (and other data sources) and renders dashboards.

### **How scraping works**
Prometheus is pull-based, not push-based. Any app that wants to be monitored exposes a `/metrics` HTTP endpoint in Prometheus's plain-text exposition format (client libraries exist for every major language — Spring Boot apps get this almost for free via `micrometer` + `spring-boot-starter-actuator`, exposing `/actuator/prometheus`). Prometheus is told (or discovers) where that endpoint lives and scrapes it on an interval (commonly 15-30s).

### **Installing the stack via Helm**
This repo already has a whole Helm workflow (see `../../helm/`); the standard way to get Prometheus + Grafana + Alertmanager + sane K8s-specific dashboards in one shot is the community `kube-prometheus-stack` chart:
```sh
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install kube-prom-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace
```

This installs, among other things:
- A Prometheus server (via the **Prometheus Operator**, which itself is a CRD-based controller — see `../crd-operators`)
- Grafana, pre-loaded with a set of Kubernetes dashboards
- `kube-state-metrics` (exposes cluster object state — pod counts, deployment status, etc. — as Prometheus metrics)
- Alertmanager

Access Grafana locally (no Ingress needed for a quick look):
```sh
kubectl port-forward -n monitoring svc/kube-prom-stack-grafana 3000:80
# then open http://localhost:3000  (default user: admin, password from the 'kube-prom-stack-grafana' Secret)
kubectl get secret -n monitoring kube-prom-stack-grafana -o jsonpath="{.data.admin-password}" | base64 -d
```

### **Telling Prometheus to scrape your own app: `ServiceMonitor`**
The Prometheus Operator (installed above) doesn't want you hand-editing Prometheus's scrape config. Instead you declare a `ServiceMonitor` **custom resource** (see `../crd-operators` for what a CRD actually is) that says "scrape any Service matching these labels":
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: sb-rest-api-monitor
  namespace: dev
  labels:
    release: kube-prom-stack   # must match the Prometheus Operator's serviceMonitorSelector
spec:
  selector:
    matchLabels:
      app: sb-rest-api          # matches the Service in front of your Spring Boot app
  endpoints:
    - port: http                # the named port on that Service
      path: /actuator/prometheus
      interval: 30s
```
Once applied, the Prometheus Operator notices it, reconfigures Prometheus automatically, and your app's custom metrics start flowing in — no manual Prometheus config editing, no restart.

### **Why this belongs next to the HPA docs**
`HorizontalPodAutoscaler` can scale on more than CPU/memory once metrics-server's limits are outgrown — via the **custom metrics API**, an HPA can scale on a Prometheus-sourced metric (e.g. requests-per-second, queue depth) using the `prometheus-adapter` chart, which translates Prometheus queries into the K8s custom-metrics API that HPA already knows how to read. That's the natural next step after this doc if you want to push the HPA examples further.

### **Gotchas**
- `kube-prometheus-stack` is resource-hungry for a local Minikube VM — bump `minikube start --cpus=4 --memory=8192` or trim it down with `--set prometheus.prometheusSpec.resources...` overrides if things get slow.
- Grafana dashboards and Prometheus scrape targets are both label-driven — a `ServiceMonitor` that doesn't carry the `release: <helm-release-name>` label (or whatever `serviceMonitorSelector` the chart was installed with) is silently ignored. This is the single most common "why isn't my metric showing up" issue.
- This is intentionally the introductory layer — production setups add remote-write to long-term storage (Thanos/Mimir/Cortex), since Prometheus's local TSDB isn't meant for years of retention.

### **Cleanup**
```sh
kubectl delete servicemonitor sb-rest-api-monitor -n dev
helm uninstall kube-prom-stack -n monitoring
kubectl delete namespace monitoring
```
