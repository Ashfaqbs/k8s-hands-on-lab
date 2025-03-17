## the liveness probe issue 

when installed the chart when the liveness probe was defined like this in values.yaml file 
we actually provide here the actuator link to liveness probe just to say if the app is up or down

# livenessProbe:
#   httpGet:
#     path: /
#     port: http
# readinessProbe:
#   httpGet:
#     path: /
#     port: http

and when pod was fine in logs but was not accesible as due to this 


error when calling the website 

──(ashfaq㉿kali-vm)-[~/…/code/k8s/helm/simple-rest-api]
└─$ curl http://192.168.49.2:31181
curl: (7) Failed to connect to 192.168.49.2 port 31181 after 0 ms: Could not connect to server
                                                                                                 

- the logs were fine 

- Describe command for the pod 

Scheduled, Pulled, Created, Started—pod’s up.

Readiness probe failed: ... connection refused—probe can’t hit 8080.
as code took time to come up atleast 4 secs 
Readiness: http-get http://:8080/helloworld delay=0s timeout=1s period=10s.

delay=0s—probe starts immediately.


                                                                                                 -


## Issue fix 
- changes in probe as 

Readiness: http-get http://:8080/helloworld delay=0s timeout=1s period=10s.

delay=0s—probe starts immediately.

Spring Boot takes ~4-5s to start (per logs).




```

livenessProbe:
  httpGet:
    path: /helloworld
    port: 8080
  initialDelaySeconds: 10  # Wait 10s
  timeoutSeconds: 5
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /helloworld
    port: 8080
  initialDelaySeconds: 10  # Wait 10s
  timeoutSeconds: 5
  periodSeconds: 10

```
changes to this in values.yaml and installed or upgraded and tested (worked fine) 

post successs 

describe command pod 

Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  48s   default-scheduler  Successfully assigned default/simplerestapi-5758b7c8d9-zssrn to minikube
  Normal  Pulled     47s   kubelet            Container image "darksharkash/simplerestapisb-k8s:latest" already present on machine
  Normal  Created    47s   kubelet            Created container: simplerestapi
  Normal  Started    46s   kubelet            Started container simplerestapi

  