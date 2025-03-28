package com.example.k8s.read;
import java.io.FileReader;
import java.io.IOException;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class GetPodLogs {
    public static void main(String[] args) throws IOException, ApiException {
        // Determine kubeconfig path
        String kubeConfigPath = System.getenv("KUBECONFIG");
        if (kubeConfigPath == null || kubeConfigPath.isEmpty()) {
            kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
        }

        // Load kubeconfig
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        // Kubernetes API
        CoreV1Api api = new CoreV1Api();

        // Pod details
        String namespace = "default";
        String podName = "java-created-pod";  // Replace with your pod name

        // Fetch and print logs
        String podLogs = api.readNamespacedPodLog(podName, namespace).execute();
        System.out.println("📜 Logs from Pod: " + podName);
        System.out.println("---------------------------------");
        System.out.println(podLogs);
    }
}



/*
Make sure the pod is up and running:
Ubuntu-VM% kubectl get pods
NAME               READY   STATUS    RESTARTS   AGE
java-created-pod   1/1     Running   0          9m28s
Ubuntu-VM% 





 OP

 Ubuntu-VM%  /usr/bin/env /usr/lib/jvm/java-21-openjdk-amd64/bin/java @/tmp/cp_1f3lyievs9po3hlx2t8voxquc.argfile com.example.k8s.read.GetPodLogs 
📜 Logs from Pod: java-created-pod
---------------------------------

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.0.1)

2025-03-28T03:19:23.782Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : Starting SpringBoot3RestApiExampleApplication v0.0.1-SNAPSHOT using Java 17.0.13 with PID 1 (/app/app.jar started by root in /app)
2025-03-28T03:19:23.787Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : No active profile set, falling back to 1 default profile: "default"
2025-03-28T03:19:25.748Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2025-03-28T03:19:25.765Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-03-28T03:19:25.766Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.4]
2025-03-28T03:19:25.925Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-03-28T03:19:25.927Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2001 ms
2025-03-28T03:19:26.587Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2025-03-28T03:19:26.619Z  INFO 1 --- [           main] s.r.SpringBoot3RestApiExampleApplication : Started SpringBoot3RestApiExampleApplication in 4.17 seconds (process running for 5.393)
2025-03-28T03:20:25.993Z  INFO 1 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-03-28T03:20:25.993Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-03-28T03:20:25.995Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 2 ms

Ubuntu-VM% 



We can also 

🔹 Want to Tail Logs Continuously? Use a WebSocket-based log stream.
🔹 Want to Save Logs to a File? Redirect podLogs to a file output.
🔹 Want to Fetch Logs of All Pods? Loop through all pods using listNamespacedPod().



 */