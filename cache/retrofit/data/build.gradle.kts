plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)
            }
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

initPublishing(
    artifactId = Config.Publishing.cacheRetrofitData,
    javadoc = emptyJavadocJar,
)