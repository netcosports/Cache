package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientUtils
import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient

actual class SamplePlatform(
    private val cache: Cache,
    private val okHttpClientBuilderDelegate: (isCache: Boolean) -> OkHttpClient.Builder
) {

    actual val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    actual val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    actual fun createHttpClientWrapper(delegate: HttpClientConfig<*>.() -> Unit): KtorHttpClientWrapper {
        return KtorHttpClientUtils.createHttpClientWrapper(
            okHttpClientBuilderDelegate = okHttpClientBuilderDelegate,
            cache = cache,
            delegate = delegate
        )
    }
}