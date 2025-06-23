package com.originsdigital.ktor.cache.data

import com.originsdigital.cache.core.CoroutineLoader
import com.originsdigital.cache.core.LoaderArguments
import com.originsdigital.cache.core.suspendLoader
import io.ktor.client.HttpClient

typealias KtorHttpClientWrapper = KtorWrapper<HttpClient>

class KtorWrapper<CLIENT>(
    val cache: CLIENT,
    val api: CLIENT
) {

    fun <DATA> createLoader(delegate: suspend CLIENT.() -> DATA): CoroutineLoader<DATA> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.CACHE -> delegate(cache)
                is LoaderArguments.API -> delegate(api)
            }
        }
    }
}