plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(project(Config.Deps.LibsModules.cacheCore))

                implementation(Config.Deps.RX.rxJava)
            }
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

initPublishing(
    artifactId = Config.Publishing.cacheRx,
    javadoc = emptyJavadocJar,
)
