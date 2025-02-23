```
 - Code > Connect Locally > Test
 Container Process:
 - Build application.properties file with ENV Variable for Kafka bootstrap server and pass a default value as well.
 - Create the Jar, Build the image and push it.
 - Pass the code image in Kafka Compose file and start all the images up
 - Test the code.

 Note: 
 there was an issue when we ran code as container where in properties file  we defined server as localhost:9092, failed to connect also tried with host.internal.docker:9092 also failed to connect to Kafka broker as node not awailable error.

 so we had to pass in code in compose along with Kafka images post then the code connected to Kafka broker successfully.
 and tested the simple api.
 ```