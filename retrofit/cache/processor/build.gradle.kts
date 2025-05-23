plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)
                implementation(Config.Deps.Kotlin.kotlinReflect) //JavaToKotlinClassMap

                implementation(project(Config.Deps.LibsModules.okhttpCacheData))
                implementation(project(Config.Deps.LibsModules.retrofitCacheData))
                implementation(project(Config.Deps.LibsModules.cacheCoreRx))
                implementation(project(Config.Deps.LibsModules.cacheCoreKtx))

                implementation(Config.Deps.Kotlin.kotlinPoet)

                implementation(Config.Deps.OkHttp.okHttp)
                implementation(Config.Deps.Retrofit.retrofit)

                implementation(Config.Deps.RX.rxJava)
            }
        }
    }
}

initPublishing(
    artifactId = Config.Publishing.retrofitCacheProcessor,
)