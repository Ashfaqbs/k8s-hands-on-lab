package com.example.demo.io_fabric8.Deployments;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class CreateDeploymentExample {
    public static void main(String[] args) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            Deployment deployment = new DeploymentBuilder()
                .withNewMetadata().withName("example-deployment").endMetadata()
                .withNewSpec()
                .withReplicas(2)
                .withNewSelector().addToMatchLabels("app", "example").endSelector()
                .withNewTemplate()
                .withNewMetadata().addToLabels("app", "example").endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName("example-container")
                .withImage("nginx")
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

            client.apps().deployments().inNamespace("dev").create(deployment);
            System.out.println("Deployment created successfully.");
        }
    }
}
