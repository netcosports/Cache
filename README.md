# Cache_Android

# Dependencies

Get last [version](https://artifactory-blr.netcodev.com/ui/native/libs-release/com/netcosports/kmm/cache/cache-core)
<pre>
    object Cache {
        const val cacheVersion = version

        //<b>Loader&lt;T&gt;</b>
        const val cacheCore= "com.netcosports.kmm.cache:cache-core:$cacheVersion"

        //extensions for <b>coroutines</b>
        const val cacheCoreKtx = "com.netcosports.kmm.cache:cache-core-ktx:$cacheVersion"

        //extensions for <b>RX</b>
        const val cacheCoreRx = "com.netcosports.kmm.cache:cache-core-rx:$cacheVersion"


        //interceptor for <b>okHttp3</b> <b>cache</b>
        const val okhttpCacheData = "com.netcosports.kmm.cache:okhttp-cache-data:$cacheVersion"


        //annotations for <b>Retrofit service</b>
        const val retrofitCacheData = "com.netcosports.kmm.cache:retrofit-cache-data:$cacheVersion"

        //annotation processor for <b>Retrofit service</b>
        const val retrofitCacheProcessor = "com.netcosports.kmm.cache:retrofit-cache-processor:$cacheVersion"


        //<b>KtorHttpClientWrapper</b> and <b>KtorHttpClientUtils</b>
        const val ktorCacheData = "com.netcosports.kmm.cache:ktor-cache-data:$cacheVersion"
    }
</pre>

# Generate wrapper for `Retrofit services`

  - Add `@CacheService` annotation for each `Retrofit service`, where cache is needed
<pre>
    @CacheService
    interface SampleCacheInterface {

        @GET
        fun getResponseSingle(@Url url: String): Single&lt;List&lt;T&gt;&gt;
        //Any <b>Single&lt;T&gt;</b> will be overridden to
        //fun getResponseSingle(@Url url: String): Loader&lt;Single&lt;List&lt;T&gt;&gt;&gt;

        @GET
        suspend fun getResponseSuspend(@Url url: String): List&lt;T&gt;
        //Any <b>suspend</b> fun will be overridden to
        //suspend fun getResponseSuspend(@Url url: String): Loader&lt;suspend () -> List&lt;T&gt;&gt;

        @GET
        fun getResponseObservable(@Url url: String): Observable&lt;List&lt;T&gt;&gt;
        //Any other <b>return type</b> will be copied without changes
    }
</pre>
  - Rebuild `app` or `module` with `Retrofit service` (or at least start `assemble` or `kapt{BuildType}Kotlin` task).

  For each annotated service will be generated kotlin file with name `{originalName}Wrapper`, for example `SampleCacheInterfaceWrapper.kt`.

##  Usage

  With `coroutines`
```
    val job: Job = viewModelScope.launch {
        val loader: Loader<suspend () -> SampleCacheEntity> = ktorHttpClientWrapper.createLoader { TODO(get<T>(url)) }
        //or
        val loader: Loader<suspend () -> SampleCacheEntity> = sampleCacheInterfaceWrapper.getResponseSuspend()
        if (zip) {
            suspendLoader { loaderArguments ->
                val firstAsync = viewModelScope.async {
                    loader.load(loaderArguments)()
                }
                val secondAsync = viewModelScope.async {
                    loader.load(loaderArguments)()
                }
                val first = firstAsync.await()
                val second = secondAsync.await()
                TODO(first to second)
            }
        } else {
            loader
        }
        .map { TODO() } //map T to R
        .change { data -> suspend { TODO(data()) } } //flatMap T to R
        .toFlow(mergeArguments) //get Flow<T> by passing MergeArguments
        .mapWrapperData { TODO() } //map T to R
        .mapWrapper { wrapper -> TODO(wrapper.isCache) } //map ResponseWrapper<T> to ResponseWrapper<R>
        .onStart {
            baseDataState.showLoading(forceReload)
        }
        .catch {
            baseDataState.handleError(it)
        }
        .onEach { (isCache, data) -> //ResponseWrapper<T>
            if (isCache) {
                if (data.isEmpty()) {
                    //do nothing, wait api
                } else {
                    baseDataState.handleContent(data)
                    baseDataState.showRefresh(true) //show loader for api
                }
            } else {
                baseDataState.handleContent(data)
            }
        }
        .onCompletion {
            baseDataState.showRefresh(false)
        }
        .collect()
```
  With `RX`
<pre>T
    val loader: Loader&lt;Single&lt;SampleCacheEntity&gt;&gt; = sampleCacheInterfaceWrapper.getResponseSingle()
    val disposable: Disposable = if (zip) {
            singleLoader { loaderArguments ->
                Single.zip(
                    loader.load(loaderArguments),
                    loader.load(loaderArguments),
                    { first, second -> TODO(first to second) }
                )
            }
        } else {
            loader
        }
        .map { "DATA" } //map T to R
        .change { single -> single.flatMap { data -> TODO(data) } } //flatMap T to R
        .toObservable(mergeArguments) //get Observable&lt;T&gt; by passing MergeArguments
        .mapWrapperData { TODO() } //map T to R
        .mapWrapper { wrapper -> TODO(wrapper.isCache) } //map ResponseWrapper&lt;T&gt; to ResponseWrapper&lt;R&gt;
        //<b>will not work without delayError = true</b>
        .observeOn(AndroidSchedulers.mainThread(), /*delayError =*/ true)
        .doOnSubscribe {
            baseDataState.showLoading(forceReload)
        }
        .subscribe({ (isCache, data) -> //ResponseWrapper&lt;T&gt;
            if (isCache) {
                if (data.isEmpty()) {
                    //do nothing, wait api
                } else {
                    baseDataState.handleContent(data)
                    baseDataState.showRefresh(true) //show loader for api
                }
            } else {
                baseDataState.baseDataState.handleContent(data)
            }
        }, {
            baseDataState.handleError(it)
        }, {
            baseDataState.showRefresh(false)
        })
</pre>
  - Get only cache or only api

  With `coroutines`
```
    val data: T = loader.api() //loader.cache()
```
  With `RX`
```
    val single: Single<T> = loader.api() //loader.cache()
```
  - Create your own
```
    val loader: Loader<T> = loader { loaderArguments ->
        TODO(loaderArguments)
    }
```