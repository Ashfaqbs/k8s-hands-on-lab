package com.example.samples;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;

public class KubernetesClientExample {
    public static void main(String[] args) throws Exception {
        // Load KubeConfig from the default location (~/.kube/config)
        ApiClient client = Config.defaultClient();

        // Set the client as the default configuration
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

        System.out.println("Connected to Kubernetes!");
    }
}
/*
OP

c: && cd c:\tmp\sb-k8s-client && cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_auhr3wjlsncdg8xrewvuk4yio.argfile com.example.samples.KubernetesClientExample "
Connected to Kubernetes!
 */