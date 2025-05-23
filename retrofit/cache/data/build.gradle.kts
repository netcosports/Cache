plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(project(Config.Deps.LibsModules.okhttpCacheData))

                implementation(Config.Deps.OkHttp.okHttp)
                implementation(Config.Deps.Retrofit.retrofit)
            }
        }
    }
}

initPublishing(
    artifactId = Config.Publishing.retrofitCacheData,
)