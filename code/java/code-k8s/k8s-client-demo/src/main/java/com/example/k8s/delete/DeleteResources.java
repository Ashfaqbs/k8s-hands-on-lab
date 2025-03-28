package com.example.k8s.delete;

import java.io.FileReader;
import java.io.IOException;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.apis.AppsV1Api;
// import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class DeleteResources {
    public static void main(String[] args) throws IOException, ApiException {
        // Load kubeconfig
        String kubeConfigPath = System.getenv("KUBECONFIG");
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Initialize API clients
        CoreV1Api coreApi = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();
        String namespace = "default";

        // Resource names
        String podName = "java-created-pod";
        String serviceName = "java-created-service";
        String deploymentName = "java-created-deployment";  // Assume the deployment name (if exists)

        // Delete Deployment if it exists
        try {
            appsApi.deleteNamespacedDeployment(deploymentName, namespace).execute();
            System.out.println("✅ Deployment deleted: " + deploymentName);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                System.out.println("❌ No Deployment found: " + deploymentName);
            } else {
                throw e;
            }
        }

        // Delete Pod if it exists
        try {
            coreApi.deleteNamespacedPod(podName, namespace).execute();
            System.out.println("✅ Pod deleted: " + podName);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                System.out.println("❌ No Pod found: " + podName);
            } else {
                throw e;
            }
        }

        // Delete Service if it exists
        try {
            coreApi.deleteNamespacedService(serviceName, namespace).execute();
            System.out.println("✅ Service deleted: " + serviceName);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                System.out.println("❌ No Service found: " + serviceName);
            } else {
                throw e;
            }
        }

        // Delete ConfigMaps in namespace  ** this was deleting cert file as well
        // coreApi.listNamespacedConfigMap(namespace).execute()
        //     .getItems()
        //     .forEach(configMap -> {
        //         try {
        //             String configMapName = configMap.getMetadata().getName();
        //             coreApi.deleteNamespacedConfigMap(configMapName, namespace).execute();
        //             System.out.println("✅ ConfigMap deleted: " + configMapName);
        //         } catch (ApiException e) {
        //             System.out.println("❌ Failed to delete ConfigMap: " + e.getMessage());
        //         }
        //     });


            // Delete ConfigMaps (EXCLUDING kube-root-ca.crt)
            coreApi.listNamespacedConfigMap(namespace).execute()
            .getItems()
            .forEach(configMap -> {
                String configMapName = configMap.getMetadata().getName();
                if (!configMapName.equals("kube-root-ca.crt")) {  // Skip the default CA ConfigMap
                    try {
                        coreApi.deleteNamespacedConfigMap(configMapName, namespace).execute();
                        System.out.println("✅ ConfigMap deleted: " + configMapName);
                    } catch (ApiException e) {
                        System.out.println("❌ Failed to delete ConfigMap: " + e.getMessage());
                    }
                } else {
                    System.out.println("⚠️ Skipping system ConfigMap: " + configMapName);
                }
            });




        // Delete Secrets in namespace
        coreApi.listNamespacedSecret(namespace).execute()
            .getItems()
            .forEach(secret -> {
                try {
                    String secretName = secret.getMetadata().getName();
                    coreApi.deleteNamespacedSecret(secretName, namespace).execute();
                    System.out.println("✅ Secret deleted: " + secretName);
                } catch (ApiException e) {
                    System.out.println("❌ Failed to delete Secret: " + e.getMessage());
                }
            });

        System.out.println("🚀 All specified resources deleted!");
    }
}


/*
 OP


 Ubuntu-VM% kubectl get all 
NAME                   READY   STATUS    RESTARTS   AGE
pod/java-created-pod   1/1     Running   0          28m

NAME                           TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
service/java-created-service   ClusterIP   10.109.150.105   <none>        8080/TCP   28m
service/kubernetes             ClusterIP   10.96.0.1        <none>        443/TCP    8d
Ubuntu-VM% kubectl get deployments
No resources found in default namespace.
Ubuntu-VM% 




Ubuntu-VM%  /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.example.k8s.delete.DeleteResources 
❌ No Deployment found: java-created-deployment
✅ Pod deleted: java-created-pod
✅ Service deleted: java-created-service
✅ ConfigMap deleted: kube-root-ca.crt
🚀 All specified resources deleted!
Ubuntu-VM% 



Ubuntu-VM% kubectl get configmap -n default

NAME               DATA   AGE
kube-root-ca.crt   1      118s
Ubuntu-VM% 

was automatically created 


refactored the code to not to delete cert file:


OP

Ubuntu-VM%  cd /media/sf_ubuntu-vm-shared/k8s-client-demo ; /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.exam
ple.k8s.delete.DeleteResources 
❌ No Deployment found: java-created-deployment
❌ No Pod found: java-created-pod
❌ No Service found: java-created-service
⚠️ Skipping system ConfigMap: kube-root-ca.crt
🚀 All specified resources deleted!
Ubuntu-VM% 


Ubuntu-VM% kubectl get all        
NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   8d
Ubuntu-VM% 




 */