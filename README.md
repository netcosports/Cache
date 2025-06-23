# Cache

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/io.github.netcosports.kmm.cache/cache-core)](https://repo1.maven.org/maven2/io/github/netcosports/kmm/cache/)

Cache is a lightweight **Kotlin MultiPlatform (KMP)** or **Android-only** library that makes Ktor (
KMM) and OkHttp (Android) networking and caching easy.
Library provides a simple API for requesting the cache, the network or both in the specified order.
Also it provides a simple API for using your current Databases in caching requests.

In **KMP projects** we use
the [Ktor Client](https://ktor.io/docs/client-create-new-application.html#create-client) with:

- [OkHttp Engine](https://ktor.io/docs/client-engines.html#okhttp)
  and [OkHttp Caching feature](https://square.github.io/okhttp/features/caching/)
  on [Android](cache/ktor/data/src/androidMain/kotlin/com/originsdigital/cache/ktor/data/KtorHttpClientUtils.kt)
- [Darwin Engine](https://ktor.io/docs/client-engines.html#darwin)
  and [NSURLRequest.CachePolicy feature](https://developer.apple.com/documentation/foundation/nsurlrequest/cachepolicy)
  on [iOS](cache/ktor/data/src/iosMain/kotlin/com/originsdigital/cache/ktor/data/KtorHttpClientUtils.kt)

The `Ktor Client` is wrapped in
a [KtorHttpClientWrapper](cache/ktor/data/src/commonMain/kotlin/com/originsdigital/cache/ktor/data/KtorHttpClientWrapper.kt)
to create
a [CoroutineLoader](cache/ktx/src/commonMain/kotlin/com/originsdigital/cache/ktx/CoroutineLoader.kt)
instead of responses.

In **Android-only projects**, we use the [OkHttp Client](https://square.github.io/okhttp/)
and [OkHttp Caching feature](https://square.github.io/okhttp/features/caching/).\
You
can [create](sample/src/main/java/com/originsdigital/cachesample/data/SampleRetrofitServiceManuallyWrapped.kt)
or generate a `RetrofitServiceWrapper` for
the [Retrofit Service](https://square.github.io/retrofit/) to
create [CoroutineLoader](cache/ktx/src/commonMain/kotlin/com/originsdigital/cache/ktx/CoroutineLoader.kt)
or [SingleLoader](cache/rx/src/jvmMain/kotlin/com/originsdigital/cache/rx/SingleLoader.kt)
instead of responses.

A `CoroutineLoader` / `SingleLoader` can return a cached response, a network response or a
`Flow<ResponseWrapper<T>>` / `Observable<ResponseWrapper<T>>` with both responses in the specified
order.

## Table of Contents

1. [KMP projects with Ktor. Android uses OkHttp Engine and iOS uses Darwin Engine](#kmp-projects-with-ktor-android-uses-okhttp-engine-and-ios-uses-darwin-engine)
    - [Gradle setup for KMP projects with Ktor](#gradle-setup-for-kmp-projects-with-ktor)
        - [Using Ktor directly](#using-ktor-directly)
        - [Using Ktor with OpenApiGenerator](#using-ktor-with-openapigenerator)
2. [KMP or Android projects with custom cache, such as DataBase](#kmp-or-android-projects-with-custom-cache-such-as-database)
    - [Gradle setup for custom cache](#gradle-setup-for-custom-cache)
        - [Using custom cache](#using-custom-cache)
3. [Android-only projects with Retrofit and OkHttp](#android-only-projects-with-retrofit-and-okhttp)
    - [Gradle setup for Android-only projects](#gradle-setup-for-android-only-projects)
        - [Manually wrapping your RetrofitService](#manually-wrapping-your-retrofitservice)
        - [Generating a RetrofitServiceWrapper](#generating-a-retrofitservicewrapper)

### KMP projects with Ktor. Android uses OkHttp Engine and iOS uses Darwin Engine

You can retrieve a cached response, a network response or a `Flow<ResponseWrapper<DATA>>`:

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
                implementation(com.origins-digital.kmp.cache:cache-ktx:$VERSION)
                // KtorWrapper
                implementation(com.origins-digital.kmp.cache:cache-ktor-data:$VERSION)
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
private val cache = Cache(
    directory = File(app.cacheDir, "cache"),
    maxSize = 10L * 1024 * 1024, // 10 MiB
)

private val baseOkHttpClient = OkHttpClient.Builder().build()
private val cacheOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = true,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .build()
private val apiOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = false,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
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

Then create a `CoroutineLoader` for the request:

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

First, wrap your `ExampleApi`:

```kotlin
// Both OkHttpClients must have the same Cache file.
private val cache = Cache(
    directory = File(app.cacheDir, "cache"),
    maxSize = 10L * 1024 * 1024, // 10 MiB
)

private val baseOkHttpClient = OkHttpClient.Builder().build()
private val cacheOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = true,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .build()
private val apiOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = false,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
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

Then create a `CoroutineLoader` for the request:

```kotlin
val coroutineLoader: CoroutineLoader<List<Repository>> = ktorApiWrapper.createLoader {
    this.getRepositories()
}
```

### KMP or Android projects with custom cache, such as DataBase

### Gradle setup for custom cache

KMM shared build.gradle

```gradle
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // core module
                implementation(com.origins-digital.kmp.cache:cache-core:$VERSION)
                // coroutine extensions
                implementation(com.origins-digital.kmp.cache:cache-ktx:$VERSION)
            }
        }
    }
}
```

or

Android build.gradle

```gradle
dependencies {
    implementation(com.origins-digital.kmp.cache:cache-core:$VERSION) // core, kmm module
    
    // You can add BOTH if you use suspend requests but have some legacy Rx requests
    implementation(com.origins-digital.kmp.cache:cache-ktx:$VERSION) // coroutines, kmm module
    implementation(com.origins-digital.kmp.cache:cache-rx:$VERSION) // Rx, jvm module
}
```

## Using custom cache

For example, if you have a `SampleKtorApiDataSource` and `SampleDatabaseDataSource`:

```kotlin
class SampleKtorApiDataSource {

    suspend fun getRepositoriesCoroutine(): List<Any> {
        return ktor.get("https://api.github.com/repositories").body()
    }

    fun getRepositoriesSingle(): Single<List<Any>> {
        return retrofit.get("https://api.github.com/repositories")
    }
}

class SampleDatabaseDataSource {

    suspend fun getRepositoriesCoroutine(): List<Any> {
        return database.getRepositoriesCoroutine()
    }

    fun getRepositoriesSingle(): Single<List<Any>> {
        return database.getRepositoriesSingle()
    }
}
```

You can create `CoroutineLoader`/`SingleLoader`:

```kotlin
class SampleRepositoryImpl(
    private val apiDataSource: SampleKtorApiDataSource,
    private val databaseDataSource: SampleDatabaseDataSource,
) : SampleRepository {

    override fun getRepositoriesCoroutine(): CoroutineLoader<List<Any>> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> apiDataSource.getRepositoriesCoroutine()
                is LoaderArguments.CACHE -> databaseDataSource.getRepositoriesCoroutine()
            }
        }
    }

    override fun getRepositoriesSingle(): SingleLoader<List<Any>> {
        return singleLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> apiDataSource.getRepositoriesSingle()
                is LoaderArguments.CACHE -> databaseDataSource.getRepositoriesSingle()
            }
        }
    }
}
```

Now you can retrieve a cached response, a network response or `Flow<ResponseWrapper<DATA>>`/
`Observable<ResponseWrapper<DATA>>`:

```kotlin
val sampleRepository: SampleRepository = TODO()

// Coroutines
val coroutineLoader = sampleRepository.getRepositoriesCoroutine()
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
val singleLoader = sampleRepository.getRepositoriesSingle()
val cachedData: Single<DATA> = singleLoader.cache()
val networkData: Single<DATA> = singleLoader.api()
val observable: Observable<ResponseWrapper<DATA>> =
    singleLoader.toObservable(MergeArguments.CACHE_AND_API)
        .map { responseWrapper ->
            if (responseWrapper.isCache) {
                "CACHE ${responseWrapper.data}"
            } else {
                "API ${responseWrapper.data}"
            }
        }
```

### Android-only projects with Retrofit and OkHttp

You can retrieve a cached response, a network response or `Flow<ResponseWrapper<DATA>>`/
`Observable<ResponseWrapper<DATA>>`:

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
val cachedData: Single<DATA> = singleLoader.cache()
val networkData: Single<DATA> = singleLoader.api()
val observable: Observable<ResponseWrapper<DATA>> =
    singleLoader.toObservable(MergeArguments.CACHE_AND_API)
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
    // add this plugin to GENERATE a RetrofitServiceWrapper for your Retrofit Service
    kotlin("kapt")
}

dependencies {
    // required core module
    implementation(com.origins-digital.kmp.cache:cache-core:$VERSION) // core, kmm module
    // required to configure cache in the OkHttpClient
    implementation(com.origins-digital.kmp.cache:cache-okhttp-data:$VERSION) // okhttp utls, jvm module
    
    // You can add BOTH if you use suspend requests but have some legacy Rx requests
    implementation(com.origins-digital.kmp.cache:cache-ktx:$VERSION) // coroutines, kmm module
    implementation(com.origins-digital.kmp.cache:cache-rx:$VERSION) // Rx, jvm module
    
     // add these modules to GENERATE a RetrofitServiceWrapper for your Retrofit Service
    implementation(com.origins-digital.kmp.cache:cache-retrofit-data:$VERSION) // annotation for processor, jvm module
    implementation(com.origins-digital.kmp.cache:cache-retrofit-processor:$VERSION) // kapt processor, jvm module
}
```

### Manually wrapping your RetrofitService

For example, if you have a `Retrofit Service`:

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

You can copy this small class and modify it if you want:

```kotlin
class RetrofitServiceWrapper<SERVICE>(
    private val retrofitDelegate: (isCache: Boolean) -> SERVICE,
) {

    val cacheService: SERVICE by lazy { retrofitDelegate(/*isCache = */true) }
    val apiService: SERVICE by lazy { retrofitDelegate(/*isCache = */false) }

    fun <RESULT : Any> coroutine(delegate: suspend SERVICE.() -> RESULT): CoroutineLoader<RESULT> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> delegate(apiService)
                is LoaderArguments.CACHE -> delegate(cacheService)
            }
        }
    }

    fun <RESULT : Any> single(delegate: SERVICE.() -> Single<RESULT>): SingleLoader<RESULT> {
        return singleLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> delegate(apiService)
                is LoaderArguments.CACHE -> delegate(cacheService)
            }
        }
    }
}
```

Then wrap your real `RetrofitService`:

```kotlin
// Both OkHttpClients must have the same Cache file.
private val cache = Cache(
    directory = File(app.cacheDir, "cache"),
    maxSize = 10L * 1024 * 1024, // 10 MiB
)

private val baseOkHttpClient = OkHttpClient.Builder().build()
private val cacheOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = true,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .build()
private val apiOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = false,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .addInterceptor(ChuckerInterceptor(app)) // Configure Chucker for API requests only
    .build()

val retrofitServiceWrapper = RetrofitServiceWrapper<SampleRetrofitService>(
    retrofitDelegate = { isCache ->
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://baseurl.com/")
            .client(
                if (isCache) {
                    cacheOkHttpClient
                } else {
                    apiOkHttpClient
                }
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(SampleRetrofitService::class.java)
    }
)
```

Get your `CoroutineLoader`, `SingleLoader` or unmodified requests:

```kotlin
val coroutineLoader: CoroutineLoader<List<Any>> =
    retrofitServiceWrapper.coroutine { this/*SampleRetrofitService*/.getResponseSuspend(url = url) }
val singleLoader: SingleLoader<List<Any>> =
    retrofitServiceWrapper.single { this/*SampleRetrofitService*/.getResponseSingle(url = url) }
val observable: Observable<List<Any>> =
    retrofitServiceWrapper.apiService.getResponseObservable(url = url)
```

### Generating a RetrofitServiceWrapper

For example, if you have a `Retrofit Service`:

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
Once the task is completed, the `RetrofitServiceWrapper` will be generated (in our example it will
be
`SampleRetrofitServiceWrapper`)

The `RetrofitServiceWrapper`:

- replaces `Single<T>` with `SingleLoader<T>`.
- replaces `suspend fun test(): T` with `fun test(): CoroutineLoader<T>`.
- Doesn't modify functions returning other types.

```kotlin
public class SampleRetrofitServiceWrapper(
    public val cacheService: SampleRetrofitService,
    public val apiService: SampleRetrofitService,
) {

    public fun getResponseSuspend(url: String): CoroutineLoader<List<Any>> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> apiService.getResponseSuspend(url)
                is LoaderArguments.CACHE -> cacheService.getResponseSuspend(url)
            }
        }
    }

    public fun getResponseSingle(url: String): SingleLoader<List<Any>> {
        return singleLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> apiService.getResponseSingle(url)
                is LoaderArguments.CACHE -> cacheService.getResponseSingle(url)
            }
        }
    }

    public fun getResponseObservable(url: String): Observable<List<Any>> {
        return apiService.getResponseObservable(url)
    }
}

```

Initialize the `SampleRetrofitServiceWrapper`:

```kotlin
// Both OkHttpClients must have the same Cache file.
private val cache = Cache(
    directory = File(app.cacheDir, "cache"),
    maxSize = 10L * 1024 * 1024, // 10 MiB
)

private val baseOkHttpClient = OkHttpClient.Builder().build()
private val cacheOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = true,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .build()
private val apiOkHttpClient = baseOkHttpClient.newBuilder()
    .setupCache(
        cache = cache,
        onlyCache = false,
        maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
    )
    .addInterceptor(ChuckerInterceptor(app)) // Configure Chucker for API requests only
    .build()

val createRetrofitClient: (okHttpClient: OkHttpClient) -> SampleRetrofitService =  { okHttpClient ->
    Retrofit.Builder()
        .baseUrl("https://api.curator.io/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
        .create(SampleRetrofitService::class.java)
}

return SampleRetrofitServiceWrapper(
    createRetrofitClient(cacheOkHttpClient),
    createRetrofitClient(apiOkHttpClient)
)
```

Get your `CoroutineLoader`, `SingleLoader` or unmodified requests:

```kotlin
val coroutineLoader: CoroutineLoader<List<Any>> =
    retrofitServiceWrapper.getResponseSuspend(url = url)
val singleLoader: SingleLoader<List<Any>> = retrofitServiceWrapper.getResponseSingle(url = url)
val observable: Observable<List<Any>> = retrofitServiceWrapper.getResponseObservable(url = url)
```