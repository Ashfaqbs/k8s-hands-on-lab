# From `values.yaml` to a Rendered Manifest — A Walkthrough

This is a companion to [Readme.md](./Readme.md) (what/why Helm) and
[helm-template-and-multi-env-values.md](./helm-template-and-multi-env-values.md) (flags and
validation). This doc answers a narrower question: **when you run `helm template` or `helm
install`, how does each field in `values.yaml` actually end up in the final Deployment /
Service / Ingress / HPA manifest?**

Every example below is real output from running `helm template` against
[`helm/sb/sb-rest-api/sb-rest-api-chart`](./sb/sb-rest-api/sb-rest-api-chart) — a standard
`helm create`-scaffolded chart — nothing here is hand-typed/fabricated output.

## How `values.yaml` Feeds the Templates

The mechanism is always the same: a template file under `templates/` references
`.Values.<key>`, and Helm substitutes whatever that key resolves to after merging
`values.yaml` with any `-f` overrides and `--set` flags. Walking through the objects you'll
touch in almost every real chart:

### Deployment

`templates/deployment.yaml` (relevant lines):
```yaml
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  ...
    containers:
      - name: {{ .Chart.Name }}
        image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
        imagePullPolicy: {{ .Values.image.pullPolicy }}
        livenessProbe:
          {{- toYaml .Values.livenessProbe | nindent 12 }}
        readinessProbe:
          {{- toYaml .Values.readinessProbe | nindent 12 }}
        resources:
          {{- toYaml .Values.resources | nindent 12 }}
```

`values.yaml` defaults:
```yaml
replicaCount: 3
image:
  repository: darksharkash/simplerestapisb-k8s
  tag: "latest"
resources: {}
```

Rendered with defaults only (`helm template sb-rest-api .`):
```yaml
spec:
  replicas: 3
  ...
    - name: sb-rest-api-chart
      image: "darksharkash/simplerestapisb-k8s:latest"
      imagePullPolicy: IfNotPresent
      livenessProbe:
        null
      readinessProbe:
        null
      resources:
        {}
```

**Gotcha, seen in real output above:** `livenessProbe`/`readinessProbe` are commented out in
`values.yaml`, so `.Values.livenessProbe` is `nil`. This template does `toYaml
.Values.livenessProbe` with no guard, so a `nil` value renders as the literal YAML `null` —
`livenessProbe:\n  null` — which is a broken probe, not "no probe". The
[secrets-tls-demo-app chart](../code/java/k8s-with-springboot/secrets-tls-demo-app/helm/demo-app)
wraps the same field as `{{- with .Values.livenessProbe }} livenessProbe: {{- toYaml . |
nindent 12 }} {{- end }}` instead — `with` skips the block entirely when the value is empty,
so an unset probe means "no probe block at all" rather than a `null` one. Prefer `with` over a
bare `toYaml` for any optional object-shaped value.

### Service

`templates/service.yaml`:
```yaml
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: http
```

`values.yaml`: `service: { type: NodePort, port: 8080 }` → renders directly as `type:
NodePort` / `port: 8080`. The simplest possible case — one value, one field, no branching.

### Ingress

`templates/ingress.yaml` is entirely gated by `{{- if .Values.ingress.enabled -}}` — when
`ingress.enabled: false` (the chart's default), **the whole file renders to nothing at all**,
not an empty/disabled resource. This is the standard Helm idiom for optional resources: wrap
the entire template body in an `if`, don't try to render a "disabled" version of the object.

When enabled, it loops over `.Values.ingress.hosts` (a list) with `{{- range .Values.ingress.hosts }}`
so you can define multiple hostnames/paths from a single values block — see the
[dev/stg/prod section](#applying-helm-template-across-dev--stg--prod) below for a filled-in
example.

### HPA (HorizontalPodAutoscaler)

`templates/hpa.yaml` is also fully gated: `{{- if .Values.autoscaling.enabled }}`. This is
the same pattern as Ingress, but it interacts with the Deployment template in a way that's
easy to miss:

```yaml
# deployment.yaml
{{- if not .Values.autoscaling.enabled }}
replicas: {{ .Values.replicaCount }}
{{- end }}
```

**`replicaCount` and `autoscaling.enabled` are mutually exclusive by design.** When
`autoscaling.enabled: true`, the Deployment's `replicas` field is omitted from the rendered
manifest entirely — not set to 0, not set to `minReplicas`, just absent. See the production
example below where this bites you if you're not paying attention.

## Applying `helm template` Across dev / stg / prod

Three real override files now exist in this chart:
[`values-dev.yaml`](./sb/sb-rest-api/sb-rest-api-chart/values-dev.yaml),
[`values-stg.yaml`](./sb/sb-rest-api/sb-rest-api-chart/values-stg.yaml),
[`values-prd.yaml`](./sb/sb-rest-api/sb-rest-api-chart/values-prd.yaml). Each is a small
override on top of `values.yaml` — not a full copy. Run them exactly like this (from inside
`sb-rest-api-chart/`):

```bash
helm template sb-rest-api . -f values-dev.yaml
helm template sb-rest-api . -f values-stg.yaml
helm template sb-rest-api . -f values-prd.yaml
```

### dev

`values-dev.yaml` sets `replicaCount: 1`, `image.tag: dev-latest`, `resources: {}`,
`ingress.enabled: false`. Real rendered Deployment:

```yaml
spec:
  replicas: 1
  ...
    - name: sb-rest-api-chart
      image: "darksharkash/simplerestapisb-k8s:dev-latest"
      livenessProbe:
        null      # still unset, still the same gotcha from above — dev doesn't override it
      readinessProbe:
        null
      resources:
        {}
```
No Ingress is rendered at all (`ingress.enabled` stays `false`, inherited from the chart's
own default). This is the lightest possible environment: one pod, no resource limits, no
external access — matches what you actually want for a local dev loop.

### stg

`values-stg.yaml` adds real `resources`, real `livenessProbe`/`readinessProbe`, 2 replicas,
and turns Ingress on with a `stg.internal` host. Real rendered Deployment + Ingress:

```yaml
spec:
  replicas: 2
  ...
    - name: sb-rest-api-chart
      image: "darksharkash/simplerestapisb-k8s:stg"
      livenessProbe:
        httpGet: { path: /actuator/health/liveness, port: http }
        initialDelaySeconds: 20
        periodSeconds: 10
      readinessProbe:
        httpGet: { path: /actuator/health/readiness, port: http }
        initialDelaySeconds: 10
        periodSeconds: 10
      resources:
        limits: { cpu: 250m, memory: 256Mi }
        requests: { cpu: 100m, memory: 128Mi }
---
apiVersion: networking.k8s.io/v1
kind: Ingress
spec:
  ingressClassName: nginx
  rules:
    - host: "sb-rest-api-stg.internal"
      http:
        paths:
          - path: /
            backend: { service: { name: sb-rest-api-sb-rest-api-chart, port: { number: 8080 } } }
```
This is the first environment where the probes actually mean something (the chart's own
default renders them as broken `null` blocks, remember) — staging is where you'd catch a
wrong health-check path before it reaches prod.

### prd

`values-prd.yaml` sets `replicaCount: 3` **and** `autoscaling.enabled: true`. Real rendered
Deployment — read the `spec:` block closely:

```yaml
spec:
  selector:
    matchLabels: { app.kubernetes.io/name: sb-rest-api-chart, app.kubernetes.io/instance: sb-rest-api }
  template:
    ...
      livenessProbe:
        httpGet: { path: /actuator/health/liveness, port: http }
        initialDelaySeconds: 30
      resources:
        limits: { cpu: 500m, memory: 512Mi }
        requests: { cpu: 250m, memory: 256Mi }
```

**Notice there is no `replicas:` line at all.** `replicaCount: 3` in `values-prd.yaml` was
silently ignored, exactly as flagged in the [HPA section](#hpa-horizontalpodautoscaler)
above, because `autoscaling.enabled: true` makes the template skip `replicas` entirely. The
real replica count in prod comes from the rendered HPA instead:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: sb-rest-api-sb-rest-api-chart }
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource: { name: cpu, target: { type: Utilization, averageUtilization: 70 } }
```

`minReplicas: 3` is what actually puts the Deployment at 3 pods on first sync (a brand-new
Deployment with no `replicas` field defaults to 1 replica from the K8s API itself, then the
HPA controller reconciles it up to `minReplicas` shortly after) — not the `replicaCount: 3`
you set in the values file. **This is exactly the kind of thing `helm template` is for:**
rendering locally and reading the output caught this before an `helm upgrade` shipped a
Deployment to production with a `replicaCount` that does nothing.

Ingress in prod also picks up a real TLS block and a `cert-manager.io/cluster-issuer`
annotation instead of staging's plain HTTP host — see
[production-hardening.md](../code/java/k8s-with-springboot/secrets-tls-demo-app/production-hardening.md)
for what issuing a *real* (non-self-signed) certificate for that annotation to work with
actually involves.

### Comparing all three at once

Since `helm template` is deterministic and pure-local, a plain `diff` between any two
renders tells you exactly what changed — the fastest sanity check before promoting:

```bash
diff <(helm template sb-rest-api . -f values-stg.yaml) \
     <(helm template sb-rest-api . -f values-prd.yaml)
```
