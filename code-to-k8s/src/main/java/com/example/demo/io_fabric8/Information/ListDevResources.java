package com.example.demo.io_fabric8.Information;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ListDevResources {
    public static void main(String[] args) {
        try {
            // Load kubeconfig file from the specified path
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                String namespace = "dev";

                // List Pods in the dev namespace
                System.out.println("Listing Pods in '" + namespace + "' namespace:");
                client.pods().inNamespace(namespace).list().getItems().forEach(pod ->
                    System.out.println("Pod: " + pod.getMetadata().getName())
                );

                // List Services in the dev namespace
                System.out.println("\nListing Services in '" + namespace + "' namespace:");
                client.services().inNamespace(namespace).list().getItems().forEach(service ->
                    System.out.println("Service: " + service.getMetadata().getName())
                );

                // List Deployments in the dev namespace
                System.out.println("\nListing Deployments in '" + namespace + "' namespace:");
                client.apps().deployments().inNamespace(namespace).list().getItems().forEach((Deployment deployment) ->
                    System.out.println("Deployment: " + deployment.getMetadata().getName())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
 * OP :
 


 "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_etasorhymri60th3hhzb0nr78.argfile com.example.demo.Information.ListDevResources "
Listing Pods in 'dev' namespace:
Pod: restapi-deployment-5754687796-xn4rw

Listing Services in 'dev' namespace:
Service: restapi-service

Listing Deployments in 'dev' namespace:
Deployment: restapi-deployment

C:\tmp\code-k8s\code-to-k8s>
 */