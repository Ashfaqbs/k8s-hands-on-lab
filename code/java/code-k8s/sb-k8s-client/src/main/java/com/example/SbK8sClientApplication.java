package com.example;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

@SpringBootApplication
public class SbK8sClientApplication {

	public static void main(String[] args) throws IOException, Exception {
		SpringApplication.run(SbK8sClientApplication.class, args);

		String kubeConfigPath = "/.kube/config";
        kubeConfigPath = kubeConfigPath.replaceFirst("^~", System.getProperty("user.home"));
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader("C://Users//ashfa//.kube//config"))).build();
        Configuration.setDefaultApiClient(client);

        // CoreV1Api api = new CoreV1Api();

        // // Create a Pod object
        // V1Pod pod = new V1Pod()
        //         .metadata(new V1ObjectMeta().name("my-simple-pod"))
        //         .spec(new V1PodSpec()
        //                 .containers(Collections.singletonList(new V1Container()
        //                         .name("nginx-container")
        //                         .image("nginx:latest"))));

        // // Create the Pod in the "default" namespace
        // var createdPod = api.createNamespacedPod("default", pod);

        // System.out.println("Pod created: " + createdPod.execute().getMetadata().getName());



		CoreV1Api api = new CoreV1Api();

        // Create a Pod object
        V1Pod pod = new V1Pod()
                .metadata(new V1ObjectMeta().name("my-simple-pod"))
                .spec(new V1PodSpec()
                        .containers(Collections.singletonList(new V1Container()
                                .name("nginx-container")
                                .image("nginx:latest")))
                        .runtimeClassName("your-runtime-class-name")); // Add runtimeClassName

        // Create the Pod in the "default" namespace
        var createdPod = api.createNamespacedPod("default", pod);

        System.out.println("Pod created: " + createdPod.execute().getMetadata().getName());
    
    
	}

}
