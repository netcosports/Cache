package com.originsdigital.cache.shared

import com.originsdigital.ktor.cache.data.KtorHttpClientUtils
import com.originsdigital.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.HttpClientConfig
import okhttp3.OkHttpClient

class SampleAndroidPlatform(
    private val okHttpClientDelegate: (isCache: Boolean) -> OkHttpClient
) : SamplePlatform {

    override fun createKtorHttpClientWrapper(
        configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
    ): KtorHttpClientWrapper {
        return KtorHttpClientUtils.createKtorHttpClientWrapper(
            okHttpClientDelegate = okHttpClientDelegate,
            configDelegate = configDelegate
        )
    }
}