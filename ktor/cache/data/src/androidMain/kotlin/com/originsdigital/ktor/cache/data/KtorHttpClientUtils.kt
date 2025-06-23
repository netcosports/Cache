package com.originsdigital.ktor.cache.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient

class KtorHttpClientUtils {

    companion object {

        fun createKtorHttpClientWrapper(
            okHttpClientDelegate: (isCache: Boolean) -> OkHttpClient,
            configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
        ): KtorHttpClientWrapper {
            return createKtorApiWrapper(
                okHttpClientDelegate = okHttpClientDelegate
            ) { httpClientEngine, isCache ->
                HttpClient(httpClientEngine) {
                    configDelegate(this, isCache)
                }
            }
        }

        fun <API> createKtorApiWrapper(
            okHttpClientDelegate: (isCache: Boolean) -> OkHttpClient,
            engineDelegate: (httpClientEngine: HttpClientEngine, isCache: Boolean) -> API
        ): KtorWrapper<API> {
            val cacheClientEngine = OkHttp.create {
                this.preconfigured = okHttpClientDelegate(true)
            }
            val apiClientEngine = OkHttp.create {
                this.preconfigured = okHttpClientDelegate(false)
            }

            return KtorWrapper(
                cacheClient = engineDelegate(cacheClientEngine, true),
                apiClient = engineDelegate(apiClientEngine, false)
            )
        }
    }
}