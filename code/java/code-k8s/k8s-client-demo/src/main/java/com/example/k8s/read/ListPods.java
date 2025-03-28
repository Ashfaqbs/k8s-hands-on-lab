package com.example.k8s.read;
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
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(
            System.getenv("KUBECONFIG") != null ? System.getenv("KUBECONFIG") : System.getProperty("user.home") + "/.kube/config"
        ))).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1PodList list = api.listNamespacedPod("default")
                .execute(); // Execute the request

        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }

}
/*
namespace default 
OP
Ubuntu-VM%  cd /media/sf_ubuntu-vm-shared/k8s-client-demo ; /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.exam
ple.k8s.ListPods 
java-created-pod

 */