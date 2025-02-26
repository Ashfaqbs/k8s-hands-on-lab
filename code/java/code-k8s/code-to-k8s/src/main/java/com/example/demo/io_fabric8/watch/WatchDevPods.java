package com.example.demo.io_fabric8.watch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

public class WatchDevPods {
    private static final Logger logger = LoggerFactory.getLogger(WatchDevPods.class);

    public static void main(String[] args) {
        String kubeConfigPath = "C:\\Users\\ashfa\\.kube\\config";
        String namespace = "dev";

        try {
            // Load kubeconfig file from the specified path
            String kubeConfigContents = new String(Files.readAllBytes(Paths.get(kubeConfigPath)), StandardCharsets.UTF_8);
            Config config = Config.fromKubeconfig(kubeConfigContents);

            try (KubernetesClient client = new DefaultKubernetesClient(config)) {
                // Watch Pods in the dev namespace
                logger.info("Watching Pods in '{}' namespace:", namespace);
                client.pods().inNamespace(namespace).watch(new Watcher<Pod>() {
                    @Override
                    public void eventReceived(Action action, Pod pod) {
                        String podName = pod.getMetadata().getName();
                        switch (action) {
                            case ADDED:
                                logger.info("Pod ADDED in '{}' namespace: {}", namespace, podName);
                                break;
                            case MODIFIED:
                                logger.info("Pod MODIFIED in '{}' namespace: {}", namespace, podName);
                                break;
                            case DELETED:
                                logger.info("Pod DELETED in '{}' namespace: {}", namespace, podName);
                                break;
                            case ERROR:
                                logger.error("Error occurred for Pod in '{}' namespace: {}", namespace, podName);
                                break;
                            default:
                                logger.info("Unknown event for Pod in '{}' namespace: {}", namespace, podName);
                                break;
                        }
                    }

                    @Override
                    public void onClose(WatcherException cause) {
                        if (cause != null) {
                            logger.error("Watch closed with error: {}", cause.getMessage());
                        } else {
                            logger.info("Watch closed normally.");
                        }
                    }
                });

                // Keep the main thread alive to continue watching
                Thread.currentThread().join();
            }
        } catch (IOException e) {
            logger.error("Error reading kubeconfig file: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error interacting with Kubernetes cluster: {}", e.getMessage());
        }
    }
}
/*
 OP:

 
C:\tmp\kubernetes\code\java\code-k8s\code-to-k8s> cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_r8wkppp8tmtjqn6hpyzbnayz.argfile com.example.demo.io_fabric8.watch.WatchDevPods "
12:33:42.306 [main] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Watching Pods in 'dev' namespace:

C:\tmp\kubernetes\code\java\code-k8s\code-to-k8s>

C:\tmp\kubernetes\code\java\code-k8s\code-to-k8s>
C:\tmp\kubernetes\code\java\code-k8s\code-to-k8s> c: && cd c:\tmp\kubernetes\code\java\code-k8s\code-to-k8s && cmd /C "C:\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_r8wkppp8tmtjqn6hpyzbnayz.argfile com.example.demo.io_fabric8.watch.WatchDevPods "
12:35:20.989 [main] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Watching Pods in 'dev' namespace:
\openjdk\graalvm-jdk-21.0.4+8.1\bin\java.exe @C:\Users\ashfa\AppData\Local\Temp\cp_r8wkppp8tmtjqn6hpyzbnayz.argfile com.example.demo.io_fabric8.watch.WatchDevPods "
12:35:20.989 [main] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Watching Pods in 'dev' namespace:
xample.demo.io_fabric8.watch.WatchDevPods "
12:35:20.989 [main] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Watching Pods in 'dev' namespace:
12:35:20.989 [main] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Watching Pods in 'dev' namespace:
12:35:21.265 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod ADDED in 'de12:35:21.265 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod ADDED in 'dev' namespace: example-pod
v' namespace: example-pod
12:35:31.905 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:32.561 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:33.549 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:33.555 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:33.556 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod DELETED in 'dev' namespace: example-pod
12:35:46.791 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod ADDED in 'dev' namespace: example-pod
12:35:46.794 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:46.800 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod
12:35:56.672 [OkHttp https://127.0.0.1:56729/...] INFO com.example.demo.io_fabric8.watch.WatchDevPods -- Pod MODIFIED in 'dev' namespace: example-pod

 */