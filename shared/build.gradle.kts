plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Config.Build.kotlinVersion
    kotlin("native.cocoapods")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        name = Config.Publishing.shared
        version = "1.0.0"
        summary = "KMM Shared Module"
        homepage = "no homepage"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            isStatic = true
            baseName = Config.Publishing.shared
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(project(Config.Deps.LibsModules.cacheCore))

                implementation(project(Config.Deps.LibsModules.cacheCoreKtx))
                implementation(Config.Deps.Coroutines.core)

                implementation(project(Config.Deps.LibsModules.ktorCacheData))
                implementation(Config.Deps.Ktor.core)
                implementation(Config.Deps.Ktor.json)
                implementation(Config.Deps.Ktor.logging)
                implementation(Config.Deps.Ktor.serialization)
                implementation(Config.Deps.Ktor.negotiation)
                implementation(Config.Deps.Kotlin.kotlinSerialization)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Config.Deps.Ktor.android)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(Config.Deps.Ktor.ios)
            }
        }
    }
}

android {
    compileSdk = Config.Build.compileSdk

    defaultConfig {
        minSdk = Config.Build.sampleMinSdk
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "com.netcosports.cache.shared"
}