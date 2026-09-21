plugins {
    java
    id("org.springframework.boot") version "4.2.0-M1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.atlas"
version = "0.0.1-SNAPSHOT"

val javaVersion = 25
val springBootVersion = "4.2.0-M1"
val springModulithVersion = "2.2.0-M1"
val testcontainersVersion = "1.21.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        }
    }

    dependencies {
        // Web
        add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")

        // JSON
        add("implementation", "com.fasterxml.jackson.core:jackson-databind")
        add("implementation", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        // Persistence
        add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")

        // Redis
        add("implementation", "org.springframework.boot:spring-boot-starter-data-redis")

        // Kafka
        add("implementation", "org.springframework.boot:spring-boot-starter-kafka")

        // Validation
        add("implementation", "org.springframework.boot:spring-boot-starter-validation")

        // Security
        add("implementation", "org.springframework.boot:spring-boot-starter-security")
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-oauth2-resource-server"
        )

        // Observability
        add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-opentelemetry"
        )

        // Database migrations
        add("implementation", "org.flywaydb:flyway-core")

        // Databases
        add("runtimeOnly", "org.postgresql:postgresql")
        add("runtimeOnly", "com.h2database:h2")

        // Spring Modulith
        add(
            "implementation",
            "org.springframework.modulith:spring-modulith-starter-core:$springModulithVersion"
        )

        // Configuration metadata
        add(
            "annotationProcessor",
            "org.springframework.boot:spring-boot-configuration-processor"
        )

        // Lombok
        add("compileOnly", "org.projectlombok:lombok")
        add("annotationProcessor", "org.projectlombok:lombok")

        // Testing
        add(
            "testImplementation",
            "org.springframework.boot:spring-boot-starter-test"
        )
        add(
            "testImplementation",
            "org.springframework.boot:spring-boot-starter-security-test"
        )
        add(
            "testImplementation",
            "org.springframework.modulith:spring-modulith-starter-test:$springModulithVersion"
        )

        // Testcontainers
        add("testImplementation", "org.testcontainers:junit-jupiter")
        add("testImplementation", "org.testcontainers:postgresql")
        add("testImplementation", "org.testcontainers:kafka")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}