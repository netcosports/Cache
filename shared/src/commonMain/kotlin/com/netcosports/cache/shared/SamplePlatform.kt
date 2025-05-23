package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher

expect class SamplePlatform {

    val uiDispatcher: CoroutineDispatcher
    val bgDispatcher: CoroutineDispatcher

    fun createHttpClientWrapper(delegate: HttpClientConfig<*>.() -> Unit): KtorHttpClientWrapper
}