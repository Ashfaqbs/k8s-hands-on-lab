package com.example.demo.io_fabric8.Deployments;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ScaleDeploymentExample {
    public static void main(String[] args) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            client.apps().deployments().inNamespace("dev").withName("example-deployment").scale(3);
            System.out.println("Deployment scaled successfully.");
        }
    }
}
