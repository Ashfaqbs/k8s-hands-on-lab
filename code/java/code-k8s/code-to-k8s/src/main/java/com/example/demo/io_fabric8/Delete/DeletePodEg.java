package com.example.demo.io_fabric8.Delete;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class DeletePodEg {
    public static void main(String[] args) {
        // Replace "your-pod-name" with the actual name of the pod you want to delete.
        String namespace = "dev";
        String podName = "example-pod";

        try (KubernetesClient client = new DefaultKubernetesClient()) {
            // Delete the specified pod from the "dev" namespace.
            boolean deleted = client.pods()
                                    .inNamespace(namespace)
                                    .withName(podName)
                                    .delete();

            if (deleted) {
                System.out.println("Pod '" + podName + "' was deleted successfully in namespace '" + namespace + "'.");
            } else {
                System.out.println("Failed to delete pod '" + podName + "'. It may not exist or the deletion was not accepted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 OP
 C:\tmp\kubernetes\code>kubectl get pods -n dev
NAME          READY   STATUS    RESTARTS   AGE
example-pod   1/1     Running   0          25s

C:\tmp\kubernetes\code\java\code-k8s\code-to-k8s> c: && cd c:\tmp\kubernetes\code\java\code-k8s\code-to-k8s && cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_r8wkppp8tmtjqn6hpyzbnayz.argfile com.example.demo.io_fabric8.Delete.DelPodEg "
Pod 'example-pod' was deleted successfully in namespace 'dev'.


C:\tmp\kubernetes\code>kubectl get pods -n dev
No resources found in dev namespace.
 */