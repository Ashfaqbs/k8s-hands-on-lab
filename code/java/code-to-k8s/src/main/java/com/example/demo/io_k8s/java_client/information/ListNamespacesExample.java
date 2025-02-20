package com.example.demo.io_k8s.java_client.information;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.util.Config;

public class ListNamespacesExample {
    public static void main(String[] args) {
        try {
            // Initialize the ApiClient. This picks up your kubeconfig (or in-cluster config)
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
            
            // Create an instance of CoreV1Api (this is in the package: io.kubernetes.client.openapi.apis)
            CoreV1Api coreV1Api = new CoreV1Api(client);
            
            // List all namespaces using the new builder pattern.
            // The listNamespace() method returns a request builder on which you can chain configuration options.
            // Here, we set watch to false (if you don't want a watch), and then execute the request.
            V1NamespaceList namespaceList = coreV1Api.listNamespace()
                    .watch(false)
                    .execute();
            
            // Iterate through and print the name of each namespace.
            for (V1Namespace ns : namespaceList.getItems()) {
                System.out.println("Namespace: " + ns.getMetadata().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
 
OP: 

C:\tmp\code-k8s\code-to-k8s> cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_6slc2edf1dhfjxe2ihm1rm5dq.argfile com.example.demo.io_k8s.java_client.ListNamespacesExample "
Namespace: default
Namespace: dev
Namespace: kube-node-lease
Namespace: kube-public
Namespace: kube-system




 */