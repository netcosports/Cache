package com.netcosports.retrofit.cache.data

import com.netcosports.okhttp.cache.data.OkHttpCacheInterceptor.Companion.DEFAULT_MAX_STALE
import com.netcosports.okhttp.cache.data.OkHttpClientUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit

inline fun <reified SERVICE, WRAPPER> createServiceWrapper(
    retrofitBuilder: Retrofit.Builder,
    okHttpClient: OkHttpClient,
    maxStale: Long = DEFAULT_MAX_STALE,
    createDelegate: (cacheService: SERVICE, apiService: SERVICE) -> WRAPPER
): WRAPPER {
    val apiService = retrofitBuilder.client(
        OkHttpClientUtils.setupCache(
            okHttpClient = okHttpClient,
            onlyCache = false,
            maxStale = maxStale
        )
    )
        .build()
        .create(SERVICE::class.java)

    val cacheService = retrofitBuilder.client(
        OkHttpClientUtils.setupCache(
            okHttpClient = okHttpClient,
            onlyCache = true,
            maxStale = maxStale
        )
    )
        .build()
        .create(SERVICE::class.java)

    return createDelegate(cacheService, apiService)
}