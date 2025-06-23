package com.originsdigital.ktor.cache.data

import com.originsdigital.okhttp.cache.data.OkHttpCacheInterceptor
import com.originsdigital.okhttp.cache.data.OkHttpClientUtils
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient

class KtorHttpClientUtils {

    companion object {

        fun createKtorHttpClientWrapper(
            okHttpClientDelegate: (isCache: Boolean) -> OkHttpClient,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE,
            configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
        ): KtorHttpClientWrapper {
            return createKtorApiWrapper(
                okHttpClientDelegate = okHttpClientDelegate,
                maxStale = maxStale
            ) { httpClientEngine, isCache ->
                HttpClient(httpClientEngine) {
                    configDelegate(this, isCache)
                }
            }
        }

        fun <API> createKtorApiWrapper(
            okHttpClientDelegate: (isCache: Boolean) -> OkHttpClient,
            maxStale: Long = OkHttpCacheInterceptor.DEFAULT_MAX_STALE,
            engineDelegate: (httpClientEngine: HttpClientEngine, isCache: Boolean) -> API
        ): KtorWrapper<API> {
            val cacheClientEngine = OkHttp.create {
                this.preconfigured = OkHttpClientUtils.setupCache(
                    okHttpClient = okHttpClientDelegate(true),
                    onlyCache = true,
                    maxStale = maxStale
                )
            }

            val apiClientEngine = OkHttp.create {
                this.preconfigured = OkHttpClientUtils.setupCache(
                    okHttpClient = okHttpClientDelegate(false),
                    onlyCache = false,
                    maxStale = maxStale
                )
            }

            return KtorWrapper(
                cache = engineDelegate(cacheClientEngine, true),
                api = engineDelegate(apiClientEngine, false)
            )
        }
    }
}