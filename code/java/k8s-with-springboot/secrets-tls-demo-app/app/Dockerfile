FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 10101 appgroup && adduser -D -u 10101 -G appgroup appuser
WORKDIR /app
COPY --from=build /build/target/demo-app.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
