package com.example.k8s.read;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class ListK8sResources {
    public static void main(String[] args) throws IOException, ApiException {
        // Determine kubeconfig path
        String kubeConfigPath = System.getenv("KUBECONFIG"); //we can set the temp path wiht this name using export command in linux/unix machine 
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }

        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Create API Clients
        CoreV1Api coreApi = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();
        NetworkingV1Api networkingApi = new NetworkingV1Api();

        String namespace = "default";

        // Get Pods
        System.out.println("\n🔵 Listing Pods:");
        List<V1Pod> pods = coreApi.listNamespacedPod(namespace).execute().getItems();
        for (V1Pod pod : pods) {
            System.out.println("   - " + pod.getMetadata().getName());
        }

        // Get Services
        System.out.println("\n🟢 Listing Services:");
        List<V1Service> services = coreApi.listNamespacedService(namespace).execute().getItems();
        for (V1Service service : services) {
            System.out.println("   - " + service.getMetadata().getName());
        }

        // Get Deployments
        System.out.println("\n🟡 Listing Deployments:");
        List<V1Deployment> deployments = appsApi.listNamespacedDeployment(namespace).execute().getItems();
        for (V1Deployment deployment : deployments) {
            System.out.println("   - " + deployment.getMetadata().getName());
        }

        // Get ReplicaSets
        System.out.println("\n🟠 Listing ReplicaSets:");
        List<V1ReplicaSet> replicaSets = appsApi.listNamespacedReplicaSet(namespace).execute().getItems();
        for (V1ReplicaSet rs : replicaSets) {
            System.out.println("   - " + rs.getMetadata().getName());
        }

        // Get ConfigMaps
        System.out.println("\n🔵 Listing ConfigMaps:");
        List<V1ConfigMap> configMaps = coreApi.listNamespacedConfigMap(namespace).execute().getItems();
        for (V1ConfigMap cm : configMaps) {
            System.out.println("   - " + cm.getMetadata().getName());
        }

        // Get Secrets
        System.out.println("\n🟣 Listing Secrets:");
        List<V1Secret> secrets = coreApi.listNamespacedSecret(namespace).execute().getItems();
        for (V1Secret secret : secrets) {
            System.out.println("   - " + secret.getMetadata().getName());
        }

        // Get Ingresses
        System.out.println("\n🔶 Listing Ingresses:");
        List<V1Ingress> ingresses = networkingApi.listNamespacedIngress(namespace).execute().getItems();
        for (V1Ingress ingress : ingresses) {
            System.out.println("   - " + ingress.getMetadata().getName());
        }
    }
}


/*



Ubuntu-VM%  /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.example.k8s.read.ListK8sResources 

🔵 Listing Pods:
   - java-created-pod

🟢 Listing Services:
   - java-created-service
   - kubernetes

🟡 Listing Deployments:

🟠 Listing ReplicaSets:

🔵 Listing ConfigMaps:
   - kube-root-ca.crt

🟣 Listing Secrets:

🔶 Listing Ingresses:
Ubuntu-VM% 



 */