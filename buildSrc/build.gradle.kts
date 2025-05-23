plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://maven.pkg.github.com/netcosports/maven-packages") {
        credentials {
            username = properties["ghUsername"].toString()
            password = properties["ghPassword"].toString()
        }
    }
}