# Installation 
- switch to rancher context
  

```

C:\Users\ashfa>kubectl create namespace fission
namespace/fission created

C:\Users\ashfa>kubectl create -k "github.com/fission/fission/crds/v1?ref=v1.20.5"
customresourcedefinition.apiextensions.k8s.io/canaryconfigs.fission.io created
customresourcedefinition.apiextensions.k8s.io/environments.fission.io created
customresourcedefinition.apiextensions.k8s.io/functions.fission.io created
customresourcedefinition.apiextensions.k8s.io/httptriggers.fission.io created
customresourcedefinition.apiextensions.k8s.io/kuberneteswatchtriggers.fission.io created
customresourcedefinition.apiextensions.k8s.io/messagequeuetriggers.fission.io created
customresourcedefinition.apiextensions.k8s.io/packages.fission.io created
customresourcedefinition.apiextensions.k8s.io/timetriggers.fission.io created

C:\Users\ashfa>helm repo add fission-charts https://fission.github.io/fission-charts/
"fission-charts" already exists with the same configuration, skipping

C:\Users\ashfa>helm repo update
Hang tight while we grab the latest from your chart repositories...
...Successfully got an update from the "fission-charts" chart repository
Update Complete. ⎈Happy Helming!⎈

C:\Users\ashfa>helm install --version v1.20.5 --namespace fission fission fission-charts/fission-all
W0107 20:59:44.923219   26136 warnings.go:70] metadata.name: this is used in Pod names and hostnames, which can result in surprising behavior; a DNS label is recommended: [must not contain dots]
NAME: fission
LAST DEPLOYED: Tue Jan  7 20:59:42 2025
NAMESPACE: fission
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
1. Install the client CLI.

Mac:
  $ curl -Lo fission https://github.com/fission/fission/releases/download/v1.20.5/fission-v1.20.5-darwin-amd64 && chmod +x fission && sudo mv fission /usr/local/bin/

Linux:
  $ curl -Lo fission https://github.com/fission/fission/releases/download/v1.20.5/fission-v1.20.5-linux-amd64 && chmod +x fission && sudo mv fission /usr/local/bin/

Windows:
  For Windows, you can use the linux binary on WSL. Or you can download this windows executable: https://github.com/fission/fission/releases/download/v1.20.5/fission-v1.20.5-windows-amd64.exe

2. You're ready to use Fission!
  You can create fission resources in the namespace "default"

  # Create an environment
  $ fission env create --name nodejs --image fission/node-env --namespace default

  # Get a hello world
  $ curl https://raw.githubusercontent.com/fission/examples/master/nodejs/hello.js > hello.js

  # Register this function with Fission
  $ fission function create --name hello --env nodejs --code hello.js --namespace default

  # Run this function
  $ fission function test --name hello --namespace default
  Hello, world!
```
