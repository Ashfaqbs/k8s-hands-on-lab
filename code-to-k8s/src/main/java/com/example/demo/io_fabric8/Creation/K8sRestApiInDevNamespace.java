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
            // Load the kubeconfig file
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                // Use the "dev" namespace (assumed to be created via kubectl)
                String namespace = "dev";

                // Create the REST API Deployment (without CRUD/database environment variables)
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

                // Create the REST API Service
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
                        .withType("NodePort") // Change to ClusterIP if only internal access is needed
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
/*
  working fine
  
  kubectl get pods or deployments or services --n dev

  kubectl logs restapi-deployment-5f8b8b8b8b-5j2j2 --n dev we need to provide the pod name

  C:\Users\ashfa>minikube service  restapi-service -n dev --url  we need to provide the service name
http://127.0.0.1:57039
❗  Because you are using a Docker driver on windows, the terminal needs to be open to run it.

 */

 