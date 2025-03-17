┌──(ashfaq㉿kali-vm)-[~/Desktop/code/k8s]
└─$ curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

Downloading https://get.helm.sh/helm-v3.17.2-linux-amd64.tar.gz
Verifying checksum... Done.
Preparing to install helm into /usr/local/bin
[sudo] password for ashfaq: 
helm installed into /usr/local/bin/helm
                                                                                                                                                                                 
┌──(ashfaq㉿kali-vm)-[~/Desktop/code/k8s]
└─$ ls
full-stack  helm  ingress  scale-up
                                                                                                                                                                                 
┌──(ashfaq㉿kali-vm)-[~/Desktop/code/k8s]
└─$ helm version

version.BuildInfo{Version:"v3.17.2", GitCommit:"cc0bbbd6d6276b83880042c1ecb34087e84d41eb", GitTreeState:"clean", GoVersion:"go1.23.7"}
                                                                                                                                                                                 
┌──(ashfaq㉿kali-vm)-[~/Desktop/code/k8s]
└─$ 
