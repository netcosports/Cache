package com.originsdigital.cache.okhttp.data

import okhttp3.Cache
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.setupCache(
    cache: Cache,
    onlyCache: Boolean,
    maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE,
): OkHttpClient.Builder {
    return cache(cache)
        .addInterceptor(
            OkHttpCacheInterceptor(
                onlyCache = onlyCache,
                maxStale = maxStale
            )
        )
}