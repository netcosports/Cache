import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")
    }
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = Config.Publishing.cacheKtorData
            transitiveExport = true
            xcf.add(this)
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(Config.Deps.Ktor.core)

                implementation(project(Config.Deps.LibsModules.cacheCore))
                implementation(project(Config.Deps.LibsModules.cacheCoreKtx))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Config.Deps.Ktor.android)

                implementation(project(Config.Deps.LibsModules.okhttpCacheData))
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
    namespace = "com.netcosports.ktor.cache.data"
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

initPublishing(
    artifactId = Config.Publishing.cacheKtorData,
    javadoc = emptyJavadocJar,
)