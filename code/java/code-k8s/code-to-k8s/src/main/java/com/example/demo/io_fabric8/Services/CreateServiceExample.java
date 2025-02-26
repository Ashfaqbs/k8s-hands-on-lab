package com.example.demo.io_fabric8.Services;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class CreateServiceExample {
    public static void main(String[] args) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            Service service = new ServiceBuilder()
                .withNewMetadata().withName("example-service").endMetadata()
                .withNewSpec()
                .addNewPort().withPort(80).withTargetPort(new IntOrString(80)).endPort()
                .addToSelector("app", "example")
                .endSpec()
                .build();

            client.services().inNamespace("dev").create(service);
            System.out.println("Service created successfully.");
        }
    }
}
