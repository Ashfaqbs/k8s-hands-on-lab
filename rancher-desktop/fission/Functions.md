### Testing out diff code functions in fission
C:\Users\ashfa>cd Desktop

## JS example in Node env

C:\Users\ashfa\Desktop> fission env create --name nodejs --image fission/node-env --namespace default
poolsize setting default to 3
environment 'nodejs' created

C:\Users\ashfa\Desktop>curl https://raw.githubusercontent.com/fission/examples/master/nodejs/hello.js > hello.js
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   119  100   119    0     0    194      0 --:--:-- --:--:-- --:--:--   195

C:\Users\ashfa\Desktop>fission function create --name hello --env nodejs --code hello.js --namespace default
Package 'hello-b950378b-6785-407c-9811-97656e41431c' created
function 'hello' created

C:\Users\ashfa\Desktop> fission function test --name hello --namespace default
hello, world!



## Python example in python env

C:\Users\ashfa\Desktop>fission env create --name python --image ghcr.io/fission/python-env
poolsize setting default to 3
environment 'python' created

C:\Users\ashfa\Desktop>curl -LO https://raw.githubusercontent.com/fission/examples/main/python/hello.py
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    41  100    41    0     0     80      0 --:--:-- --:--:-- --:--:--    80

C:\Users\ashfa\Desktop>fission function create --name hello-py --env python --code hello.py
Package 'hello-py-3a614302-67b9-4092-8ef6-a9fb0b46ac37' created
function 'hello-py' created

C:\Users\ashfa\Desktop>fission function test --name hello-py
Hello, world!


## Go example in go env


C:\Users\ashfa\Desktop>fission env create --name go --image ghcr.io/fission/go-env --builder ghcr.io/fission/go-builder
poolsize setting default to 3
environment 'go' created

C:\Users\ashfa\Desktop>curl -LO https://raw.githubusercontent.com/fission/examples/main/go/hello-world/hello.go
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   199  100   199    0     0    380      0 --:--:-- --:--:-- --:--:--   380

C:\Users\ashfa\Desktop>fission function create --name hello-go --env go --src hello.go --entrypoint Handler
Package 'hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f' created
function 'hello-go' created

C:\Users\ashfa\Desktop>fission pkg list | grep hello-go
'grep' is not recognized as an internal or external command,
operable program or batch file.


- try in git terminal and 

ashfa@Ashu MINGW64 ~
$ fission pkg list | grep hello-go
hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f running      go     07 Jan 25 21:03 IST default

ashfa@Ashu MINGW64 ~
$ fission pkg list | grep hello-go
hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f succeeded    go     07 Jan 25 21:06 IST default

- Go code package is succeeded

C:\Users\ashfa\Desktop>fission function test --name hello-go
Hello, world!




## Java example in java env


C:\Users\ashfa\Desktop>fission environment create --name java --image ghcr.io/fission/jvm-env --builder ghcr.io/fission/jvm-builder --keeparchive --version 3
poolsize setting default to 3
environment 'java' created


C:\Users\ashfa\Desktop>mkdir -p src\main\java\io\fission

C:\Users\ashfa\Desktop>cd src\main\java\io\fission

C:\Users\ashfa\Desktop\src\main\java\io\fission>curl -L https://raw.githubusercontent.com/fission/examples/main/java/hello-world/src/main/java/io/fission/HelloWorld.java -o HelloWorld.java
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   350  100   350    0     0   2458      0 --:--:-- --:--:-- --:--:--  2482

C:\Users\ashfa\Desktop\src\main\java\io\fission>cd ..\..\..\..

C:\Users\ashfa\Desktop\src>curl -LO https://raw.githubusercontent.com/fission/environments/master/jvm/examples/java/pom.xml
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  2072  100  2072    0     0   4262      0 --:--:-- --:--:-- --:--:--  4272

C:\Users\ashfa\Desktop\src>Compress-Archive -Path .\src\, .\pom.xml -DestinationPath java-src-pkg.zip
'Compress-Archive' is not recognized as an internal or external command,
operable program or batch file.

- Try in power shell to zip it.

PS C:\Users\ashfa\Desktop\src> Compress-Archive -Path .\main, .\pom.xml -DestinationPath ..\java-src-pkg.zip
PS C:\Users\ashfa\Desktop\src> ls


    Directory: C:\Users\ashfa\Desktop\src


Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----          1/7/2025   9:13 PM                main
-a----          1/7/2025   9:25 PM           2072 pom.xml


PS C:\Users\ashfa\Desktop\src> cd ..
PS C:\Users\ashfa\Desktop> ls


    Directory: C:\Users\ashfa\Desktop


Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----          1/7/2025   9:13 PM                -p
d-----          1/7/2025   9:25 PM                src
-a----          1/7/2025   9:03 PM            199 hello.go
-a----          1/7/2025   9:01 PM            119 hello.js
-a----          1/7/2025   9:02 PM             41 hello.py
-a----          1/7/2025   9:28 PM           1415 java-src-pkg.zip


PS C:\Users\ashfa\Desktop> fission package create --name hello-pkg --env java --src java-src-pkg.zip
Package 'hello-pkg' created

- verify the status of the package will take some time
PS C:\Users\ashfa\Desktop> fission pkg list

PS C:\Users\ashfa\Desktop> fission pkg list
NAME                                          BUILD_STATUS ENV    LASTUPDATEDAT       NAMESPACE
hello-pkg                                     running      java   07 Jan 25 21:29 IST default
hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f succeeded    go     07 Jan 25 21:06 IST default
hello-py-3a614302-67b9-4092-8ef6-a9fb0b46ac37 succeeded    python 07 Jan 25 21:02 IST default
hello-b950378b-6785-407c-9811-97656e41431c    succeeded    nodejs 07 Jan 25 21:01 IST default
PS C:\Users\ashfa\Desktop> fission pkg list
NAME                                          BUILD_STATUS ENV    LASTUPDATEDAT       NAMESPACE
hello-pkg                                     succeeded    java   07 Jan 25 21:32 IST default
hello-go-b8d0897a-28b4-46ce-a2cf-087ccedfde9f succeeded    go     07 Jan 25 21:06 IST default
hello-py-3a614302-67b9-4092-8ef6-a9fb0b46ac37 succeeded    python 07 Jan 25 21:02 IST default
hello-b950378b-6785-407c-9811-97656e41431c    succeeded    nodejs 07 Jan 25 21:01 IST default

PS C:\Users\ashfa\Desktop> fission function test --name hello-java

- when calling the function getting timeout issue

PS C:\Users\ashfa\Desktop> fission function test --name hello-java

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

Error: error executing HTTP request: Get "http://127.0.0.1:52385/fission-function/hello-java": function request timeout (60000000000)s exceeded
PS C:\Users\ashfa\Desktop>


