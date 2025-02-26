package com.example.samples;

import java.io.FileReader;
import java.io.IOException;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class DeletePod {

    public static void main(String[] args) throws IOException, ApiException {
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader("C://Users//ashfa//.kube//config"))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        String podName = "example-pod"; // Replace with the pod name you want to delete
        String namespace = "dev";

        api.deleteNamespacedPod(podName, namespace).execute();

        System.out.println("Pod '" + podName + "' deleted from 'dev' namespace.");
    }
}

// C:\Users\ashfa>kubectl get pods -n dev
// NAME          READY   STATUS    RESTARTS      AGE
// example-pod   1/1     Running   1 (65m ago)   88m

// (c) Microsoft Corporation. All rights reserved.

// C:\tmp\sb-k8s-client> cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_auhr3wjlsncdg8xrewvuk4yio.argfile com.example.samples.DeletePod "
// Pod 'example-pod' deleted from 'dev' namespace.


// C:\Users\ashfa>kubectl get pods -n dev
// No resources found in dev namespace.