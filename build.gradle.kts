buildscript {
    repositories {
        google()
        gradlePluginPortal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Config.Build.gradleVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Config.Build.kotlinVersion}")
    }
}

allprojects {
    repositories {
        google()
        gradlePluginPortal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

subprojects {
    afterEvaluate {
        try {
            project.extensions.getByName<com.android.build.gradle.BaseExtension>("android")
        } catch (ignore: Throwable) {
            null
        }?.apply {
            compileSdkVersion(Config.Build.compileSdk)
            defaultConfig {
                minSdk = Config.Build.minSdk
                targetSdk = Config.Build.targetSdk
            }

            compileOptions {
                sourceCompatibility = Config.Build.javaVersion
                targetCompatibility = Config.Build.javaVersion
            }
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = Config.Build.javaVersion.toString()
                }
            }
        }
        // JVM targets
        try {
            project.extensions.getByName<org.gradle.api.plugins.internal.DefaultJavaPluginExtension>(
                "java"
            )
        } catch (ignore: Throwable) {
            null
        }?.apply {
            sourceCompatibility = Config.Build.javaVersion
            targetCompatibility = Config.Build.javaVersion
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = Config.Build.javaVersion.toString()
                }
            }
        }
    }
}
