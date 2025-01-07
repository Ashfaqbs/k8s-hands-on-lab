Container Management and Kubernetes on the Desktop

An open-source application that provides all the essentials to work with containers and Kubernetes on the desktop


Note we were already using minikube from docker desktop support  and Kubectl was referring to minikube context. close docker desktop and switch kubectl context to rancher-desktop.

Post installing switch context:
```

C:\Users\ashfa>kubectl config use-context minikube
Switched to context "minikube".

C:\Users\ashfa>kubectl config use-context docker-desktop
Switched to context "docker-desktop".

C:\Users\ashfa>kubectl config use-context rancher-desktop
Switched to context "rancher-desktop".

C:\Users\ashfa>kubectl get pods
No resources found in default namespace.

C:\Users\ashfa>kubectl get nodes
NAME   STATUS   ROLES                  AGE   VERSION
ashu   Ready    control-plane,master   62m   v1.31.4+k3s1

this is working.

and if we want to use minikube start the docker desktop and switch the context to minikube and run the abouve two commands to verify

```




Installation Link:
- https://forums.rancher.com/t/rancher-desktop-question-about-install/42038/7


Resources:
https://www.youtube.com/watch?v=UnBvNUbxPvA
https://forums.rancher.com/t/rancher-desktop-question-about-install/42038/7
https://www.youtube.com/watch?v=I9YtRhPRMqc&t=473s&pp=ygUXcmFuY2hlciBkZXNrdG9wIHdpbmRvd3M%3D


