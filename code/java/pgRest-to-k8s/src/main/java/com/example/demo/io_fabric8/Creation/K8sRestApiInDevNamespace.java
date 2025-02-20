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

public class K8sRestApiInDevNamespace {
    public static void main(String[] args) {
        try {
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                String namespace = "dev";

                Deployment restApiDeployment = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("restapi-deployment")
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(1)
                        .withNewSelector()
                            .addToMatchLabels("app", "restapi")
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "restapi")
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withName("restapi")
                                    .withImage("darksharkash/simplerestapisb-k8s")
                                    .addNewPort()
                                        .withContainerPort(8080)
                                    .endPort()
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

                Deployment createdDeployment = client.apps().deployments()
                    .inNamespace(namespace)
                    .create(restApiDeployment);
                System.out.println("Created REST API Deployment: " + createdDeployment.getMetadata().getName());

                Service restApiService = new ServiceBuilder()
                    .withNewMetadata()
                        .withName("restapi-service")
                    .endMetadata()
                    .withNewSpec()
                        .withSelector(Collections.singletonMap("app", "restapi"))
                        .addNewPort()
                            .withProtocol("TCP")
                            .withPort(8080)
                            .withTargetPort(new IntOrString(8080))
                        .endPort()
                        .withType("NodePort")
                    .endSpec()
                    .build();

                Service createdService = client.services()
                    .inNamespace(namespace)
                    .create(restApiService);
                System.out.println("Created REST API Service: " + createdService.getMetadata().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

 