# Cache [last version](https://github.com/netcosports/maven-packages/packages/2046284)

This is a **Kotlin MultiPlatform (KMP)** or **Android-only** library.

In **KMP projects** we use
the [Ktor Client](https://ktor.io/docs/client-create-new-application.html#create-client) with:

- [OkHttp Engine](https://ktor.io/docs/client-engines.html#okhttp)
  and [OkHttp Caching feature](https://square.github.io/okhttp/features/caching/)
  on [Android](ktor/cache/data/src/androidMain/kotlin/com/netcosports/ktor/cache/data/KtorHttpClientUtils.kt)
  on [Android](ktor/cache/data/src/androidMain/kotlin/com/netcosports/ktor/cache/data/KtorHttpClientUtils.kt)
- [Darwin Engine](https://ktor.io/docs/client-engines.html#darwin)
  and [NSURLRequest.CachePolicy feature](https://developer.apple.com/documentation/foundation/nsurlrequest/cachepolicy)
  on [iOS](ktor/cache/data/src/iosMain/kotlin/com/netcosports/ktor/cache/data/KtorHttpClientUtils.kt)

The `Ktor Client` is wrapped in
a [KtorHttpClientWrapper](ktor/cache/data/src/commonMain/kotlin/com/netcosports/ktor/cache/data/KtorHttpClientWrapper.kt)
to create
a [CoroutineLoader](cache/core-ktx/src/commonMain/kotlin/com/netcosports/cache/core/CoroutineLoader.kt)
instead of responses.

In **Android-only projects**, we use the [OkHttp Client](https://square.github.io/okhttp/)
and [OkHttp Caching feature](https://square.github.io/okhttp/features/caching/).\
We generate a `RetrofitServiceWrapper` for
the [Retrofit Service](https://square.github.io/retrofit/) to
create [CoroutineLoader](cache/core-ktx/src/commonMain/kotlin/com/netcosports/cache/core/CoroutineLoader.kt)
or [RxLoader](cache/core-rx/src/jvmMain/kotlin/com/netcosports/cache/core/RxLoader.kt)
instead of responses.

A `CoroutineLoader` / `RxLoader` can return a cached response, a network response or a
`Flow<ResponseWrapper<T>>` / `Observable<ResponseWrapper<T>>` with both responses in the specified
order.

## Table of Contents

1. [KMP projects with Ktor. Android uses OkHttp Engine and iOS uses Darwin Engine](#kmp-projects-with-ktor-android-uses-okhttp-engine-and-ios-uses-darwin-engine)
    - [Gradle setup for KMP projects with Ktor](#gradle-setup-for-kmp-projects-with-ktor)
        - [Using Ktor directly](#using-ktor-directly)
        - [Using Ktor with OpenApiGenerator](#using-ktor-with-openapigenerator)
2. [Android-only projects with Retrofit and OkHttp](#android-only-projects-with-retrofit-and-okhttp)
    - [Gradle setup for Android-only projects](#gradle-setup-for-android-only-projects)
        - [Generating a RetrofitServiceWrapper](#generating-a-retrofitservicewrapper)

### KMP projects with Ktor. Android uses OkHttp Engine and iOS uses Darwin Engine

You can retrieve a cached response, a network response or a Flow<ResponseWrapper<DATA>>:

```kotlin
val cachedData: DATA = coroutineLoader.cache() //suspend fun
val networkData: DATA = coroutineLoader.api() //suspend fun
val flow: Flow<ResponseWrapper<DATA>> = coroutineLoader.toFlow(MergeArguments.CACHE_AND_API)
    .map { responseWrapper ->
        if (responseWrapper.isCache) {
            "CACHE ${responseWrapper.data}"
        } else {
            "API ${responseWrapper.data}"
        }
    }
```

### Gradle setup for KMP projects with Ktor

shared build.gradle

```gradle
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // core module
                implementation(com.origins-digital.kmp.cache:cache-core:$VERSION)
                // coroutine extensions
                implementation(com.origins-digital.kmp.cache:cache-core-ktx:$VERSION)
                // KtorWrapper
                implementation(com.origins-digital.kmp.cache:ktor-cache-data:$VERSION)
            }
        }
    }
}
```

## Using Ktor directly

If you have a `Ktor request`:

```kotlin
val client = io.ktor.client.HttpClient()
val repositories: List<Repository> =
    client.request("https://api.github.com/repositories").body() //suspend fun
```

First, wrap your `Ktor Client`:

```kotlin
// Both OkHttpClients must have the same Cache file. 
private val cacheOkHttpClient = OkHttpClient.Builder()
    .cache(
        Cache(
            directory = File(app.cacheDir, "cache"),
            maxSize = 10L * 1024 * 1024, // 10 MiB
        )
    ).build()
private val apiOkHttpClient = cacheOkHttpClient.newBuilder()
    .addInterceptor(ChuckerInterceptor(app)) // Configure Chucker for API requests only
    .build()
val ktorHttpClientWrapper = KtorHttpClientUtils.createKtorHttpClientWrapper(
    okHttpClientDelegate = { isCache ->
        /* Configure OkHttpClient depending on whether it's a client for cached responses or network responses */
        if (isCache) {
            cacheOkHttpClient
        } else {
            apiOkHttpClient
        }
    },
    configDelegate = { isCache ->
        /* Install Ktor Features depending on whether it's a client for cached responses or network responses */
        install(ContentNegotiation) {
            json(json) // Configure Json for all requests
        }
        if (!isCache) {
            install(Logging) // Enable KtorLogging only for API requests
        }
    }
)
```

Then create a CoroutineLoader for the request:

```kotlin
val coroutineLoader: CoroutineLoader<List<Repository>> = ktorHttpClientWrapper.createLoader {
    this/* Ktor HttpClient */.request("https://api.github.com/repositories").body()
}
```

## Using Ktor with OpenApiGenerator

If you have a generated `ExampleApi`:

```kotlin
internal interface ExampleApi {

    suspend fun getRepositories(): List<Repository>

    companion object {
        operator fun invoke(
            basePath: String = "http://localhost",
            httpClientEngine: HttpClientEngine,
            json: Json,
            clientBlock: HttpClient.() -> Unit = {},
            configBlock: HttpClientConfig<*>.() -> Unit
        ): ExampleApi = ExampleApiImpl(basePath, httpClientEngine, json, clientBlock, configBlock)
    }
}
```

First, wrap your ExampleApi:

```kotlin
// Both OkHttpClients must have the same Cache file. 
private val cacheOkHttpClient = OkHttpClient.Builder()
    .cache(
        Cache(
            directory = File(app.cacheDir, "cache"),
            maxSize = 10L * 1024 * 1024, // 10 MiB
        )
    ).build()
private val apiOkHttpClient = cacheOkHttpClient.newBuilder()
    .addInterceptor(ChuckerInterceptor(app)) // Configure Chucker for API requests only
    .build()
val ktorApiWrapper = KtorHttpClientUtils.createKtorApiWrapper(
    okHttpClientDelegate = { isCache ->
        /* Configure OkHttpClient depending on whether it's a client for cached responses or network responses */
        if (isCache) {
            cacheOkHttpClient
        } else {
            apiOkHttpClient
        }
    },
    engineDelegate = { httpClientEngine, isCache ->
        ExampleApi(
            basePath = baseUrl,
            httpClientEngine = httpClientEngine,
            json = json,
            clientBlock = {},
            configBlock = {},
        )
    }
)
```

Then create a CoroutineLoader for the request:

```kotlin
val coroutineLoader: CoroutineLoader<List<Repository>> = ktorApiWrapper.createLoader {
    this.getRepositories()
}
```

### Android-only projects with Retrofit and OkHttp

You can retrieve a cached response, a network response or a Observable<ResponseWrapper<DATA>>:

```kotlin
// Coroutines
val cachedData: DATA = coroutineLoader.cache() //suspend fun
val networkData: DATA = coroutineLoader.api() //suspend fun
val flow: Flow<ResponseWrapper<DATA>> = coroutineLoader.toFlow(MergeArguments.CACHE_AND_API)
    .map { responseWrapper ->
        if (responseWrapper.isCache) {
            "CACHE ${responseWrapper.data}"
        } else {
            "API ${responseWrapper.data}"
        }
    }

// RX
val cachedData: Single<DATA> = rxLoader.cache()
val networkData: Single<DATA> = rxLoader.api()
val observable: Observable<ResponseWrapper<DATA>> =
    rxLoader.toObservable(MergeArguments.CACHE_AND_API)
        .map { responseWrapper ->
            if (responseWrapper.isCache) {
                "CACHE ${responseWrapper.data}"
            } else {
                "API ${responseWrapper.data}"
            }
        }
```

### Gradle setup for Android-only projects

```gradle
plugins {
    kotlin("kapt") // add this plugin to gerenerate a RetrofitServiceWrapper for your Retrofit Service
}

dependencies {
    // required
    implementation(project(Config.Deps.LibsModules.cacheCore)) // core, jvm module
    // required to configure cache in the OkHttpClient
    implementation(project(Config.Deps.LibsModules.okhttpCacheData)) // okhttp, jvm module
    
    // You can add BOTH if you use suspend requests but have some legacy Rx requests
    implementation(project(Config.Deps.LibsModules.cacheCoreKtx)) // coroutines, jvm module
    implementation(project(Config.Deps.LibsModules.cacheCoreRx)) // Rx, jvm module
    
    // add this module ONLY if you use Retrofit Service
    implementation(project(Config.Deps.LibsModules.retrofitCacheData)) // retrofit service, jvm module
    // add this module to generate a RetrofitServiceWrapper
    kapt(project(Config.Deps.LibsModules.retrofitCacheProcessor)) // retrofit kapt processor, jvm module
}
```

### Generating a RetrofitServiceWrapper

For example, if you have a Retrofit Service:

```kotlin
interface SampleRetrofitService {

    @GET
    fun getResponseSingle(@Url url: String): Single<List<Any>>

    @GET
    fun getResponseObservable(@Url url: String): Observable<List<Any>>

    @GET
    suspend fun getResponseSuspend(@Url url: String): List<Any>
}
```

First, annotate it with `@CacheService`:

```kotlin
@CacheService
interface SampleRetrofitService {
    /* code */
}
```

Run `assemble` or `kapt{BuildType}Kotlin` task for this module to generate a
`RetrofitServiceWrapper`.
Ensure the kapt plugin and processor are added to your module.
Once the task is completed, the RetrofitServiceWrapper will be generated (in our example it will be
`SampleRetrofitServiceWrapper`)

The `RetrofitServiceWrapper`:

- replaces `Single<T>` with `RxLoader<T>`.
- replaces `suspend fun test(): T` with `fun test(): CoroutineLoader<T>`.
- Doesn't modify functions returning other types.

```kotlin
public class SampleRetrofitServiceWrapper(
    public val cacheService: SampleRetrofitService,
    public val apiService: SampleRetrofitService,
) {
    public fun getResponseSingle(url: String): RxLoader<List<Any>> {
        @kotlin.Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        return RxLoader(
            cacheService.getResponseSingle(url),
            apiService.getResponseSingle(url)
        )
    }

    public fun getResponseObservable(url: String): Observable<List<Any>> =
        apiService.getResponseObservable(url)

    public fun getResponseSuspend(url: String): CoroutineLoader<List<Any>> {
        @kotlin.Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        return CoroutineLoader(
            suspend { cacheService.getResponseSuspend(url) },
            suspend { apiService.getResponseSuspend(url) }
        )
    }

    public companion object {
        public fun create(
            retrofitBuilder: Retrofit.Builder,
            okHttpClient: OkHttpClient,
            maxStale: Long = DEFAULT_MAX_STALE,
        ): SampleRetrofitServiceWrapper =
            createServiceWrapper<SampleRetrofitService, SampleRetrofitServiceWrapper>(
                retrofitBuilder, okHttpClient, maxStale
            ) { cacheService, apiService ->
                SampleRetrofitServiceWrapper(cacheService, apiService)
            }
    }
}
```

Initialize the `SampleRetrofitServiceWrapper`:

```kotlin
// Both OkHttpClients must have the same Cache file. 
private val cacheOkHttpClient = OkHttpClient.Builder()
    .cache(
        Cache(
            directory = File(app.cacheDir, "cache"),
            maxSize = 10L * 1024 * 1024, // 10 MiB
        )
    ).build()
private val apiOkHttpClient = cacheOkHttpClient.newBuilder()
    .addInterceptor(ChuckerInterceptor(app)) // Configure Chucker for API requests only
    .build()

val gson = GsonBuilder().setLenient().create()

// just provide the Retrofit.Builder with apiOkHttpClient and get the Wrapper
val retrofitBuilder = Retrofit.Builder()
    .baseUrl("https://baseurl.com/")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))

// provide apiOkHttpClient, the CacheOkHttpClient will be created by making a copy without interceptors 
return SampleRetrofitServiceWrapper.create(retrofitBuilder, apiOkHttpClient)

// or create the Wrapper yourself if you need different interceptors for cache and api and etc
val cacheService = Retrofit.Builder()
    .baseUrl("https://baseurl.com/")
    .client(
        OkHttpClientUtils.setupCache(
            okHttpClient = cacheOkHttpClient,
            onlyCache = true
        )
    )
    .addConverterFactory(GsonConverterFactory.create(gson))
    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
    .build()
    .create(SampleRetrofitService::class.java)

val apiService = Retrofit.Builder()
    .baseUrl("https://baseurl.com/")
    .client(
        OkHttpClientUtils.setupCache(
            okHttpClient = apiOkHttpClient,
            onlyCache = false
        )
    )
    .addConverterFactory(GsonConverterFactory.create(gson))
    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
    .build()
    .create(SampleRetrofitService::class.java)

return SampleRetrofitServiceWrapper(cacheService, apiService)
```

Get your RxLoader<T>, CoroutineLoader<T> or unmodified requests:

```kotlin
val rxLoader: RxLoader<List<Any>> = retrofitServiceWrapper.getResponseSingle(url = url)
val observable: Observable<List<Any>> = retrofitServiceWrapper.getResponseObservable(url = url)
val coroutineLoader: CoroutineLoader<List<Any>> =
    retrofitServiceWrapper.getResponseSuspend(url = url)
```