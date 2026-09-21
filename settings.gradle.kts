pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
    }

    plugins {
        id("org.springframework.boot") version providers
            .gradleProperty("springBootVersion")
            .get() apply false

        id("io.spring.dependency-management") version "1.1.7" apply false
    }
}

rootProject.name = "atlas-platform"

include(":cafeteria")
include(":sugarcane")

project(":cafeteria").projectDir = file("modules/cafeteria")
project(":sugarcane").projectDir = file("modules/sugarcane")