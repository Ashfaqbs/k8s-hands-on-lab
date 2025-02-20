package com.example.demo.io_fabric8.Creation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesDeploymentRunnerV2 {
    public static void main(String[] args) {
        try {
            // Load the kubeconfig from a specific file
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            // Create a Kubernetes client using the provided kubeconfig
            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                // Create the Deployment object
                Deployment deployment = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("restservice-deployment")
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(1)
                        .withNewSelector()
                            .addToMatchLabels("app", "restservice")
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "restservice")
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withName("restservice")
                                    // Update the tag as needed (e.g., :v1)
                                    .withImage("darksharkash/simplerestapisb-k8s:latest")
                                    .addNewPort()
                                        .withContainerPort(8080)
                                    .endPort()
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

                Deployment createdDeployment = client.apps().deployments().inNamespace("default").create(deployment);
                System.out.println("Deployment created: " + createdDeployment.getMetadata().getName());

                // Create the Service object to expose the Deployment
                Service service = new ServiceBuilder()
                    .withNewMetadata()
                        .withName("restservice-service")
                    .endMetadata()
                    .withNewSpec()
                        .withType("NodePort") // Change to ClusterIP if calling from within the cluster
                        .withSelector(Collections.singletonMap("app", "restservice"))
                        .addNewPort()
                            .withProtocol("TCP")
                            .withPort(80)
                            .withTargetPort(new IntOrString(8080))
                            .withNodePort(30001) // Optional: specify a static node port
                        .endPort()
                    .endSpec()
                    .build();

                Service createdService = client.services().inNamespace("default").create(service);
                System.out.println("Service created: " + createdService.getMetadata().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
