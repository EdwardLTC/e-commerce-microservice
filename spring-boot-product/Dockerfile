FROM gradle:8.5-jdk17 AS build
WORKDIR /app

COPY . .
RUN gradle build -x test --no-daemon

FROM openjdk:19
WORKDIR /app

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/e_commerce_microservice

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 2003

ENTRYPOINT ["java", "-Dserver.port=2003", "-jar", "app.jar"]
