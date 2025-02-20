package com.example.demo.io_k8s.java_client.information;
import java.io.FileReader;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

public class K8sResourceLister {

    public static void main(String[] args) {
        try {
            // Path to your kubeconfig file
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";

            // Load the kubeconfig file
            ApiClient client = Config.fromConfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)));
            Configuration.setDefaultApiClient(client);

            // Core API for Namespaces, Pods, and Services
            CoreV1Api coreV1Api = new CoreV1Api(client);
            
            // Apps API for Deployments
            AppsV1Api appsV1Api = new AppsV1Api(client);

            // List Namespaces
            System.out.println("Listing all namespaces:");
            V1NamespaceList namespaceList = coreV1Api.listNamespace().execute();
            for (V1Namespace ns : namespaceList.getItems()) {
                System.out.println("- " + ns.getMetadata().getName());
            }

            // List Pods
            System.out.println("\nListing all pods:");
            V1PodList podList = coreV1Api.listPodForAllNamespaces().execute();
            podList.getItems().forEach(pod -> System.out.println("- " + pod.getMetadata().getName()));

            // List Services
            System.out.println("\nListing all services:");
            V1ServiceList serviceList = coreV1Api.listServiceForAllNamespaces().execute();
            serviceList.getItems().forEach(service -> System.out.println("- " + service.getMetadata().getName()));

            // List Deployments
            System.out.println("\nListing all deployments:");
            V1DeploymentList deploymentList = appsV1Api.listDeploymentForAllNamespaces().execute();
            deploymentList.getItems().forEach(deployment -> System.out.println("- " + deployment.getMetadata().getName()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*    
 
OP :

Listing all namespaces:
- default
- dev
- kube-node-lease
- kube-public
- kube-system

Listing all pods:
- restapi-deployment-5754687796-xn4rw
- coredns-6f6b679f8f-mmsq2
- etcd-minikube
- kube-apiserver-minikube
- kube-controller-manager-minikube
- kube-proxy-9c96w
- kube-scheduler-minikube
- storage-provisioner

Listing all services:
- kubernetes
- mysql
- restapi-service
- kube-dns

Listing all deployments:
- restapi-deployment
- coredns

 */