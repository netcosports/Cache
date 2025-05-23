package com.netcosports.retrofit.cache.data

import com.netcosports.okhttp.cache.data.OkHttpCacheInterceptor.Companion.DEFAULT_MAX_STALE
import com.netcosports.okhttp.cache.data.OkHttpClientUtils
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit

inline fun <reified SERVICE, WRAPPER> createServiceWrapper(
    retrofitBuilder: Retrofit.Builder,
    okHttpClientBuilder: OkHttpClient.Builder,
    cache: Cache,
    maxStale: Long = DEFAULT_MAX_STALE,
    createDelegate: (SERVICE, SERVICE) -> WRAPPER
): WRAPPER {
    val apiService = retrofitBuilder.client(
        OkHttpClientUtils.setupCache(okHttpClientBuilder, cache, onlyCache = false, maxStale = maxStale).build()
    )
        .build()
        .create(SERVICE::class.java)
    val cacheService = retrofitBuilder.client(
        OkHttpClientUtils.setupCache(okHttpClientBuilder, cache, onlyCache = true, maxStale = maxStale).build()
    )
        .build()
        .create(SERVICE::class.java)
    return createDelegate(cacheService, apiService)
}