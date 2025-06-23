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

                implementation(project(Config.Deps.LibsModules.cacheRetrofitData))
                implementation(project(Config.Deps.LibsModules.cacheCore))
                implementation(project(Config.Deps.LibsModules.cacheRx))
                implementation(project(Config.Deps.LibsModules.cacheKtx))

                implementation(Config.Deps.Kotlin.kotlinPoet)

                implementation(Config.Deps.RX.rxJava)
            }
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

initPublishing(
    artifactId = Config.Publishing.cacheRetrofitProcessor,
    javadoc = emptyJavadocJar,
)