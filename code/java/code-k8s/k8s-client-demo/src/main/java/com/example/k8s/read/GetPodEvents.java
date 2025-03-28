package com.example.k8s.read;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.EventsV1Api;
import io.kubernetes.client.openapi.models.EventsV1Event;
import io.kubernetes.client.openapi.models.EventsV1EventList;

import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class GetPodEvents {
    public static void main(String[] args) throws IOException, ApiException {
        // Determine kubeconfig path
        String kubeConfigPath = System.getenv("KUBECONFIG");
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }

        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Kubernetes API clients
        CoreV1Api coreApi = new CoreV1Api();
        EventsV1Api eventsApi = new EventsV1Api();

        // Pod details
        String namespace = "default";
        String podName = "java-created-pod";  // Replace with your pod name

        // Fetch all events in the namespace
        EventsV1EventList eventList = eventsApi.listNamespacedEvent(namespace).execute();

        // Print events related to the pod
        System.out.println("📢 Events for Pod: " + podName);
        System.out.println("---------------------------------");

        List<EventsV1Event> events = eventList.getItems();
        boolean foundEvents = false;
        
        for (EventsV1Event event : events) {
            if (event.getRegarding() != null && podName.equals(event.getRegarding().getName())) {
                foundEvents = true;
                System.out.println("📅 Timestamp: " + event.getMetadata().getCreationTimestamp());
                System.out.println("🔹 Type: " + event.getType());
                System.out.println("🔍 Reason: " + event.getReason());
                System.out.println("📜 Message: " + event.getNote());
                System.out.println("---------------------------------");
            }
        }

        if (!foundEvents) {
            System.out.println("❌ No events found for this pod.");
        }
    }
}
/*
Ubuntu-VM% kubectl get all 
NAME                   READY   STATUS    RESTARTS   AGE
pod/java-created-pod   1/1     Running   0          28m

NAME                           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
service/java-created-service   ClusterIP   10.109.150.105   <none>        8080/TCP   28m
service/kubernetes             ClusterIP   10.96.0.1        <none>        443/TCP    8d
Ubuntu-VM% 


 OP

 Ubuntu-VM%  /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.example.k8s.read.GetPodEvents 
📢 Events for Pod: java-created-pod
---------------------------------
📅 Timestamp: 2025-03-28T02:54:46Z
🔹 Type: Normal
🔍 Reason: Scheduled
📜 Message: Successfully assigned default/java-created-pod to minikube
---------------------------------
📅 Timestamp: 2025-03-28T02:54:47Z
🔹 Type: Warning
🔍 Reason: FailedCreatePodSandBox
📜 Message: Failed to create pod sandbox: rpc error: code = Unknown desc = RuntimeHandler "runc" not supported
---------------------------------
📅 Timestamp: 2025-03-28T03:03:32Z
🔹 Type: Normal
🔍 Reason: Scheduled
📜 Message: Successfully assigned default/java-created-pod to minikube
---------------------------------
📅 Timestamp: 2025-03-28T03:03:36Z
🔹 Type: Normal
🔍 Reason: Pulling
📜 Message: Pulling image "simplerestapisb-k8s"
---------------------------------
📅 Timestamp: 2025-03-28T03:03:39Z
🔹 Type: Warning
🔍 Reason: Failed
📜 Message: Failed to pull image "simplerestapisb-k8s": Error response from daemon: pull access denied for simplerestapisb-k8s, repository does not exist or may require 'docker login': denied: requested access to the resource is denied
---------------------------------
📅 Timestamp: 2025-03-28T03:03:39Z
🔹 Type: Warning
🔍 Reason: Failed
📜 Message: Error: ErrImagePull
---------------------------------
📅 Timestamp: 2025-03-28T03:03:40Z
🔹 Type: Normal
🔍 Reason: BackOff
📜 Message: Back-off pulling image "simplerestapisb-k8s"
---------------------------------
📅 Timestamp: 2025-03-28T03:03:40Z
🔹 Type: Warning
🔍 Reason: Failed
📜 Message: Error: ImagePullBackOff
---------------------------------
📅 Timestamp: 2025-03-28T03:06:37Z
🔹 Type: Normal
🔍 Reason: Scheduled
📜 Message: Successfully assigned default/java-created-pod to minikube
---------------------------------
📅 Timestamp: 2025-03-28T03:06:38Z
🔹 Type: Normal
🔍 Reason: Pulling
📜 Message: Pulling image "darksharkash/simplerestapisb-k8s:latest"
---------------------------------
📅 Timestamp: 2025-03-28T03:07:10Z
🔹 Type: Normal
🔍 Reason: Pulled
📜 Message: Successfully pulled image "darksharkash/simplerestapisb-k8s:latest" in 32.053s (32.053s including waiting). Image size: 203831382 bytes.
---------------------------------
📅 Timestamp: 2025-03-28T03:07:11Z
🔹 Type: Normal
🔍 Reason: Created
📜 Message: Created container: simplerestapisb-k8s
---------------------------------
📅 Timestamp: 2025-03-28T03:07:11Z
🔹 Type: Normal
🔍 Reason: Started
📜 Message: Started container simplerestapisb-k8s
---------------------------------
📅 Timestamp: 2025-03-28T03:17:21Z
🔹 Type: Normal
🔍 Reason: Killing
📜 Message: Stopping container simplerestapisb-k8s
---------------------------------
📅 Timestamp: 2025-03-28T03:19:15Z
🔹 Type: Normal
🔍 Reason: Scheduled
📜 Message: Successfully assigned default/java-created-pod to minikube
---------------------------------
📅 Timestamp: 2025-03-28T03:19:16Z
🔹 Type: Normal
🔍 Reason: Pulling
📜 Message: Pulling image "darksharkash/simplerestapisb-k8s:latest"
---------------------------------
📅 Timestamp: 2025-03-28T03:19:20Z
🔹 Type: Normal
🔍 Reason: Pulled
📜 Message: Successfully pulled image "darksharkash/simplerestapisb-k8s:latest" in 3.934s (3.934s including waiting). Image size: 203831382 bytes.
---------------------------------
📅 Timestamp: 2025-03-28T03:19:21Z
🔹 Type: Normal
🔍 Reason: Created
📜 Message: Created container: simplerestapisb-k8s
---------------------------------
📅 Timestamp: 2025-03-28T03:19:21Z
🔹 Type: Normal
🔍 Reason: Started
📜 Message: Started container simplerestapisb-k8s
---------------------------------
📅 Timestamp: 2025-03-28T03:38:33Z
🔹 Type: Warning
🔍 Reason: NodeNotReady
📜 Message: Node is not ready
---------------------------------
Ubuntu-VM% 



 */