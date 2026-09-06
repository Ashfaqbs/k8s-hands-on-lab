# Helm Commands Cheat Sheet

### **1. Repo Management**
```bash
helm repo add <name> <url>          # Register a chart repository
helm repo update                    # Refresh local cache of all added repos
helm repo list                      # List registered repos
helm search repo <keyword>          # Search charts across added repos
helm search hub <keyword>           # Search Artifact Hub
```

---

### **2. Chart Scaffolding & Packaging**
```bash
helm create <chart-name>            # Scaffold a new chart with standard structure
helm package <chart-path>           # Package a chart into a .tgz for distribution
helm lint <chart-path>              # Static-check a chart's structure/values (no cluster needed)
helm dependency update <chart-path> # Pull subchart dependencies listed in Chart.yaml
```

---

### **3. Installing & Upgrading Releases**
```bash
helm install <release-name> <chart-path>                 # Install using default values.yaml
helm install <release-name> <chart-path> -f values-dev.yaml   # Install with an override file
helm install <release-name> <chart-path> --set replicaCount=3 # One-off value override
helm upgrade <release-name> <chart-path> -f values-dev.yaml   # Upgrade an existing release
helm upgrade --install <release-name> <chart-path> -f values-dev.yaml
                                                            # Upgrade if it exists, install if it doesn't
                                                            # (the pattern almost every CI/CD pipeline uses)
```

---

### **4. Dry Runs & Local Validation (see also: helm-template-and-multi-env-values.md)**
```bash
helm template <release-name> <chart-path> -f values-dev.yaml   # Render manifests locally, no cluster contact
helm install <release-name> <chart-path> --dry-run --debug     # Server-side validation, nothing actually created
helm lint <chart-path>                                          # Chart-level static validation
```

---

### **5. Inspecting Releases**
```bash
helm list                           # List releases in current namespace
helm list -A                        # List releases across all namespaces
helm status <release-name>          # Show status of a release
helm get values <release-name>      # Show the values actually used by a release
helm get values <release-name> -a   # Include the chart's computed defaults, not just overrides
helm get manifest <release-name>    # Show the exact K8s manifests currently deployed by a release
helm get notes <release-name>       # Re-print the chart's post-install NOTES.txt
```

---

### **6. Rollback & History**
```bash
helm history <release-name>                 # List all revisions of a release
helm rollback <release-name> <revision>     # Roll back to a specific revision
helm rollback <release-name>                # Roll back to the previous revision
```

---

### **7. Uninstalling**
```bash
helm uninstall <release-name>               # Remove a release (deletes all its resources)
helm uninstall <release-name> --keep-history # Remove resources but keep revision history for inspection
```

---

### **Additional Useful Flags (apply to install/upgrade/template)**
```bash
-f <file>, --values <file>   # Layer a values file on top of values.yaml (repeatable, last one wins)
--set key=value              # One-off override, highest precedence, overrides every -f file
-n <namespace>                # Target namespace
--dry-run                    # Simulate the action without applying it
--debug                      # Verbose output, including the fully computed values
--atomic                     # On upgrade, auto-rollback if the upgrade fails
--wait                       # Block until all resources are in a Ready state
```
