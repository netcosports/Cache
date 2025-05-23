package com.netcosports.ktor.cache.data

import com.netcosports.cache.core.Loader
import com.netcosports.cache.core.LoaderArguments
import com.netcosports.cache.core.suspendLoader
import io.ktor.client.*

typealias KtorHttpClientWrapper = KtorApiWrapper<HttpClient>

class KtorApiWrapper<T>(
    val cache: T,
    val api: T
) {

    fun <R> createLoader(delegate: suspend T.() -> R): Loader<suspend () -> R> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.CACHE -> delegate(cache)
                is LoaderArguments.API -> delegate(api)
            }
        }
    }
}