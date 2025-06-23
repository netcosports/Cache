import org.gradle.api.JavaVersion

object Config {

    object Build {
        const val gradleVersion = "8.10.0"
        const val kotlinVersion = "2.1.21"

        const val compileSdk = 35
        const val minSdk = 21
        const val sampleMinSdk = 21
        const val targetSdk = compileSdk

        val javaVersion = JavaVersion.VERSION_11

        const val packageNameDev = "com.originsdigital.components.sample.dev"
        const val packageNameProd = "com.originsdigital.components.sample"

        const val versionName = "3.0.0"
        const val versionOffset = 0
    }

    object Publishing {

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
            const val ios = "io.ktor:ktor-client-darwin:$version"
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
            const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Build.kotlinVersion}"

            const val kotlinPoet = "com.squareup:kotlinpoet:2.2.0"

            const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1"
        }

        object LibsModules {
            const val cacheCore = ":cache:core"
            const val cacheCoreKtx = ":cache:core-ktx"
            const val cacheCoreRx = ":cache:core-rx"

            const val cacheOkHttpData = ":okhttp:cache:data"

            const val cacheRetrofitData = ":retrofit:cache:data"
            const val cacheRetrofitProcessor = ":retrofit:cache:processor"

            const val cacheKtorData = ":ktor:cache:data"

            const val shared = ":shared"
        }

        object LibsRemote {
            const val cacheVersion = Build.versionName

            const val cacheCore = "${Publishing.cacheGroupId}:cache-core:$cacheVersion"
            const val cacheCoreKtx = "${Publishing.cacheGroupId}:cache-core-ktx:$cacheVersion"
            const val cacheCoreRx = "${Publishing.cacheGroupId}:cache-core-rx:$cacheVersion"

            const val cacheOkHttData = "${Publishing.cacheGroupId}:cache-okhttp-data:$cacheVersion"

            const val cacheRetrofitData = "${Publishing.cacheGroupId}:cache-retrofit-data:$cacheVersion"
            const val cacheRetrofitProcessor = "${Publishing.cacheGroupId}:cache-retrofit-processor:$cacheVersion"

            const val cacheKtorData = "${Publishing.cacheGroupId}:cache-ktor-data:$cacheVersion"
        }
    }
}
