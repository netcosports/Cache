package com.netcosports.okhttp.cache.data

import okhttp3.OkHttpClient

class OkHttpClientUtils {

    companion object {

        fun setupCache(
            okHttpClient: OkHttpClient,
            onlyCache: Boolean,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
        ): OkHttpClient {
            val builder = okHttpClient.newBuilder()
            if (onlyCache) {
                builder.interceptors().clear()
            }
            builder.addInterceptor(
                OkHttpCacheInterceptor(
                    onlyCache = onlyCache,
                    maxStale = maxStale
                )
            )
            return builder.build()
        }
    }
}