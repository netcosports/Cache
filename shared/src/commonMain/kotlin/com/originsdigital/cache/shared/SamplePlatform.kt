package com.originsdigital.cache.shared

import com.originsdigital.cache.ktor.data.KtorHttpClientWrapper
import io.ktor.client.HttpClientConfig

interface SamplePlatform {

    fun createKtorHttpClientWrapper(
        configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit
    ): KtorHttpClientWrapper
}