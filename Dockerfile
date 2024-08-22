FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/nginx-images-cache-0.0.1-SNAPSHOT.jar /app/nginx-images-spring-boot-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "nginx-images-spring-boot-app.jar"]