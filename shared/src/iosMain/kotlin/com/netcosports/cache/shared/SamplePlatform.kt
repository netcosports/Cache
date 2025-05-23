package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientUtils
import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class SamplePlatform {

    actual val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    actual val bgDispatcher: CoroutineDispatcher = Dispatchers.Default

    actual fun createHttpClientWrapper(delegate: HttpClientConfig<*>.() -> Unit): KtorHttpClientWrapper {
        return KtorHttpClientUtils.createHttpClientWrapper(
            delegate = delegate
        )
    }
}