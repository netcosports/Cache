package com.netcosports.ktor.cache.data

import com.netcosports.okhttp.cache.data.OkHttpCacheInterceptor
import com.netcosports.okhttp.cache.data.OkHttpClientUtils
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import okhttp3.Cache
import okhttp3.OkHttpClient

class KtorHttpClientUtils {

    companion object {

        fun createHttpClientWrapper(
            okHttpClientBuilderDelegate: (isCache: Boolean) -> OkHttpClient.Builder,
            cache: Cache,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE,
            delegate: HttpClientConfig<*>.() -> Unit
        ): KtorHttpClientWrapper {
            return createKtorApiWrapper(
                okHttpClientBuilderDelegate,
                cache,
                maxStale
            ) { _, engine ->
                HttpClient(engine, delegate)
            }
        }

        fun <T> createKtorApiWrapper(
            okHttpClientBuilderDelegate: (isCache: Boolean) -> OkHttpClient.Builder,
            cache: Cache,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE,
            delegate: (isCached: Boolean, HttpClientEngine) -> T
        ): KtorApiWrapper<T> {

            val cacheClientEngine = OkHttp.create {
                this.preconfigured = OkHttpClientUtils.setupCache(
                    okHttpClientBuilderDelegate(true),
                    cache,
                    onlyCache = true,
                    maxStale = maxStale
                ).build()
            }

            val apiClientEngine =
                OkHttp.create {
                    this.preconfigured = OkHttpClientUtils.setupCache(
                        okHttpClientBuilderDelegate(false),
                        cache,
                        onlyCache = false,
                        maxStale = maxStale
                    ).build()
                }

            return KtorApiWrapper(
                cache = delegate(true, cacheClientEngine),
                api = delegate(false, apiClientEngine)
            )
        }
    }
}