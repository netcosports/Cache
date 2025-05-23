package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.HttpClientConfig

interface SamplePlatform {

    fun createKtorHttpClientWrapper(
        configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
    ): KtorHttpClientWrapper
}