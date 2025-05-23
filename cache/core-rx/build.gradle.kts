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

initPublishing(
    artifactId = Config.Publishing.cacheCoreRx,
)
