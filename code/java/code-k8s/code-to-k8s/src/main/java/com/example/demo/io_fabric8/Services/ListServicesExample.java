package com.example.demo.io_fabric8.Services;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ListServicesExample {
    public static void main(String[] args) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            client.services().inNamespace("dev").list().getItems().forEach(service ->
                System.out.println("Service: " + service.getMetadata().getName())
            );
        }
    }
}
