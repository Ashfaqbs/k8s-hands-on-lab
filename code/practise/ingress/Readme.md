**Kubernetes Ingress: Our Traffic Cop Explained**

Hey there! This doc’s our go-to guide for Kubernetes Ingress—think of it as the bouncer at the club, deciding who gets in and where they go. We’ll break it down step-by-step, starting with every key we’ll see in an Ingress YAML, then walk through two setups: one where our app lives at https://app.example.com (simple base path), and another where it’s https://app.example.com/api with subpaths like /health and /users. I’ll explain why we set things up, what happens when we hit the URL, and when it’s clutch to tweak stuff. Plus, we’ll wrap up with some do’s and don’ts using a real example. Ready? Let’s dive in!

---

**The Big Picture: What’s Ingress?**

- **What:** Ingress is Kubernetes’ way to handle HTTP/HTTPS traffic from outside our cluster—like a traffic cop directing cars (requests) to the right parking lot (services).
- **Why:** Without it, our pods are just floating around—Ingress gives them a public address (e.g., app.example.com) and rules to route traffic.
- **When:** Use it when we’ve got apps (like our Spring Boot backend or React frontend) that need to talk to the world.

---

**Every Key in Ingress: The Full Reference**

Here’s every piece of the Ingress YAML puzzle—think of this as our cheat sheet. We’ll explain what it does, why it’s there, and what happens if we mess with it.

1. **apiVersion**
   - **What:** Tells Kubernetes which version of the Ingress rules we’re using—like picking the right rulebook.
   - **Value:** networking.k8s.io/v1 (current as of 2025).
   - **Why:** Kubernetes needs to know how to read our YAML—older versions (like extensions/v1beta1) are outdated.
   - **What Happens:** Set it to networking.k8s.io/v1, and it works. Wrong version? Deployment fails—Kubernetes goes, “Huh?”
   - **When:** Always set this—first line, every time.

2. **kind**
   - **What:** Says this is an Ingress resource—not a pod or service.
   - **Value:** Ingress.
   - **Why:** Kubernetes has tons of resource types—this tags it as traffic-routing.
   - **What Happens:** Set to Ingress, it’s good. Anything else (like Pod)? Fails hard—wrong resource.
   - **When:** Always Ingress—no exceptions.

3. **metadata**
   - **What:** Info about our Ingress—like its name and where it lives.
   - **Subkeys:**
     - name: Nickname (e.g., app-ingress).
     - namespace: Which room in the cluster (e.g., default).
   - **Why:** Kubernetes needs a unique ID and a spot to put it—think of it like naming a file and picking a folder.
   - **What Happens:**
     - name: app-ingress—shows up as app-ingress in kubectl get ing.
     - namespace: default—lives in default. Skip it? Defaults there anyway.
     - No name? Fails—Kubernetes needs a label.
   - **When:** Always name it; namespace if we’re splitting apps (e.g., dev, prod).

4. **metadata.annotations**
   - **What:** Extra instructions for the Ingress controller (like nginx)—little tweaks to bend the rules.
     - Examples:
       - nginx.ingress.kubernetes.io/rewrite-target: /: Rewrites URLs.
       - nginx.ingress.kubernetes.io/ssl-redirect: "true": Forces HTTPS.
   - **Why:** Default behavior might not cut it—annotations let we customize (e.g., force HTTPS for security).
   - **What Happens:**
     - No annotations: Basic routing, HTTP allowed.
     - ssl-redirect: "true": http:// → https://—browser loves it.
     - rewrite-target: /: Strips paths—/api/foo becomes /foo at the service.
   - **When:** Add when we need special sauce—like HTTPS or URL tricks.

5. **spec**
   - **What:** The meat of Ingress—rules for routing traffic.
   - **Why:** Without this, Ingress is just a name—no directions for traffic.
   - **Subkeys:** Below—each gets its own breakdown.

---

**Scenario 1: Base Path (host:port)**

- **Goal:** App at https://app.example.com—no fancy subpaths, just the root.

YAML:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  namespace: default
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  rules:
  - host: app.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: app-service
            port:
              number: 80
  tls:
  - hosts:
    - app.example.com
    secretName: app-tls
```

Key-by-Key Breakdown:

- **apiVersion:** Set to networking.k8s.io/v1—this ensures the Ingress is up-to-date and works properly.
- **kind:** Ingress, telling Kubernetes to route traffic.
- **metadata.name:** Names the Ingress—e.g., app-ingress.
- **annotations:** ssl-redirect: "true" forces HTTPS.
- **spec.ingressClassName:** nginx picks Nginx as the controller.
- **spec.rules.host:** Limits traffic to app.example.com.
- **spec.rules.http.paths.path:** Catches all traffic under /.
- **spec.tls:** Enables HTTPS using the app-tls certificate.

**Calling the URL:**
- https://app.example.com: Goes to app-service:80/—app loads.
- https://app.example.com/foo: app-service:80/foo—works if app handles it.
- http://app.example.com: Redirects to HTTPS—annotation kicks in.
- https://wrong.com: 404—host mismatch.

---

**Scenario 2: API Path (host:port/api)**

- **Goal:** API at https://app.example.com/api with /health and /users.

YAML:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  namespace: default
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  rules:
  - host: app.example.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: app-service
            port:
              number: 80
  tls:
  - hosts:
    - app.example.com
    secretName: app-tls
```

Key-by-Key Breakdown:

- **apiVersion & kind:** Same deal—modern Ingress.
- **metadata.name:** Keeps it trackable.
- **metadata.namespace:** Stays simple in default.
- **annotations:** ssl-redirect: "true" for API security.
- **spec.ingressClassName:** Nginx handles API traffic.
- **spec.rules.host:** Locks traffic to app.example.com.
- **spec.rules.http.paths.path:** Targets API traffic.
- **spec.rules.http.paths.pathType:** Prefix, allowing subpaths like /api/users.
- **backend.service.name:** Links to API pods.
- **backend.service.port.number:** API listens here.
- **spec.tls:** Enables HTTPS for the API.

**Calling the URL:**
- https://app.example.com/api/health: app-service:80/api/health—metrics show.
- https://app.example.com/api/users: app-service:80/api/users—data flows.
- https://app.example.com/: 404—no / path.
- http://app.example.com/api: Redirects to HTTPS—secure.

---

**Good and Bad Practices: Real Example**

Let’s take https://shop.example.com—an e-commerce app.

**Good Practices:**
- **TLS Always:** Secure shopping with HTTPS.
- **Specific Hosts:** Only allow traffic to shop.example.com.
- **Path Precision:** Use Prefix for subpaths like /cart.
- **Annotations for HTTPS:** Enforce HTTPS.

**Bad Practices:**
- **Rewrite Overuse:** Don’t rewrite paths unless needed.
- **No Namespace:** Use namespaces to avoid clashes in big clusters.
- **Wrong Port:** Double-check our service port to avoid 503 errors.

---

**Extra Tips:**
- **Multiple Paths:** Split paths to different services for flexibility.
- **Default Backend:** Add a fallback service—404 becomes a friendly page.
- **CORS:** Enable CORS for cross-domain calls.

---
