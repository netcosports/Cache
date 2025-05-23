plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version Config.Build.kotlinVersion
}

android {
    namespace = "com.netcosports.cachesample"

    compileSdk = Config.Build.compileSdk

    defaultConfig {
        minSdk = Config.Build.sampleMinSdk
        targetSdk = Config.Build.targetSdk

        versionCode = getCustomVersionCode()
        versionName = getCustomVersionName()
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    flavorDimensions.add("env")
    productFlavors {
        create("development") {
            dimension = "env"
            applicationId = Config.Build.packageNameDev
        }

        create("production") {
            dimension = "env"
            applicationId = Config.Build.packageNameProd
        }
    }

    compileOptions {
        sourceCompatibility = Config.Build.javaVersion
        targetCompatibility = Config.Build.javaVersion
    }

    kotlinOptions {
        jvmTarget = Config.Build.javaVersion.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(Config.Deps.Kotlin.kotlinJdk8)
    implementation(Config.Deps.Coroutines.android)

    implementation(Config.Deps.AndroidX.appcompat)
    implementation(Config.Deps.AndroidX.recyclerView)
    implementation(Config.Deps.AndroidX.swipeRefresh)
    implementation(Config.Deps.AndroidX.lifecycleViewModel)

    implementation(Config.Deps.NetcoComponents.compositeAdapter)

    implementation(project(Config.Deps.LibsModules.shared))

    implementation(project(Config.Deps.LibsModules.cacheCore))
    implementation(project(Config.Deps.LibsModules.cacheCoreKtx))
    implementation(project(Config.Deps.LibsModules.cacheCoreRx))

    implementation(Config.Deps.RX.rxJava)
    implementation(Config.Deps.RX.rxAndroid)
    implementation(Config.Deps.Retrofit.rxjava2)

    implementation(project(Config.Deps.LibsModules.okhttpCacheData))
    implementation(project(Config.Deps.LibsModules.retrofitCacheData))
    kapt(project(Config.Deps.LibsModules.retrofitCacheProcessor))
    implementation(Config.Deps.Retrofit.convertedGson)
    implementation(Config.Deps.Retrofit.retrofit)
    implementation(Config.Deps.OkHttp.okHttp)
    implementation(Config.Deps.OkHttp.chucker)
}