import org.gradle.api.JavaVersion

object Config {

    object Build {
        const val kotlinVersion = "2.1.21"

        const val compileSdk = 35
        const val minSdk = 21
        const val sampleMinSdk = 21
        const val targetSdk = compileSdk

        val javaVersion = JavaVersion.VERSION_11

        const val packageNameDev = "com.netcosports.components.sample"
        const val packageNameProd = "com.netcosports.components.sample"

        const val versionName = "3.0.0"
        const val versionOffset = 0
    }

    object Publishing {

        fun jvm() = arrayOf("jvm")
        fun android() = arrayOf("androidDebug", "androidRelease")
        fun ios() = arrayOf("iosArm64", "iosX64", "iosSimulatorArm64")
        fun kmm() = arrayOf("kotlinMultiplatform", "metadata")

        const val cacheGroupId = "io.github.netcosports.kmm.cache"
        const val cacheVersion = Build.versionName //"1.0.0"

        const val cacheCore = "cache-core"
        const val cacheCoreKtx = "cache-core-ktx"
        const val cacheCoreRx = "cache-core-rx"

        const val cacheOkHttpData = "cache-okhttp-data"

        const val cacheRetrofitData = "cache-retrofit-data"
        const val cacheRetrofitProcessor = "cache-retrofit-processor"

        const val cacheKtorData = "cache-ktor-data"

        const val shared = "shared"
    }

    object Deps {
        object AndroidX {
            const val appcompat = "androidx.appcompat:appcompat:1.7.0"
            const val recyclerView = "androidx.recyclerview:recyclerview:1.4.0"
            const val swipeRefresh = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
            const val lifecycleVersion = "2.8.7"
            const val lifecycleViewModel =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
        }

        object Coroutines {
            const val version = "1.10.2"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        }

        object NetcoComponents {
            const val compositeAdapter =
                "io.github.netcosports.compositeadapter:composite-adapter:1.1.1"
        }

        object Ktor {
            const val version = "3.1.3"
            const val core = "io.ktor:ktor-client-core:$version"
            const val json = "io.ktor:ktor-client-json:$version"
            const val logging = "io.ktor:ktor-client-logging:$version"
            const val android = "io.ktor:ktor-client-okhttp:$version"
            const val ios = "io.ktor:ktor-client-darwin-legacy:$version"
            const val serialization = "io.ktor:ktor-serialization-kotlinx-json:$version"
            const val negotiation = "io.ktor:ktor-client-content-negotiation:${version}"
        }

        object RX {
            const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.21"
            const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
        }

        object OkHttp {
            const val okHttp = "com.squareup.okhttp3:okhttp:4.12.0"
            const val chucker = "com.github.chuckerteam.chucker:library:4.1.0"
        }

        object Retrofit {
            const val version = "2.11.0"
            const val retrofit = "com.squareup.retrofit2:retrofit:$version"
            const val rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:$version"
            const val convertedGson = "com.squareup.retrofit2:converter-gson:$version"
        }

        object Kotlin {
            const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Build.kotlinVersion}"
            const val kotlinJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Build.kotlinVersion}"
            const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Build.kotlinVersion}"

            const val kotlinPoet = "com.squareup:kotlinpoet:2.2.0"

            const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1"
        }

        object LibsModules {
            const val cacheCore = ":cache:core"
            const val cacheCoreKtx = ":cache:core-ktx"
            const val cacheCoreRx = ":cache:core-rx"

            const val okhttpCacheData = ":okhttp:cache:data"

            const val retrofitCacheData = ":retrofit:cache:data"
            const val retrofitCacheProcessor = ":retrofit:cache:processor"

            const val ktorCacheData = ":ktor:cache:data"

            const val shared = ":shared"
        }

        object LibsRemote {
            const val cacheVersion = Build.versionName

            const val cacheCore = "com.netcosports.kmm.cache:cache-core:$cacheVersion"
            const val cacheCoreKtx = "com.netcosports.kmm.cache:cache-core-ktx:$cacheVersion"
            const val cacheCoreRx = "com.netcosports.kmm.cache:cache-core-rx:$cacheVersion"

            const val okhttpCacheData = "com.netcosports.kmm.cache:okhttp-cache-data:$cacheVersion"

            const val retrofitCacheData =
                "com.netcosports.kmm.cache:retrofit-cache-data:$cacheVersion"
            const val retrofitCacheProcessor =
                "com.netcosports.kmm.cache:retrofit-cache-processor:$cacheVersion"

            const val ktorCacheData = "com.netcosports.kmm.cache:ktor-cache-data:$cacheVersion"
        }
    }
}
