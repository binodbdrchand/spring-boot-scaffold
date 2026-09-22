// ============================================================
// Atlas Platform - Root Gradle Build
// ============================================================
//
// This file defines build conventions shared by all Atlas modules.
//
// IMPORTANT ARCHITECTURE RULE:
//
// Each module is an independent Gradle project and is intended to
// be independently buildable and deployable.
//
// The root project provides:
//
//   - Java version / toolchain
//   - Spring Boot version
//   - Dependency version management
//   - Common dependencies
//   - Common test configuration
//   - Common repository configuration
//
// Module-specific build logic belongs in the module's own
// build.gradle.kts file.
//
// Current modules:
//
//   :cafeteria
//   :sugarcane
//
// The contracts/ directory is intentionally NOT a Gradle project.
// Contract source files live there, while each module owns generation
// of its corresponding Java contract classes.
//
// ============================================================


plugins {

    // Provides the standard Java Gradle plugin.
    //
    // This gives the root project and its subprojects access to
    // standard Java lifecycle tasks such as:
    //
    //   compileJava
    //   compileTestJava
    //   test
    //   jar
    //
    java

    // Provides Spring Boot Gradle support.
    //
    // The version is deliberately declared here so the entire
    // platform uses one Spring Boot version.
    //
    // Subprojects apply the plugin through their own build files
    // using the version managed by pluginManagement/settings.gradle.kts.
    id("org.springframework.boot") version "4.2.0-M1"

    // Provides Maven-style dependency management and BOM support.
    //
    // This allows Spring Boot's dependency BOM to control compatible
    // versions of Spring Framework, Jackson, Hibernate, etc. instead
    // of specifying versions individually for every dependency.
    id("io.spring.dependency-management") version "1.1.7"
}


// ============================================================
// Project identity
// ============================================================

// Common Maven/Gradle group for Atlas artifacts.
group = "com.atlas"

// Current platform development version.
version = "0.0.1-SNAPSHOT"


// ============================================================
// Platform versions
// ============================================================
//
// Keep important platform versions centralized here.
//
// This prevents individual modules from accidentally using different
// versions of the same platform technology.
//

// Java language/toolchain version used by the entire platform.
val javaVersion = 25

// Spring Boot version used by the platform.
val springBootVersion = "4.2.0-M1"

// Spring Modulith version used for modular application architecture
// and module-boundary verification.
val springModulithVersion = "2.2.0-M1"

// Testcontainers BOM version.
//
// The BOM keeps Testcontainers modules on compatible versions.
val testcontainersVersion = "1.21.3"


// ============================================================
// Java configuration
// ============================================================

java {

    toolchain {

        // All modules compile against Java 25.
        //
        // Using a Gradle toolchain means developers do not need to
        // manually configure Gradle to use a particular local JDK.
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}


// ============================================================
// Repository configuration
// ============================================================
//
// Maven Central is the standard repository for the majority of the
// platform's Java dependencies.
//

repositories {
    mavenCentral()
}


// ============================================================
// Root dependency management
// ============================================================
//
// Import BOMs rather than specifying versions individually.
//
// A BOM (Bill of Materials) provides a tested set of mutually
// compatible dependency versions.
//
// Spring Boot's BOM manages versions for things such as:
//
//   - Spring Framework
//   - Jackson
//   - Hibernate
//   - JUnit
//   - Spring Security
//   - Flyway
//   - many other Spring ecosystem dependencies
//
// Testcontainers' BOM does the same for Testcontainers modules.
//

dependencyManagement {

    imports {

        // Spring Boot dependency BOM.
        //
        // Individual Spring ecosystem dependencies below therefore
        // do not need explicit versions.
        mavenBom(
            "org.springframework.boot:spring-boot-dependencies:$springBootVersion"
        )

        // Testcontainers dependency BOM.
        //
        // Keeps Testcontainers modules on compatible versions.
        mavenBom(
            "org.testcontainers:testcontainers-bom:$testcontainersVersion"
        )
    }
}


// ============================================================
// Common configuration for every Atlas module
// ============================================================
//
// Every included module is an independent Gradle project.
//
// Common platform dependencies and build conventions are applied here
// so that :cafeteria, :sugarcane, and future modules have a consistent
// baseline.
//
// Module-specific dependencies should still be added in the module's
// own build.gradle.kts when they are not genuinely common to every
// module.
//

subprojects {

    // ------------------------------------------------------------
    // Java
    // ------------------------------------------------------------
    //
    // Make every module a Java project.
    //
    // This gives each module the standard Java source sets and tasks:
    //
    //   src/main/java
    //   src/main/resources
    //   src/test/java
    //   src/test/resources
    //
    // and tasks such as:
    //
    //   compileJava
    //   compileTestJava
    //   test
    //   jar
    //
    apply(plugin = "java")


    // ------------------------------------------------------------
    // Dependency management
    // ------------------------------------------------------------
    //
    // Apply the dependency-management plugin to every module so
    // Spring Boot's BOM and the Testcontainers BOM can control
    // dependency versions inside each module.
    //
    apply(plugin = "io.spring.dependency-management")


    // ------------------------------------------------------------
    // Repositories
    // ------------------------------------------------------------
    //
    // All modules use Maven Central unless a module explicitly
    // requires an additional repository.
    //
    repositories {
        mavenCentral()
    }


    // ------------------------------------------------------------
    // Dependency BOMs
    // ------------------------------------------------------------
    //
    // Import the same BOMs into every module.
    //
    // This is necessary because each module is its own Gradle project.
    //
    dependencyManagement {

        imports {

            // Spring Boot's managed dependency versions.
            mavenBom(
                "org.springframework.boot:spring-boot-dependencies:$springBootVersion"
            )

            // Testcontainers' managed dependency versions.
            mavenBom(
                "org.testcontainers:testcontainers-bom:$testcontainersVersion"
            )
        }
    }


    // ============================================================
    // Common dependencies
    // ============================================================

    dependencies {

        // --------------------------------------------------------
        // Spring MVC / REST
        // --------------------------------------------------------
        //
        // Provides Spring MVC and the embedded web infrastructure
        // used to expose HTTP REST APIs.
        //
        // Includes the Servlet-based web stack.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-webmvc"
        )


        // --------------------------------------------------------
        // Jackson
        // --------------------------------------------------------
        //
        // JSON serialization/deserialization.
        //
        // Used primarily for converting Java objects to/from JSON
        // for REST APIs and other JSON-based integrations.
        add(
            "implementation",
            "com.fasterxml.jackson.core:jackson-databind"
        )


        // --------------------------------------------------------
        // Jackson Java Time
        // --------------------------------------------------------
        //
        // Adds JSON support for java.time types such as:
        //
        //   Instant
        //   LocalDate
        //   LocalDateTime
        //   OffsetDateTime
        //
        // Important because Atlas domain models use Java's modern
        // date/time API rather than legacy java.util.Date.
        add(
            "implementation",
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
        )


        // --------------------------------------------------------
        // Spring Data JPA
        // --------------------------------------------------------
        //
        // Provides Spring's integration with JPA/Hibernate.
        //
        // Used for:
        //
        //   - EntityManager
        //   - repositories
        //   - transaction integration
        //   - persistence
        //   - Hibernate ORM
        //
        // Database schema ownership remains with Flyway; Hibernate
        // validates the schema rather than modifying it.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-data-jpa"
        )


        // --------------------------------------------------------
        // Spring Data Redis
        // --------------------------------------------------------
        //
        // Provides Spring integration with Redis.
        //
        // Intended for platform capabilities such as:
        //
        //   - caching
        //   - short-lived state
        //   - distributed coordination where appropriate
        //   - future platform infrastructure
        //
        // Individual modules may not necessarily use Redis directly.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-data-redis"
        )


        // --------------------------------------------------------
        // Spring Kafka
        // --------------------------------------------------------
        //
        // Provides Kafka producer/consumer integration.
        //
        // Kafka is intended for asynchronous/event-driven integration
        // where a module actually needs it.
        //
        // IMPORTANT:
        //
        // Kafka is part of the current common technical baseline, but
        // module architecture should not unnecessarily become coupled
        // to Kafka-specific APIs.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-kafka"
        )


        // --------------------------------------------------------
        // Spring Validation
        // --------------------------------------------------------
        //
        // Provides Jakarta Bean Validation integration.
        //
        // Used for validating incoming API data and other objects
        // using annotations such as:
        //
        //   @NotNull
        //   @NotBlank
        //   @Size
        //   @Positive
        //
        // Generated OpenAPI models can also use Bean Validation.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-validation"
        )


        // --------------------------------------------------------
        // Spring Security
        // --------------------------------------------------------
        //
        // Provides the Spring Security framework.
        //
        // Used for:
        //
        //   - authentication integration
        //   - authorization
        //   - request security
        //   - method security
        //   - security filters
        //
        // Authentication providers remain configurable at the
        // deployment/application level.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-security"
        )


        // --------------------------------------------------------
        // OAuth2 Resource Server
        // --------------------------------------------------------
        //
        // Enables applications to act as OAuth2/OIDC resource servers.
        //
        // Primarily provides JWT/bearer-token validation support for
        // APIs protected by an external identity provider.
        //
        // This does NOT force Atlas to use a particular IdP.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-oauth2-resource-server"
        )


        // --------------------------------------------------------
        // Spring Boot Actuator
        // --------------------------------------------------------
        //
        // Provides operational endpoints and application
        // instrumentation such as:
        //
        //   - health
        //   - metrics
        //   - application information
        //   - readiness/liveness integration
        //
        // Useful for monitoring and production operations.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-actuator"
        )


        // --------------------------------------------------------
        // Spring Boot OpenTelemetry
        // --------------------------------------------------------
        //
        // Provides OpenTelemetry integration for application
        // observability.
        //
        // Intended to support distributed traces, metrics and
        // telemetry export without tying application code directly
        // to a particular observability vendor.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-opentelemetry"
        )


        // --------------------------------------------------------
        // Flyway
        // --------------------------------------------------------
        //
        // Database schema migration framework.
        //
        // Atlas uses Flyway as the authority for actual database
        // schema evolution.
        //
        // The intended workflow is:
        //
        //   JPA Entity
        //       ↓
        //   Hibernate reference DDL
        //       ↓
        //   Review
        //       ↓
        //   Flyway migration
        //       ↓
        //   Database
        //
        // Hibernate is therefore used to validate the schema, while
        // Flyway owns database changes.
        add(
            "implementation",
            "org.springframework.boot:spring-boot-starter-flyway"
        )


        // ========================================================
        // Runtime database drivers
        // ========================================================

        // --------------------------------------------------------
        // PostgreSQL JDBC driver
        // --------------------------------------------------------
        //
        // Production database driver.
        //
        // Kept as runtimeOnly because application source code should
        // not need to compile directly against PostgreSQL-specific
        // JDBC classes.
        add(
            "runtimeOnly",
            "org.postgresql:postgresql"
        )


        // --------------------------------------------------------
        // H2 database
        // --------------------------------------------------------
        //
        // Lightweight database used primarily for development and
        // automated integration tests.
        //
        // Current cafeteria tests use an in-memory H2 database so
        // the test suite can start with a clean database automatically.
        //
        // H2 is NOT intended to be the production database.
        add(
            "runtimeOnly",
            "com.h2database:h2"
        )


        // ========================================================
        // Spring Modulith
        // ========================================================

        // --------------------------------------------------------
        // Spring Modulith Core
        // --------------------------------------------------------
        //
        // Provides support for structuring and verifying application
        // modules and their boundaries.
        //
        // This fits the Atlas architecture because the platform
        // deliberately avoids microservices while still maintaining
        // strong module boundaries.
        add(
            "implementation",
            "org.springframework.modulith:spring-modulith-starter-core:$springModulithVersion"
        )


        // ========================================================
        // Annotation processors / compile-only dependencies
        // ========================================================

        // --------------------------------------------------------
        // Spring Boot configuration processor
        // --------------------------------------------------------
        //
        // Generates metadata for @ConfigurationProperties.
        //
        // This improves IDE autocomplete and configuration metadata
        // without becoming a runtime dependency.
        add(
            "annotationProcessor",
            "org.springframework.boot:spring-boot-configuration-processor"
        )


        // --------------------------------------------------------
        // Lombok
        // --------------------------------------------------------
        //
        // Compile-time code generation for common Java boilerplate
        // such as getters/setters.
        //
        // compileOnly means Lombok itself is not required at runtime.
        add(
            "compileOnly",
            "org.projectlombok:lombok"
        )

        // Runs Lombok's annotation processor during compilation.
        add(
            "annotationProcessor",
            "org.projectlombok:lombok"
        )


        // ========================================================
        // Test dependencies
        // ========================================================

        // --------------------------------------------------------
        // Spring Boot Test
        // --------------------------------------------------------
        //
        // Main Spring integration-testing foundation.
        //
        // Provides Spring test support, AssertJ, JUnit integration,
        // Mockito and related testing infrastructure.
        add(
            "testImplementation",
            "org.springframework.boot:spring-boot-starter-test"
        )


        // --------------------------------------------------------
        // Spring Security Test
        // --------------------------------------------------------
        //
        // Security-specific test utilities.
        //
        // Useful for testing authenticated requests, security
        // contexts, authorization and security filters.
        add(
            "testImplementation",
            "org.springframework.boot:spring-boot-starter-security-test"
        )


        // --------------------------------------------------------
        // Spring Modulith Test
        // --------------------------------------------------------
        //
        // Provides testing support for Spring Modulith.
        //
        // Useful for verifying module boundaries and module-level
        // application behavior.
        add(
            "testImplementation",
            "org.springframework.modulith:spring-modulith-starter-test:$springModulithVersion"
        )


        // --------------------------------------------------------
        // Testcontainers JUnit integration
        // --------------------------------------------------------
        //
        // Integrates Testcontainers with JUnit.
        //
        // Allows integration tests to start real infrastructure
        // containers when required.
        add(
            "testImplementation",
            "org.testcontainers:junit-jupiter"
        )


        // --------------------------------------------------------
        // Testcontainers PostgreSQL
        // --------------------------------------------------------
        //
        // Allows integration tests to run against a real PostgreSQL
        // instance in a disposable container.
        //
        // This is particularly useful when testing database behavior
        // that differs from H2.
        add(
            "testImplementation",
            "org.testcontainers:postgresql"
        )


        // --------------------------------------------------------
        // Testcontainers Kafka
        // --------------------------------------------------------
        //
        // Allows integration tests to run against a real Kafka broker
        // in a disposable container.
        add(
            "testImplementation",
            "org.testcontainers:kafka"
        )


        // --------------------------------------------------------
        // JUnit Platform Launcher
        // --------------------------------------------------------
        //
        // Provides the JUnit Platform launcher used by Gradle to
        // discover and execute JUnit tests.
        //
        // This is explicitly declared at test runtime because the
        // Gradle Test task executes through the JUnit Platform.
        add(
            "testRuntimeOnly",
            "org.junit.platform:junit-platform-launcher"
        )
    }


    // ============================================================
    // Test configuration
    // ============================================================
    //
    // Each module is an independent Gradle project.
    //
    // Therefore the JUnit Platform must be configured for Test tasks
    // belonging to each subproject.
    //
    // This MUST remain inside `subprojects {}`.
    //
    // If this configuration were placed only at the root-project
    // level, the root Test tasks would use JUnit Platform, but the
    // module Test tasks would not necessarily inherit that behavior.
    //
    // The result can be particularly confusing:
    //
    //   - test source exists                 ✓
    //   - test class compiles                ✓
    //   - JUnit dependencies exist          ✓
    //   - Gradle discovers zero tests       ✗
    //
    // useJUnitPlatform() tells Gradle to discover and execute tests
    // through the JUnit Platform.
    // ============================================================

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}


// ============================================================
// Root Spring Boot executable configuration
// ============================================================
//
// The root project is an aggregation/build project rather than an
// executable application.
//
// Individual modules such as :cafeteria and :sugarcane own their
// applications and deployment artifacts.
//
// Therefore the root project should not produce a Spring Boot
// executable JAR.
//

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}


// ============================================================
// Root JAR configuration
// ============================================================
//
// Keep the normal Java JAR task enabled.
//
// This allows the root project to continue participating in the
// standard Gradle Java lifecycle without producing a Boot executable.
//

tasks.named<Jar>("jar") {
    enabled = true
}