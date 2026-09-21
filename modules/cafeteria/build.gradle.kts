import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot")
    id("org.openapi.generator") version "7.8.0"
    id("org.jsonschema2dataclass") version "6.1.0"
}

group = "com.atlas.cafeteria"
version = "0.0.1-SNAPSHOT"

// ============================================================
// OpenAPI -> Java
// ============================================================

tasks.register<GenerateTask>("generateRestApi") {
    description = "Generate Cafeteria REST API classes from OpenAPI"

    generatorName.set("java")

    inputSpec.set(
        "$rootDir/contracts/cafeteria/src/main/resources/api/openapi.yaml"
    )

    outputDir.set(
        layout.buildDirectory
            .dir("generated/openapi")
            .get()
            .asFile
            .absolutePath
    )

    apiPackage.set("com.atlas.cafeteria.api.generated")
    modelPackage.set("com.atlas.cafeteria.api.generated.model")

    library.set("native")

    configOptions.set(
        mapOf(
            "useJakartaEe" to "true",
            "useBeanValidation" to "true",
            "generateRecords" to "true",
            "interfaceOnly" to "true",
            "generateSupportingFiles" to "false",
            "dateLibrary" to "java8",
            "openApiNullable" to "false"
        )
    )
}

// ============================================================
// JSON Schema -> Java
// ============================================================

jsonSchema2Pojo {
    targetDirectoryPrefix.set(
        layout.buildDirectory.dir("generated/jsonschema")
    )

    executions {
        create("events") {
            io {
                source.setFrom(
                    files(
                        "$rootDir/contracts/cafeteria/src/main/resources/events/schemas"
                    )
                )

                sourceType.set("jsonschema")
            }

            klass {
                targetPackage.set(
                    "com.atlas.cafeteria.events"
                )
            }

            methods {
                annotateJsr303Jakarta.set(true)
                builders.set(false)
            }

            fields {
                integerUseLong.set(true)
            }
        }
    }
}

// ============================================================
// Generated Java sources
// ============================================================

sourceSets {
    main {
        java {
            srcDir(
                layout.buildDirectory.dir(
                    "generated/openapi/src/main/java"
                )
            )

            srcDir(
                layout.buildDirectory.dir(
                    "generated/jsonschema/sources/java/main"
                )
            )
        }
    }
}

// ============================================================
// Build ordering
// ============================================================

tasks.named("compileJava") {
    dependsOn(
        "generateRestApi",
        "generateJsonSchema2DataClass"
    )
}

// ============================================================
// Generate everything
// ============================================================

tasks.register("generateContracts") {
    description = "Generate all Cafeteria contracts"

    dependsOn(
        "generateRestApi",
        "generateJsonSchema2DataClass"
    )
}