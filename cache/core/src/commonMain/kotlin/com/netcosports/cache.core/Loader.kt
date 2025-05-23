package com.netcosports.cache.core

class Loader<T>(private val cache: T, private val api: T) {

    fun load(loaderArguments: LoaderArguments): T {
        return when (loaderArguments) {
            is LoaderArguments.CACHE -> cache
            is LoaderArguments.API -> api
        }
    }
}

fun <T> loader(delegate: (loaderArguments: LoaderArguments) -> T): Loader<T> {
    return Loader(
        cache = delegate(LoaderArguments.getLoaderArgument(isCache = true)),
        api = delegate(LoaderArguments.getLoaderArgument(isCache = false))
    )
}