package com.example.samples;
import java.io.FileReader;
import java.io.IOException;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class ListPods {


      public static void main(String[] args) throws IOException, ApiException {
        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader("C://Users//ashfa//.kube//config"))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1PodList list = api.listNamespacedPod("dev")
                .execute(); // Execute the request

        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }
    
}
/*
OP 

C:\tmp\sb-k8s-client> c: && cd c:\tmp\sb-k8s-client && cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_auhr3wjlsncdg8xrewvuk4yio.argfile com.example.Util "
example-pod
 */