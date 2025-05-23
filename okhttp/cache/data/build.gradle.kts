plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(Config.Deps.OkHttp.okHttp)
            }
        }
    }
}

initPublishing(
    artifactId = Config.Publishing.okhttpCacheData,
)