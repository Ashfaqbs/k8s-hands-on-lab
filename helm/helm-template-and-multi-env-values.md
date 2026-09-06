# `helm template` and Multi-Environment `values` Files

This picks up where [Readme.md](./Readme.md) leaves off ("segregating environment-specific
configuration") and answers the practical question: once you have `values-dev.yaml`,
`values-stg.yaml`, `values-prd.yaml`, how do you actually validate what Helm is going to
produce for each environment *before* touching a cluster?

## What is `helm template`?

`helm template <release-name> <chart-path>` renders a chart plus its values into the final
Kubernetes YAML manifests, entirely on your machine. It does **not**:
- contact the Kubernetes API server
- check whether the cluster even exists
- create, update, or delete anything

It's pure client-side templating: Chart.yaml + values.yaml (+ any `-f` overrides) + Go
templates in `templates/*.yaml` → plain YAML on stdout. That makes it the fastest possible
feedback loop for "did I break the template" or "what will this actually deploy."

## Why you need it (not just "nice to have")

- **Catch templating errors before they hit a cluster.** A typo in a `{{ .Values.xxx }}`
  reference, bad indentation, or a missing required value fails immediately and locally,
  instead of failing mid-upgrade against a real environment.
- **Review the exact manifest before it's applied.** Especially useful before promoting a
  change from dev to staging to prod — you can read precisely what changed.
- **Diff environments against each other.** See [Diffing environments](#diffing-environments-before-you-promote) below.
- **No side effects.** Safe to run repeatedly, in CI, on a laptop with no cluster access at
  all — it needs the chart directory and nothing else.

## The multi-env values pattern

Keep `values.yaml` as the chart's defaults, and keep each environment file as a small
**override**, not a full copy of every key:

```
sb-rest-api-chart/
  Chart.yaml
  values.yaml            # defaults (safe for local/dev use)
  values-dev.yaml         # only the keys that differ for dev
  values-stg.yaml         # only the keys that differ for staging
  values-prd.yaml         # only the keys that differ for prod
  templates/
```

Example, using the chart already in this repo
([`helm/sb/sb-rest-api/sb-rest-api-chart`](./sb/sb-rest-api/sb-rest-api-chart)):

```yaml
# values-dev.yaml
replicaCount: 1
image:
  tag: "dev-latest"
resources: {}
ingress:
  enabled: false
```

```yaml
# values-stg.yaml
replicaCount: 2
image:
  tag: "stg"
resources:
  limits: { cpu: 250m, memory: 256Mi }
  requests: { cpu: 100m, memory: 128Mi }
ingress:
  enabled: true
  hosts:
    - host: myapp-stg.internal
      paths: [{ path: /, pathType: ImplementationSpecific }]
```

```yaml
# values-prd.yaml
replicaCount: 3
image:
  tag: "1.4.2"          # pin a real version in prod, never "latest"
resources:
  limits: { cpu: 500m, memory: 512Mi }
  requests: { cpu: 250m, memory: 256Mi }
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
```

**Never put real secrets (DB passwords, API keys, tokens) in a `values-*.yaml` file** — those
belong in a Secret sourced from a vault/secrets manager, referenced by name from values.
See the [RBAC](../concepts/rbac/Readme.md) and cluster security docs for the access-control
side of that.

## Rendering per environment

```bash
cd helm/sb/sb-rest-api/sb-rest-api-chart

# Dev: defaults + dev override
helm template sb-rest-api . -f values-dev.yaml

# Staging: defaults + staging override
helm template sb-rest-api . -f values-stg.yaml

# Prod: defaults + prod override
helm template sb-rest-api . -f values-prd.yaml
```

You can also layer more than one `-f` explicitly (useful if you split "common overrides" from
"per-env overrides"):

```bash
helm template sb-rest-api . -f values.yaml -f common-overrides.yaml -f values-prd.yaml
```

**Precedence rule:** later files win over earlier ones, and `--set` wins over every `-f` file.
So `-f values.yaml -f values-prd.yaml` means "start from defaults, then apply prod's
overrides on top" — get the order wrong and you silently apply the wrong environment's
values.

## Key flags and why you'd reach for them

| Flag | What it does | When you need it |
|---|---|---|
| `-f <file>` / `--values <file>` | Layers a values file on top of the chart's `values.yaml`. Repeatable; last one wins on overlapping keys. | Selecting which environment to render (`values-dev.yaml`, `values-stg.yaml`, ...). |
| `--set key=value` | One-off override, highest precedence over every `-f` file. | Quick local experiment ("what if replicaCount was 5") without editing a file. |
| `--show-only templates/deployment.yaml` | Renders only the named template instead of the whole chart. | Large charts with many templates — you only care about one resource right now. |
| `--debug` | Verbose output; also prints the fully computed values Helm resolved after merging all sources. | Figuring out *why* a value came out wrong — shows you the final merged values, not just the rendered YAML. |
| `-n <namespace>` | Sets `.Release.Namespace`, which some templates reference directly. | Any chart whose templates use `{{ .Release.Namespace }}` (e.g. building a fully-qualified Service DNS name). |
| `--output-dir <dir>` | Writes each rendered template to a real file instead of stdout. | Diffing environments on disk, or feeding the output to another tool. |
| `--kube-version` / `--api-versions` | Fakes the target cluster's K8s version / installed API groups. | Charts that branch on `.Capabilities` (e.g. old vs new Ingress API) — lets you render as if targeting a specific cluster version without being connected to one. |

## `helm template` vs `helm lint` vs `--dry-run` — pick the right validation for the job

They check different things and are not interchangeable:

| Tool | Contacts a cluster? | Catches |
|---|---|---|
| `helm lint <chart>` | No | Chart-level structural problems: malformed `Chart.yaml`, missing required values per `values.schema.json`, obviously broken YAML. Doesn't need any `-f` values file at all. |
| `helm template ... -f values-dev.yaml` | No | Templating logic errors (bad references, wrong indentation, missing values causing empty required fields). Does **not** know if the resulting YAML is even valid for your actual cluster's API. |
| `helm template ... \| kubectl apply --dry-run=server -f -` | **Yes** (server-side, nothing persisted) | Everything above, plus real API-server validation: wrong `apiVersion`, CRDs that don't exist in this cluster, admission webhook rejections, schema violations Helm itself doesn't know about. |
| `helm install/upgrade ... --dry-run --debug` | **Yes** (server-side, nothing persisted) | Same server-side checks as the line above, run through Helm's actual install/upgrade path — the closest simulation to a real install without changing anything. |

**Practical rule of thumb:** run `helm lint` and `helm template` locally while you're actively
editing a chart (fast, no cluster needed). Before actually promoting a change, run `helm
template ... | kubectl apply --dry-run=server -f -` (or `helm upgrade --dry-run --debug`)
against the target cluster once — only that step catches problems that depend on what's
really running there.

## Diffing environments before you promote

Since `helm template` is deterministic, plain `diff` tells you exactly what changes between
two environments — a fast sanity check before pushing a change from dev to prod:

```bash
diff <(helm template sb-rest-api . -f values-stg.yaml) \
     <(helm template sb-rest-api . -f values-prd.yaml)
```

If you see a difference you didn't expect (e.g. `resources` accidentally missing from prod),
you've caught it before it ever reached a cluster.

## Gotchas

- **`-f` order matters.** `-f values-dev.yaml -f values-prd.yaml` silently applies prod on top
  of dev — always double check the order in your scripts/CI jobs.
- **`--set` can't express nested lists cleanly.** For anything beyond a flat key or a shallow
  map, use a `-f` file (or `--set-json`) instead of fighting `--set`'s dotted-path syntax.
- **`helm template` does not run hooks.** `pre-install`/`post-install` Jobs defined via Helm
  hooks are not executed (and, depending on chart, may not even render) — only `helm
  install`/`upgrade` actually runs them. Don't assume template output is 100% identical to
  what install does end-to-end.
- **Never commit resolved secrets.** If a values file needs a secret, reference an existing
  K8s Secret / External Secret by name — don't inline the real value even in a
  "just for now" env file.
