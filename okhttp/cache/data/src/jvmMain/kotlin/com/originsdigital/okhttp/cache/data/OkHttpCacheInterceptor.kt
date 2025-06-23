package com.originsdigital.okhttp.cache.data

import okhttp3.Interceptor
import okhttp3.Response

class OkHttpCacheInterceptor(
    private val onlyCache: Boolean,
    private val maxStale: Long = DEFAULT_MAX_STALE
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (onlyCache) {
            chain.proceed(
                request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }

    companion object {
        const val DEFAULT_MAX_STALE: Long = 7 * 24 * 60 * 60 //7 days
    }
}