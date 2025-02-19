package com.example.demo.io_k8s.java_client.information;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;

public class ListK8sResources {

    public static void main(String[] args) throws Exception {
        // Initialize the ApiClient. This will pick up your kubeconfig (or in-cluster config).
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        // Create an instance of CoreV1Api to list Pods and Services.
        CoreV1Api coreV1Api = new CoreV1Api(client);
        
        // For pods, using the fluent (builder) style:
        V1PodList podList = coreV1Api.listPodForAllNamespaces()
            // You can optionally configure parameters:
            .watch(false)   // false means it returns the current snapshot (set to true for a watch)
            .execute();
        
        // List all pods:
        podList.getItems().forEach(pod ->
            System.out.println("Pod: " + pod.getMetadata().getName())
        );
        
        // Similarly, list all services:
        V1ServiceList serviceList = coreV1Api.listServiceForAllNamespaces()
            .watch(false)
            .execute();
        
        serviceList.getItems().forEach(service ->
            System.out.println("Service: " + service.getMetadata().getName())
        );
        
        // For deployments, use the AppsV1Api.
        AppsV1Api appsV1Api = new AppsV1Api(client);
        V1DeploymentList deploymentList = appsV1Api.listDeploymentForAllNamespaces()
            .watch(false)
            .execute();
        
        deploymentList.getItems().forEach(deployment ->
            System.out.println("Deployment: " + deployment.getMetadata().getName())
        );
    }
}

/* OP
 



C:\tmp\code-k8s\code-to-k8s> cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_6slc2edf1dhfjxe2ihm1rm5dq.argfile com.example.demo.io_k8s.java_client.ListK8sResources "
Pod: restapi-deployment-5754687796-xn4rw
Pod: coredns-6f6b679f8f-mmsq2
Pod: etcd-minikube
Pod: kube-apiserver-minikube
Pod: kube-controller-manager-minikube
Pod: kube-proxy-9c96w
Pod: kube-scheduler-minikube
Pod: storage-provisioner
Service: kubernetes
Service: mysql
Service: restapi-service
Service: kube-dns
Deployment: restapi-deployment
Deployment: coredns


 */