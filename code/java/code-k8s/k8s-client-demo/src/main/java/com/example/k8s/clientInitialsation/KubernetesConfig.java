package com.example.k8s.clientInitialsation;


import java.io.FileReader;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
// import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

@Configuration
public class KubernetesConfig {

    // Default

    // @Bean
    // public ApiClient kubernetesApiClient() throws IOException {
    //     ApiClient client = Config.defaultClient();
    //     io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
    //     return client;
    // }


       @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        // Specify the custom path for the kubeconfig file
        String kubeConfigPath = System.getenv("KUBECONFIG");

        // If KUBECONFIG environment variable is not set, fall back to default location
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            String homeDir = System.getProperty("user.home");
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // For Windows, use the default Windows path for kubeconfig
                kubeConfigPath = "C://Users//your_user//.kube//config";
            } else {
                // For Linux/Ubuntu, use the default Linux path
                kubeConfigPath = homeDir + "/.kube/config";
            }
        }

        // Create an API client using the custom kubeconfig file
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        
        // Set the custom client as the default API client for Kubernetes operations
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        
        return client;
    }
}