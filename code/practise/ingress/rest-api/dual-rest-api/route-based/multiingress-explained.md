Alright, man! Let’s break down this springboot-multi-ingress.yaml file line by line in simple, layman’s terms, tailored to our dual REST API project. I’ll explain what each part does, why we chose it, and how it works under the hood when you call http://springboot.local/api1/helloworld or /api2/helloworld. We’ll cover the regex, annotations, pathType, and how it fixes our earlier 404 issue. Ready? Here we go!
The Ingress File Explained
yaml

apiVersion: networking.k8s.io/v1

    What: Tells Kubernetes which version of the Ingress “recipe” we’re using.
    Why: v1 is the stable, modern version as of 2025—keeps us up-to-date.
    For Us: Just a standard header so Kubernetes knows how to read this file.

yaml

kind: Ingress

    What: Says this is an Ingress resource—like a traffic cop for routing web requests.
    Why: We need it to direct outside calls (e.g., springboot.local) to our two APIs inside Kubernetes.
    For Us: Sets up rules to split traffic between api1 and api2.

yaml

metadata:
  name: springboot-multi-ingress
  namespace: default

    What: Gives our Ingress a name and a “home” in the cluster.
        name: springboot-multi-ingress: How we’ll refer to it (e.g., kubectl get ingress springboot-multi-ingress).
        namespace: default: Where it lives—default is our main workspace.
    Why: Keeps it organized and unique in the default namespace where our APIs (api1-service, api2-service) are.
    For Us: Links this traffic cop to our project’s stuff.

yaml

annotations:
  nginx.ingress.kubernetes.io/rewrite-target: /$2

    What: A special note for NGINX (our traffic cop) saying, “Hey, tweak the URL before passing it to the APIs.”
        Annotations: Little instructions stuck on the Ingress for extra behavior.
        rewrite-target: /$2: Tells NGINX to rewrite the URL, keeping only the part we capture later (explained with regex).
    Why: Our Spring Boot app expects /helloworld, but we’re calling /api1/helloworld. This strips off /api1 so the app gets what it wants.
    For Us: Fixes the 404 issue—without this, the app saw /api1/helloworld, didn’t recognize it, and said “Nope, not found.”
    Under the Hood: When you call /api1/helloworld, NGINX chops it to /helloworld before handing it to the pod. Same for /api2.

yaml

spec:

    What: The “spec” is the main plan—where we write the actual traffic rules.
    Why: Everything below this is the blueprint for how requests flow to our APIs.
    For Us: Sets the stage for routing to api1-service and api2-service.

yaml

  ingressClassName: nginx

    What: Tells Kubernetes which traffic cop to use—here, it’s NGINX.
    Why: We’re using the NGINX Ingress Controller (from minikube addons enable ingress) to handle our rules. This links our Ingress to it.
    For Us: Ensures NGINX (not some other controller) reads this file and directs traffic.

yaml

  rules:

    What: The list of traffic rules—like a map saying “this URL goes here.”
    Why: We need to tell NGINX how to split calls between our two APIs.
    For Us: Sets up the /api1 and /api2 split under one hostname.

yaml

  - host: "springboot.local"

    What: Says all rules below apply to requests for springboot.local.
    Why: We picked one hostname for both APIs, using paths (/api1, /api2) to tell them apart. Keeps it simple with one /etc/hosts entry.
    For Us: When you type springboot.local/api1/helloworld, NGINX knows to handle it. We mapped this to 192.168.49.2 in /etc/hosts.

yaml

    http:

    What: Says these rules are for HTTP traffic (web requests).
    Why: Our APIs use HTTP (not HTTPS yet), so this section defines how web calls work.
    For Us: Matches our curl http://springboot.local/api1/helloworld calls.

yaml

      paths:

    What: A list of paths (URL pieces) and where they go—like road signs.
    Why: We’re splitting traffic based on /api1 or /api2, so we need separate paths.
    For Us: Defines how NGINX picks between api1-service and api2-service.

yaml

      - path: /api1(/|$)(.*)

    What: A fancy rule (regex) for matching URLs starting with /api1.
        /api1: The base path we want to catch.
        (/|$): Means “either a slash (/) or the end of the URL” (e.g., /api1/ or just /api1).
        (.*): Grabs everything after that (e.g., helloworld in /api1/helloworld).
    Why: We need to catch /api1/helloworld (or /api1/anything) and rewrite it. The regex lets us split the URL into “prefix” (/api1) and “rest” (helloworld).
    For Us: Matches calls like /api1/helloworld, and the (.*) part is what we keep (as $2) for the app.

yaml

        pathType: ImplementationSpecific

    What: Tells Kubernetes, “This path is special—let NGINX figure it out.”
    Why: 
        Originally Prefix, which is for simple paths (e.g., /api1), but our regex (/api1(/|$)(.*)) is too fancy for that.
        ImplementationSpecific hands it to NGINX, which loves regex and rewrites.
    Why Changed: Prefix threw a warning (cannot be used with pathType Prefix) because it doesn’t support regex. ImplementationSpecific says “NGINX, you got this,” and kills the warning.
    For Us: Lets our regex work smoothly without Kubernetes complaining.

yaml

        backend:
          service:
            name: api1-service
            port:
              number: 80

    What: Points traffic matching /api1 to api1-service on port 80.
        name: api1-service: The Service for our first API’s pods.
        port: 80: The Service’s port, which forwards to pod’s 8080.
    Why: Connects this path to the right API instance (api1-deployment pods).
    For Us: When /api1/helloworld hits, NGINX sends it to api1-service, which picks an api1 pod.

yaml

      - path: /api2(/|$)(.*)

    What: Same deal, but for /api2—matches URLs like /api2/helloworld.
    Why: Gives our second API its own path to catch traffic.
    For Us: Separates /api2 calls from /api1, routing them differently.

yaml

        pathType: ImplementationSpecific

    What: Same as above—lets NGINX handle the /api2 regex.
    Why: Consistency with /api1, avoids warnings, and supports our rewrite.
    For Us: Keeps NGINX in charge of the fancy path logic.

yaml

        backend:
          service:
            name: api2-service
            port:
              number: 80

    What: Sends /api2 traffic to api2-service on port 80.
    Why: Links this path to our second API’s pods (api2-deployment).
    For Us: Ensures /api2/helloworld hits the right instance.

Under the Hood: What Happens When You Call the API
Calling http://springboot.local/api1/helloworld

    Your System: 
        Browser/curl sees springboot.local, checks /etc/hosts, finds 192.168.49.2.
        Sends request to 192.168.49.2:80 (Minikube’s IP).
    NGINX Ingress Controller: 
        Listens on 192.168.49.2:80, gets springboot.local/api1/helloworld.
        Checks springboot-multi-ingress rules:
            host: springboot.local matches.
            path: /api1(/|$)(.*) matches:
                /api1 = prefix.
                helloworld = (.*) (captured as $2).
    Rewrite Magic: 
        Annotation rewrite-target: /$2 kicks in.
        /api1/helloworld → /$2 → /helloworld (strips /api1).
        NGINX sends /helloworld to api1-service:80.
    Service to Pod: 
        api1-service (ClusterIP, e.g., 10.96.x.x) forwards to a pod labeled app: api1 on port 8080.
        Pod gets /helloworld.
    Spring Boot: 
        App knows /helloworld (its endpoint), replies with "Hello World".
        Response goes back: pod → Service → NGINX → you.

Same for /api2/helloworld

    Matches /api2(/|$)(.*), rewrites to /helloworld, hits api2-service, same response.

Why No 404 Now?

    Before: NGINX sent /api1/helloworld to the pod, but Spring Boot only knew /helloworld—mismatch = 404.
    Now: Rewrite fixes it—pod sees /helloworld, matches its code, no issue.

Why These Choices?

    Regex Paths: /api1(/|$)(.*) lets us grab the useful part (helloworld) and ditch the prefix. Needed for rewrite to work with our app’s fixed endpoint.
    Annotation: rewrite-target is like a URL barber—trims what the app doesn’t need. Without it, we’d need to change the app (not an option here).
    ImplementationSpecific: Lets NGINX do its regex thing without Kubernetes freaking out. Prefix was too basic for our trick.

Layman’s Take

    Think of It: Ingress is a bouncer at a club (springboot.local). You say /api1/helloworld, it checks the list (rules), rewrites your “ticket” to /helloworld, and sends you to the right room (api1-service). Same for /api2.
    Hard Bit: The regex and rewrite are like secret codes—NGINX cracks them to make the app happy.