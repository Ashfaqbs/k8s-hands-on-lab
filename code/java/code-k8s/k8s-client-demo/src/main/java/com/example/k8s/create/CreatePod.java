package com.example.k8s.create;

import java.io.FileReader;
import java.io.IOException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class CreatePod {
    public static void main(String[] args) throws IOException, ApiException {
        // Determine kubeconfig path based on the environment
        String kubeConfigPath = System.getenv("KUBECONFIG");
        
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }

        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Kubernetes API
        CoreV1Api api = new CoreV1Api();

        // Define Pod Metadata
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("java-created-pod");

        // Define Container
        V1Container container = new V1Container();
        container.setName("simplerestapisb-k8s");
        container.setImage("darksharkash/simplerestapisb-k8s:latest");  // Updated image name

        

        // Define Pod Spec
V1PodSpec podSpec = new V1PodSpec();
podSpec.setRuntimeClassName("default-runtime");  // Use the new RuntimeClass
podSpec.setContainers(java.util.Collections.singletonList(container));


        // Create Pod Object
        V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");
        pod.setMetadata(metadata);
        pod.setSpec(podSpec);

        // Deploy the Pod in the default namespace
        api.createNamespacedPod("default", pod).execute();

        System.out.println("✅ Pod 'java-created-pod' deployed successfully!");
    }
}

/*
 OP:

 Ubuntu-VM% kubectl get all
NAME                   READY   STATUS    RESTARTS   AGE
pod/java-created-pod   1/1     Running   0          62s

NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
service/kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   8d
Ubuntu-VM% kubectl logs pod/java-created-pod 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.0.1)

2025-03-28T03:07:13.066Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : Starting SpringBoot3RestApiExampleApplication v0.0.1-SNAPSHOT using Java 17.0.13 with PID 1 (/app/app.jar started by root in /app)
2025-03-28T03:07:13.070Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : No active profile set, falling back to 1 default profile: "default"
2025-03-28T03:07:14.598Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2025-03-28T03:07:14.613Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-03-28T03:07:14.614Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.4]
2025-03-28T03:07:14.720Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-03-28T03:07:14.725Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1527 ms
2025-03-28T03:07:15.210Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2025-03-28T03:07:15.235Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : Started SpringBoot3RestApiExampleApplication in 2.909 seconds (process running for 3.486)
Ubuntu-VM% 





 */