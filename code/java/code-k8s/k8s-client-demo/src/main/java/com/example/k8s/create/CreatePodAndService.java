package com.example.k8s.create;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class CreatePodAndService {
    public static void main(String[] args) throws IOException, ApiException {
        // Determine kubeconfig path based on the environment
        String kubeConfigPath = System.getenv("KUBECONFIG");
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }

        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Kubernetes API
        CoreV1Api api = new CoreV1Api();

        // Define Pod Metadata
        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.setName("java-created-pod");
        podMetadata.setLabels(Collections.singletonMap("app", "simplerestapisb"));

        // Define Container
        V1Container container = new V1Container();
        container.setName("simplerestapisb-k8s");
        container.setImage("darksharkash/simplerestapisb-k8s:latest"); // Updated image name
        container.setPorts(Collections.singletonList(new V1ContainerPort().containerPort(8080)));

        // Define Pod Spec
        V1PodSpec podSpec = new V1PodSpec();
        podSpec.setRuntimeClassName("default-runtime");  // Use the new RuntimeClass
        podSpec.setContainers(Collections.singletonList(container));

        // Create Pod Object
        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");
        pod.setMetadata(podMetadata);
        pod.setSpec(podSpec);

        // Deploy the Pod in the default namespace
        api.createNamespacedPod("default", pod).execute();
        System.out.println("✅ Pod 'java-created-pod' deployed successfully!");

        // -------------------- SERVICE CREATION --------------------

        // Define Service Metadata
        V1ObjectMeta serviceMetadata = new V1ObjectMeta();
        serviceMetadata.setName("java-created-service");
        serviceMetadata.setLabels(Collections.singletonMap("app", "simplerestapisb"));

        // Define Service Spec
        V1ServiceSpec serviceSpec = new V1ServiceSpec();
        serviceSpec.setSelector(Collections.singletonMap("app", "simplerestapisb")); // Select pod using label
        serviceSpec.setPorts(Collections.singletonList(
                new V1ServicePort()
                        .port(8080)  // Service port
                        .targetPort(new IntOrString(8080))  // Target pod port
        ));
        serviceSpec.setType("ClusterIP");  // Default Service Type (Can be changed to NodePort/LoadBalancer)

        // Create Service Object
        V1Service service = new V1Service();
        service.setApiVersion("v1");
        service.setKind("Service");
        service.setMetadata(serviceMetadata);
        service.setSpec(serviceSpec);

        // Deploy the Service in the default namespace
        api.createNamespacedService("default", service).execute();
        System.out.println("✅ Service 'java-created-service' deployed successfully!");
    }
}


/*
 

OP 
Ubuntu-VM%  /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.example.k8s.create.CreatePodAndService 
✅ Pod 'java-created-pod' deployed successfully!
✅ Service 'java-created-service' deployed successfully!
Ubuntu-VM% 



portforward 

Ubuntu-VM% kubectl get all                               
NAME                   READY   STATUS    RESTARTS   AGE
pod/java-created-pod   1/1     Running   0          7s

NAME                           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
service/java-created-service   ClusterIP   10.109.150.105   <none>        8080/TCP   7s
service/kubernetes             ClusterIP   10.96.0.1        <none>        443/TCP    8d
Ubuntu-VM% kubectl describe pod/java-created-pod         
Name:                java-created-pod
Namespace:           default
Priority:            0
Runtime Class Name:  default-runtime
Service Account:     default
Node:                minikube/192.168.58.2
Start Time:          Fri, 28 Mar 2025 08:49:15 +0530
Labels:              app=simplerestapisb
Annotations:         <none>
Status:              Running
IP:                  10.244.0.40
IPs:
  IP:  10.244.0.40
Containers:
  simplerestapisb-k8s:
    Container ID:   docker://696d8a85b3c36dbc41f9eab8b31454ce691148fd9cc6d291547d0c2dc55c03b4
    Image:          darksharkash/simplerestapisb-k8s:latest
    Image ID:       docker-pullable://darksharkash/simplerestapisb-k8s@sha256:ce9834f795cb26bd3b26c7243f98761f51361b2813576e016922bb5b062ae568
    Port:           8080/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Fri, 28 Mar 2025 08:49:21 +0530
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-z5wl7 (ro)
Conditions:
  Type                        Status
  PodReadyToStartContainers   True 
  Initialized                 True 
  Ready                       True 
  ContainersReady             True 
  PodScheduled                True 
Volumes:
  kube-api-access-z5wl7:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  21s   default-scheduler  Successfully assigned default/java-created-pod to minikube
  Normal  Pulling    20s   kubelet            Pulling image "darksharkash/simplerestapisb-k8s:latest"
  Normal  Pulled     16s   kubelet            Successfully pulled image "darksharkash/simplerestapisb-k8s:latest" in 3.934s (3.934s including waiting). Image size: 203831382 bytes.
  Normal  Created    15s   kubelet            Created container: simplerestapisb-k8s
  Normal  Started    15s   kubelet            Started container simplerestapisb-k8s
Ubuntu-VM% kubectl get all                      
NAME                   READY   STATUS    RESTARTS   AGE
pod/java-created-pod   1/1     Running   0          24s

NAME                           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
service/java-created-service   ClusterIP   10.109.150.105   <none>        8080/TCP   24s
service/kubernetes             ClusterIP   10.96.0.1        <none>        443/TCP    8d
Ubuntu-VM% kubectl port-forward pod/java-created-pod 8080:8080

Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
Handling connection for 8080
Handling connection for 8080

Ubuntu-VM% curl http://localhost:8080/helloworld
helloworld%   



 */