# Installation of HELM

[Official Documentation](https://helm.sh/)

[Installation Guide](https://helm.sh/docs/intro/install/)

- Installation in windows.

```
- since we dont have scoop or chocolatey installed we will use winget.


Microsoft Windows [Version 10.0.26100.2605]
(c) Microsoft Corporation. All rights reserved.

C:\Users\ashfa>winget install Helm.Helm
Found Helm [Helm.Helm] Version 3.16.4
This application is licensed to you by its owner.
Microsoft is not responsible for, nor does it grant any licenses to, third-party packages.
Downloading https://get.helm.sh/helm-v3.16.4-windows-amd64.zip
  ██████████████████████████████  16.9 MB / 16.9 MB
Successfully verified installer hash
Extracting archive...
Successfully extracted archive
Starting package install...
Path environment variable modified; restart your shell to use the new value.
Command line alias added: "helm"
Successfully installed



```


- Verify the installation
```

C:\Users\ashfa>helm version
version.BuildInfo{Version:"v3.16.4", GitCommit:"7877b45b63f95635153b29a42c0c2f4273ec45ca", GitTreeState:"clean", GoVersion:"go1.22.7"}


```
