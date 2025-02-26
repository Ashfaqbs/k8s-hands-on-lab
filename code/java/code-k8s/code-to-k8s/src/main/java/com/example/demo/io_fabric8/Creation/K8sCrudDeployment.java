package com.example.demo.io_fabric8.Creation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class K8sCrudDeployment {
    public static void main(String[] args) {
        try {
            // Load the kubeconfig from the file
            String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            try (KubernetesClient client = new DefaultKubernetesClient(config)) {

                // 1. Create Spring Boot CRUD App Deployment
                Deployment appDeployment = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("springboot-crud-deployment")
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(3)
                        .withNewSelector()
                            .addToMatchLabels("app", "springboot-k8s-mysql")
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "springboot-k8s-mysql")
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withName("springboot-crud-k8s")
                                    .withImage("darksharkash/sb3j21crud-k8s:latest")
                                    .addNewPort().withContainerPort(8080).endPort()
                                    // Environment variables for DB connection
                                    .addNewEnv().withName("DB_HOST").withValue("mysql").endEnv()
                                    .addNewEnv().withName("DB_NAME").withValue("tempSchema").endEnv()
                                    .addNewEnv().withName("DB_USERNAME").withValue("root").endEnv()
                                    .addNewEnv().withName("DB_PASSWORD").withValue("root").endEnv()
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

                Deployment createdAppDeployment = client.apps().deployments()
                        .inNamespace("default")
                        .create(appDeployment);
                System.out.println("Created App Deployment: " + createdAppDeployment.getMetadata().getName());

                // 2. Create Spring Boot CRUD Service
                Service appService = new ServiceBuilder()
                    .withNewMetadata()
                        .withName("springboot-crud-svc")
                    .endMetadata()
                    .withNewSpec()
                        .withSelector(Collections.singletonMap("app", "springboot-k8s-mysql"))
                        .addNewPort()
                            .withProtocol("TCP")
                            .withPort(8080)
                            .withTargetPort(new IntOrString(8080))
                        .endPort()
                        .withType("NodePort") // Change to ClusterIP if needed
                    .endSpec()
                    .build();

                Service createdAppService = client.services().inNamespace("default").create(appService);
                System.out.println("Created App Service: " + createdAppService.getMetadata().getName());

                // 3. Create PersistentVolumeClaim for MySQL Storage
                PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                    .withNewMetadata()
                        .withName("mysql-pv-claim")
                        .addToLabels("app", "mysql")
                        .addToLabels("tier", "database")
                    .endMetadata()
                    .withNewSpec()
                        .withAccessModes("ReadWriteOnce")
                        .withNewResources()
                            .addToRequests("storage", new Quantity("1Gi"))
                        .endResources()
                    .endSpec()
                    .build();

                PersistentVolumeClaim createdPVC = client.persistentVolumeClaims()
                        .inNamespace("default")
                        .create(pvc);
                System.out.println("Created PVC: " + createdPVC.getMetadata().getName());

                // 4. Create MySQL Deployment
                Deployment mysqlDeployment = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("mysql")
                        .addToLabels("app", "mysql")
                        .addToLabels("tier", "database")
                    .endMetadata()
                    .withNewSpec()
                        .withNewSelector()
                            .addToMatchLabels("app", "mysql")
                            .addToMatchLabels("tier", "database")
                        .endSelector()
                        .withNewStrategy()
                            .withType("Recreate")
                        .endStrategy()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "mysql")
                                .addToLabels("tier", "database")
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withName("mysql")
                                    .withImage("mysql:5.7")
                                    .withArgs("--ignore-db-dir=lost+found")
                                    .addNewEnv().withName("MYSQL_ROOT_PASSWORD").withValue("root").endEnv()
                                    .addNewEnv().withName("MYSQL_DATABASE").withValue("tempSchema").endEnv()
                                    .addNewPort().withContainerPort(3306).withName("mysql").endPort()
                                    .addNewVolumeMount()
                                        .withName("mysql-persistent-storage")
                                        .withMountPath("/var/lib/mysql")
                                    .endVolumeMount()
                                .endContainer()
                                .addNewVolume()
                                    .withName("mysql-persistent-storage")
                                    .withNewPersistentVolumeClaim()
                                        .withClaimName("mysql-pv-claim")
                                    .endPersistentVolumeClaim()
                                .endVolume()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                    .build();

                Deployment createdMysqlDeployment = client.apps().deployments()
                        .inNamespace("default")
                        .create(mysqlDeployment);
                System.out.println("Created MySQL Deployment: " + createdMysqlDeployment.getMetadata().getName());

                // 5. Create MySQL Service (Headless Service)
                Service mysqlService = new ServiceBuilder()
                    .withNewMetadata()
                        .withName("mysql")
                        .addToLabels("app", "mysql")
                        .addToLabels("tier", "database")
                    .endMetadata()
                    .withNewSpec()
                        .withSelector(new HashMap<String, String>() {{
                            put("app", "mysql");
                            put("tier", "database");
                        }})
                        .addNewPort()
                            .withPort(3306)
                            .withTargetPort(new IntOrString(3306))
                        .endPort()
                        .withClusterIP("None") // Headless service
                    .endSpec()
                    .build();

                Service createdMysqlService = client.services()
                        .inNamespace("default")
                        .create(mysqlService);
                System.out.println("Created MySQL Service: " + createdMysqlService.getMetadata().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


/*

Tested Code :  https://github.com/Ashfaqbs/kubernetes/tree/main/k8s-with-springboot/sb-crud/sb-crud-k8s-sample

Tested and working fine 


*/