package com.netcosports.cache.core

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope

fun <T> suspendLoader(delegate: suspend (loaderArguments: LoaderArguments) -> T): Loader<suspend () -> T> {
    return loader { loaderArguments ->
        suspend { delegate(loaderArguments) }
    }
}

@Deprecated("Use map")
fun <FROM, TO> Loader<suspend () -> FROM>.change(
    delegate: (suspend () -> FROM) -> suspend () -> TO
): Loader<suspend () -> TO> {
    return suspendLoader { loaderArguments ->
        delegate(load(loaderArguments = loaderArguments))()
    }
}

fun <FROM, TO> Loader<suspend () -> FROM>.map(
    mapper: suspend (FROM) -> TO
): Loader<suspend () -> TO> {
    return suspendLoader { loaderArguments ->
        mapper(load(loaderArguments = loaderArguments)())
    }
}

suspend fun <T> Loader<suspend () -> T>.cache(): T {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_REFERENCE", "INVISIBLE_SETTER")
    return load(LoaderArguments.getLoaderArgument(isCache = true))()
}

suspend fun <T> Loader<suspend () -> T>.api(): T {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_REFERENCE", "INVISIBLE_SETTER")
    return load(LoaderArguments.getLoaderArgument(isCache = false))()
}

fun <T> Loader<suspend () -> T>.toFlow(mergeArguments: MergeArguments): Flow<ResponseWrapper<T>> {
    return flow {
        @Suppress("UNUSED_VARIABLE")
        val result = when (mergeArguments) {
            MergeArguments.ONLY_API -> emit(ResponseWrapper(isCache = false, data = api()))
            MergeArguments.ONLY_CACHE -> emit(ResponseWrapper(isCache = true, data = cache()))
            MergeArguments.CACHE_AND_API -> {
                supervisorScope {
                    val cacheAsync = async { cache() }
                    val apiAsync = async { api() }

                    try {
                        emit(ResponseWrapper(isCache = true, data = cacheAsync.await()))
                    } catch (ignore: Exception) {
                        //ignore cache error
                    }

                    emit(ResponseWrapper(isCache = false, data = apiAsync.await()))
                }
            }
            MergeArguments.CACHE_OR_API -> {
                try {
                    emit(ResponseWrapper(isCache = true, data = cache()))
                } catch (ignore: Exception) {
                    emit(ResponseWrapper(isCache = false, data = api()))
                }
            }
            MergeArguments.API_OR_CACHE -> {
                try {
                    emit(ResponseWrapper(isCache = false, data = api()))
                } catch (ignoreApi: Exception) {
                    try {
                        emit(ResponseWrapper(isCache = true, data = cache()))
                    } catch (ignoreCache: Exception) {
                        throw ignoreApi //throw api error if no cache
                    }
                }
            }
        }
    }
}
