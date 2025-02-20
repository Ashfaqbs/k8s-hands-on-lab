package com.example.demo.io_k8s.java_client.information;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;

public class KubernetesExample {
    public static void main(String[] args) throws Exception {
        // Initialize the API client (this picks up your kubeconfig or in-cluster config)
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        
        CoreV1Api coreV1Api = new CoreV1Api(client);
        
        // Use the builder pattern via the request object
        V1PodList podList = coreV1Api.listPodForAllNamespaces()
        .pretty(null)                // instead of setPretty(null)
        .allowWatchBookmarks(null)   // instead of setAllowWatchBookmarks(null)
        ._continue(null)             // note: "continue" is a reserved word, so the method is named _continue()
        .fieldSelector(null)
        .labelSelector(null)
        .limit(null)
        .resourceVersion(null)
        .resourceVersionMatch(null)
        .timeoutSeconds(null)
        .watch(false)
        .execute();
    
        podList.getItems().forEach(pod -> 
            System.out.println(pod.getMetadata().getName()));

            /* 
            OP
            
            
            restapi-deployment-5754687796-xn4rw
coredns-6f6b679f8f-mmsq2
etcd-minikube
kube-apiserver-minikube
kube-controller-manager-minikube
kube-proxy-9c96w
kube-scheduler-minikube
storage-provisioner
 
            
            
            
            */
    }
}
