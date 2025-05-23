package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientUtils
import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.HttpClientConfig

class SampleIosPlatform : SamplePlatform {

    override fun createKtorHttpClientWrapper(
        configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
    ): KtorHttpClientWrapper {
        return KtorHttpClientUtils.createKtorHttpClientWrapper(
            configDelegate = configDelegate
        )
    }
}