# Use an Alpine-based JDK 17 as the base image
FROM eclipse-temurin:17.0.13_11-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Expose the port your application will listen on
EXPOSE 8080

# Copy the JAR into /app and rename it to app.jar
ADD target/spring-boot-3-rest-api-example-0.0.1-SNAPSHOT.jar /app/app.jar

# Define the entry point command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
