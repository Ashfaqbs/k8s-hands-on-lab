package com.example.demo.io_fabric8.Creation;
import java.util.Collections;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesDeploymentRunner {
    public static void main(String[] args) {
        // Connect to the cluster (minikube should be configured in your kubeconfig)
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            // 1. Create the Deployment object
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

            // 2. Create the Service object to expose the deployment
            Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName("restservice-service")
                .endMetadata()
                .withNewSpec()
                    .withType("NodePort") // Use ClusterIP if calling from within the cluster
                    .withSelector(Collections.singletonMap("app", "restservice"))
                    .addNewPort()
                        .withProtocol("TCP")
                        .withPort(80)           // Port exposed by the service
                        .withTargetPort(new IntOrString(8080)) // Port on the container
                        .withNodePort(30001)    // Optional: specify the node port
                    .endPort()
                .endSpec()
                .build();

            Service createdService = client.services().inNamespace("default").create(service);
            System.out.println("Service created: " + createdService.getMetadata().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
 

Tested Code:
https://github.com/Ashfaqbs/kubernetes/tree/main/k8s-with-springboot/sb-rest-api




 */