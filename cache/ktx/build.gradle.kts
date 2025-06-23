import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = Config.Publishing.cacheKtx
            xcf.add(this)
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Config.Deps.Kotlin.kotlin)

                implementation(project(Config.Deps.LibsModules.cacheCore))

                implementation(Config.Deps.Coroutines.core)
            }
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

initPublishing(
    artifactId = Config.Publishing.cacheKtx,
    javadoc = emptyJavadocJar,
)
