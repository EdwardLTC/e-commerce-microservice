FROM openjdk:19
WORKDIR /app

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/e_commerce_microservice

COPY build/libs/*.jar app.jar

EXPOSE 2003

ENTRYPOINT ["java", "-Dserver.port=2003", "-jar", "app.jar"]
