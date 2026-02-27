import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.ecommerce.springboot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

extra["exposedVersion"] = "1.0.0"

dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka:3.0.10")

    // --- Database ---
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:${property("exposedVersion")}")
    implementation("org.jetbrains.exposed:exposed-core:${property("exposedVersion")}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${property("exposedVersion")}")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:${property("exposedVersion")}")
    implementation("org.postgresql:postgresql:42.7.2")

    // --- gRPC + Protobuf ---
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.64.0")
    implementation("io.grpc:grpc-stub:1.64.0")
    implementation("io.grpc:grpc-netty-shaded:1.75.0")
    implementation("net.devh:grpc-spring-boot-starter:2.15.0.RELEASE")
    implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation("com.google.protobuf:protobuf-java-util:3.25.3")
    implementation("build.buf.protoc-gen-validate:protoc-gen-validate:1.0.2")

    // --- Avro + Kafka ---
    implementation("org.apache.avro:avro:1.11.4")
    implementation("io.confluent:kafka-avro-serializer:7.5.0")

    // --- Kotlin ---
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // --- Utility ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:0.6.0")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("jakarta_omit")
                    option("@generated=omit")
                }
                id("grpckt")
            }
        }
    }
}

avro {
    fieldVisibility.set("PRIVATE") // Kotlin style encapsulation
    isCreateSetters.set(false)
    outputCharacterEncoding.set("UTF-8")
    stringType.set("String")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateAvroJava {
    source("../.avro")
}

sourceSets {
    main {
        proto {
            srcDir("../.proto")
        }
    }
}
