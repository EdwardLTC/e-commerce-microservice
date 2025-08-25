# Note: cd to root repo and run: docker build -f spring-boot-product/Dockerfile -t product-service .
FROM gradle:8.5-jdk17 AS build

# Install JDK 19 from Adoptium
RUN apt-get install -y wget unzip && \
    wget https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19.0.2%2B7/OpenJDK19U-jdk_x64_linux_hotspot_19.0.2_7.tar.gz && \
    mkdir /jdk-19 && \
    tar -xzf OpenJDK19U-jdk_x64_linux_hotspot_19.0.2_7.tar.gz -C /jdk-19 --strip-components=1 && \
    rm OpenJDK19U-jdk_x64_linux_hotspot_19.0.2_7.tar.gz

# Set JDK 19 for Gradle toolchain
ENV JAVA_HOME=/jdk-19
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app

COPY .proto /proto

COPY spring-boot-product /app

RUN ln -s /proto /app/../.proto

RUN chmod +x ./gradlew

RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:19-jre AS runtime

WORKDIR /app

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/e_commerce_microservice
ENV GRPC_SERVICES_USER_HOST=asp-user

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-Dserver.port=9090", "-jar", "app.jar"]
