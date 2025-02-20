package com.example.demo.controller;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PostgrestConfig;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@RestController
@RequestMapping("/api")
public class PostgrestDeploymentController {

    /*
     
    Start Minikube 
     */
    @PostMapping("/deployPostgrest")
    public String deployPostgrest(@RequestBody PostgrestConfig configInput) throws IOException {
        // Use auto-configuration (or load kubeconfig explicitly if needed)
         String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

        try (KubernetesClient client = new DefaultKubernetesClient(config)) {
            // Set the namespace; you can also pass this in the payload if desired
            String namespace = "dev";

            // Build the Deployment for PostgREST using user-supplied configuration
            Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName("postgrest-deployment")
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withNewSelector()
                        .addToMatchLabels("app", "postgrest")
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", "postgrest")
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("postgrest")
                                .withImage("postgrest/postgrest")
                                // Set environment variables based on API input
                                .addNewEnv()
                                    .withName("PGRST_DB_URI")
                                    .withValue(configInput.getDbUri())
                                .endEnv()
                                .addNewEnv()
                                    .withName("PGRST_DB_SCHEMA")
                                    .withValue(configInput.getDbSchema())
                                .endEnv()
                                .addNewEnv()
                                    .withName("PGRST_DB_ANON_ROLE")
                                    .withValue(configInput.getDbRole())
                                .endEnv()
                                .addNewPort()
                                    .withContainerPort(configInput.getPort())
                                .endPort()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();

            Deployment createdDeployment = client.apps().deployments()
                .inNamespace(namespace)
                .create(deployment);

            // Build the corresponding Service to expose the PostgREST container
            Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName("postgrest-service")
                .endMetadata()
                .withNewSpec()
                    .withSelector(Collections.singletonMap("app", "postgrest"))
                    .addNewPort()
                        .withProtocol("TCP")
                        .withPort(configInput.getPort())
                        .withTargetPort(new IntOrString(configInput.getPort()))
                    .endPort()
                    // You can choose other service types if needed (e.g., ClusterIP, LoadBalancer)
                    .withType("NodePort")
                .endSpec()
                .build();

            Service createdService = client.services()
                .inNamespace(namespace)
                .create(service);

            return "Created PostgREST Deployment: " + createdDeployment.getMetadata().getName() +
                   " and Service: " + createdService.getMetadata().getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error deploying PostgREST: " + e.getMessage();
        }
    }
}
/*
  POST http://localhost:8080/api/deployPostgrest

  Req body
  {
  "dbUri": "postgres://postgres:admin@host.docker.internal:9991/mainschema",
  "dbSchema": "public",
  "dbRole": "postgres",
  "port": 3000
}


OP

Created PostgREST Deployment: postgrest-deployment and Service: postgrest-service


 */


 /*
  K8s Side 


  C:\Users\ashfa>kubectl get pods -n dev
NAME                                    READY   STATUS    RESTARTS   AGE
postgrest-deployment-68594b9d89-5s74h   1/1     Running   0          56s

C:\Users\ashfa>kubectl logs postgrest-deployment-68594b9d89-5s74h  -n dev
20/Feb/2025:16:37:39 +0000: Starting PostgREST 12.2.8...
20/Feb/2025:16:37:39 +0000: Listening on port 3000
20/Feb/2025:16:37:39 +0000: Listening for notifications on the "pgrst" channel
20/Feb/2025:16:37:39 +0000: Successfully connected to PostgreSQL 17.0 (Debian 17.0-1.pgdg120+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 12.2.0-14) 12.2.0, 64-bit
20/Feb/2025:16:37:39 +0000: Config reloaded
20/Feb/2025:16:37:39 +0000: Schema cache queried in 11.4 milliseconds
20/Feb/2025:16:37:39 +0000: Schema cache loaded 9 Relations, 5 Relationships, 1 Functions, 0 Domain Representations, 4 Media Type Handlers, 1196 Timezones
20/Feb/2025:16:37:39 +0000: Schema cache loaded in 1.9 milliseconds


C:\Users\ashfa>kubectl get services -n dev
NAME                TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
postgrest-service   NodePort   10.109.209.226   <none>        3000:32245/TCP   3m47s



C:\Users\ashfa>minikube service postgrest-service --url -n dev
http://127.0.0.1:56078
❗  Because you are using a Docker driver on windows, the terminal needs to be open to run it.


http://127.0.0.1:56078/employee



[
{
"id": 2,
"emp_code": 102,
"name": "Jane Smith"
},
{
"id": 35,
"emp_code": 101,
"name": "Emily Davis"
},
{
"id": 4,
"emp_code": 103,
"name": "Alice Wonderland"
}
]

  */