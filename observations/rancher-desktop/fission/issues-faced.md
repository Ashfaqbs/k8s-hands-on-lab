- in Java we need to take the src folder and pom.xml and create a zip of it .
  ![image](https://github.com/user-attachments/assets/c52fd0f0-8666-4ad2-8e5b-9192b1161f25)
  
```

C:\Users\ashfa\Desktop> fission package create --name hello-pkg-1 --env java --src java-src-pkg1.zip
Package 'hello-pkg-1' created

C:\Users\ashfa\Desktop>fission pkg list
NAME                                          BUILD_STATUS ENV    LASTUPDATEDAT       NAMESPACE
hello-pkg-1                                   succeeded    java   07 Jan 25 22:17 IST default
hello-pkg                                     succeeded    java   07 Jan 25 21:32 IST default
hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f succeeded    go     07 Jan 25 21:06 IST default
hello-py-3a614302-67b9-4092-8ef6-a9fb0b46ac37 succeeded    python 07 Jan 25 21:02 IST default
hello-b950378b-6785-407c-9811-97656e41431c    succeeded    nodejs 07 Jan 25 21:01 IST default

C:\Users\ashfa\Desktop>fission function create --name hello-java-1 --env java --pkg hello-pkg-1 --entrypoint io.fission.HelloWorld
function 'hello-java-1' created

C:\Users\ashfa\Desktop>fission function test --name hello-java-1

Options:
  --name=''               Function name
  --method=[GET]          HTTP Methods: GET,POST,PUT,DELETE,HEAD. To mention single method: --method GET
                          and for multiple methods --method GET --method POST. [DEPRECATED for 'fn create',
                          use 'route create' instead]
  --header=[] (-H)        Request headers
  --body='' (-b)          Request body
  --query=[] (-q)         Request query parameters: -q key1=value1 -q key2=value2
  --timeout=1m0s (-t)     Length of time to wait for the response. If set to zero or negative number, no
                          timeout is set
  --dbtype='kubernetes'   Log database type, e.g. influxdb (currently influxdb and kubernetes logs are
                          supported)
  --subpath=''            Sub Path to check if function internally supports routing

Global Options:
  --verbosity=1 (-v)   CLI verbosity (0 is quiet, 1 is the default, 2 is verbose)
  --kube-context=''    Kubernetes context to be used for the execution of Fission commands
  --namespace='' (-n)  If present, the namespace scope for this CLI request

Usage:
  fission function test [options]

Error: error executing HTTP request: Get "http://127.0.0.1:51633/fission-function/hello-java-1": function request timeout (60000000000)s exceeded

C:\Users\ashfa\Desktop>
```

- Observing the timeout issues.
