package com.netcosports.okhttp.cache.data

import okhttp3.Cache
import okhttp3.OkHttpClient

class OkHttpClientUtils {

    companion object {

        fun setupCache(
            builder: OkHttpClient.Builder,
            cache: Cache, onlyCache: Boolean,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
        ): OkHttpClient.Builder {
            if (onlyCache) {
                builder.interceptors().clear()
            }
            builder.addInterceptor(OkHttpCacheInterceptor(onlyCache = onlyCache, maxStale = maxStale))
            return builder.cache(cache)
        }
    }
}