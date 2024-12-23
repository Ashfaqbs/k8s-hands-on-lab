# Cron jobs
- In real time task scheduling is an essential aspect of managing the recurring jobs or automated  the process to handle the recurring jobs.

for example:

![alt text](image.png)

![alt text](image-1.png)

## Code 
- A spring application which reads a csv file and processes it and sends in a mail for every 2 mins 
- The schedule is set to run every 2 mins and is achieved by the scheduler.
![alt text](image-2.png)
- We are not exposing any service or endpoint from this application.
- We will remove the line ```	@Scheduled(cron = "0 */2 * * * *")``` as we will not schedule from the app but we will
schedule it from K8s and we will define the function there.
- How k8s will call this function is running the main class of the application

```
@SpringBootApplication

public class SbCronjobAppApplication implements CommandLineRunner {
	
	  @Autowired
	    private ReportGenerationScheduler scheduler;

	public static void main(String[] args) {
		SpringApplication.run(SbCronjobAppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		scheduler.generateReportAndSendEmail();
	}
}
```

- Start the minikube and refer to docker-env.
- Create the docker image for the application.
- Dockerfile
```
# Use an Alpine-based JDK 17 as the base image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Expose the port your application will listen on
EXPOSE 8080

# Copy the JAR into /app and rename it to app.jar
ADD target/sb-cron-app.jar /app/app.jar

# Define the entry point command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


```

- Verify the image 
```

C:\Users\ashfa\OneDrive\Desktop\My-Learning\Java\Code\SB-k8s\sb-cronjob-app>docker build -t darksharkash/sb-cron-app:v1 .
[+] Building 4.8s (9/9) FINISHED                                                                                                       docker:desktop-linux
 => [internal] load build definition from Dockerfile                                                                                                   0.0s
 => => transferring dockerfile: 453B                                                                                                                   0.0s
 => [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine                                                                       2.5s
 => [auth] library/eclipse-temurin:pull token for registry-1.docker.io                                                                                 0.0s
 => [internal] load .dockerignore                                                                                                                      0.0s
 => => transferring context: 2B                                                                                                                        0.0s
 => [1/3] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:2a0bbb1db6d8db42c66ed00c43d954cf458066cc37be12b55144781da7864fdf                 0.0s
 => [internal] load build context                                                                                                                      1.3s
 => => transferring context: 41.38MB                                                                                                                   1.3s
 => CACHED [2/3] WORKDIR /app                                                                                                                          0.0s
 => [3/3] ADD target/sb-cron-app.jar /app/app.jar                                                                                                      0.8s
 => exporting to image                                                                                                                                 0.1s
 => => exporting layers                                                                                                                                0.1s
 => => writing image sha256:d2e18cd2d5b2102b0bf37d1b4759028b027679244fdfe9cabca8f9e128f7d111                                                           0.0s
 => => naming to docker.io/darksharkash/sb-cron-app:v1                                                                                                 0.0s

View build details: docker-desktop://dashboard/build/desktop-linux/desktop-linux/lcnv7zadunue93zyenxwq87xr

What's next:
    View a summary of image vulnerabilities and recommendations → docker scout quickview

C:\Users\ashfa\OneDrive\Desktop\My-Learning\Java\Code\SB-k8s\sb-cronjob-app>docker images
REPOSITORY                                TAG                                                                           IMAGE ID       CREATED         SIZE
darksharkash/sb-cron-app                  v1                                                                            d2e18cd2d5b2   8 seconds ago   251MB
```
- Run the image
```
C:\Users\ashfa\OneDrive\Desktop\My-Learning\Java\Code\SB-k8s\sb-cronjob-app>docker run -p 8080:8080 darksharkash/sb-cron-app:v1

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.1)

2024-12-23T16:27:06.534Z  INFO 1 --- [sb-cronjob-app] [           main] c.example.demo.SbCronjobAppApplication   : Starting SbCronjobAppApplication v0.0.1-SNAPSHOT using Java 21.0.5 with PID 1 (/app/app.jar started by root in /app)
2024-12-23T16:27:06.537Z  INFO 1 --- [sb-cronjob-app] [           main] c.example.demo.SbCronjobAppApplication   : No active profile set, falling back to 1 default profile: "default"
2024-12-23T16:27:07.229Z  INFO 1 --- [sb-cronjob-app] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 9191 (http)
2024-12-23T16:27:07.242Z  INFO 1 --- [sb-cronjob-app] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-12-23T16:27:07.243Z  INFO 1 --- [sb-cronjob-app] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.34]
2024-12-23T16:27:07.270Z  INFO 1 --- [sb-cronjob-app] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-12-23T16:27:07.271Z  INFO 1 --- [sb-cronjob-app] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 695 ms
2024-12-23T16:27:07.588Z  INFO 1 --- [sb-cronjob-app] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 9191 (http) with context path '/'
2024-12-23T16:27:07.599Z  INFO 1 --- [sb-cronjob-app] [           main] c.example.demo.SbCronjobAppApplication   : Started SbCronjobAppApplication in 1.424 seconds (process running for 1.851)
2024-12-23T16:27:07.602Z  INFO 1 --- [sb-cronjob-app] [           main] c.e.demo.job.ReportGenerationScheduler   : ReportGenerationScheduler::generateReportAndSendEmail Execution started on Mon Dec 23 16:27:07 GMT 2024
Data loaded from server 100
2024-12-23T16:27:12.740Z  INFO 1 --- [sb-cronjob-app] [           main] c.e.demo.job.ReportGenerationScheduler   : ReportGenerationScheduler::generateReportAndSendEmail Execution ended on Mon Dec 23 16:27:12 GMT 2024
2024-12-23T16:27:24.865Z  INFO 1 --- [sb-cronjob-app] [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2024-12-23T16:27:24.868Z  INFO 1 --- [sb-cronjob-app] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
```

- Create the YAML file here we will create a CronjobJob object.

```
apiVersion: batch/v1
kind: CronJob # Kubernetes resource kind we are creating, CronJob is a k8s object
metadata:
  name: sb-cron-job # name of the CronJob
spec:
  schedule: "*/2 * * * *" # cron schedule this will run every 2 mins
  successfulJobsHistoryLimit: 1 # Number of successful jobs to keep
  failedJobsHistoryLimit: 5 # Number of failed jobs to keep IMP this clears the pods , as new pods are created as per cron
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: sb-cron-job
              image: darksharkash/sb-cron-app:v1
              imagePullPolicy: IfNotPresent
          restartPolicy: OnFailure
```

- Run the YAML file and verify .
```

C:\Users\ashfa\OneDrive\Desktop\My-Learning\Java\Code\SB-k8s\sb-cronjob-app>kubectl apply -f k8s.yaml
cronjob.batch/sb-cron-job created

C:\Users\ashfa\OneDrive\Desktop\My-Learning\Java\Code\SB-k8s\sb-cronjob-app>kubectl get cronjobs
NAME          SCHEDULE      TIMEZONE   SUSPEND   ACTIVE   LAST SCHEDULE   AGE
sb-cron-job   */2 * * * *   <none>     False     0        <none>          11s


-minikube dashboard 
C:\Users\ashfa>minikube dashboard
🤔  Verifying dashboard health ...
🚀  Launching proxy ...
🤔  Verifying proxy health ...
🎉  Opening http://127.0.0.1:61085/api/v1/namespaces/kubernetes-dashboard/services/http:kubernetes-dashboard:/proxy/ in your default browser...


```


- minikube dashboard:

![alt text](image-4.png)
![alt text](image-5.png)
![alt text](image-3.png)

- mail:

![alt text](image-6.png)