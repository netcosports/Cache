package com.netcosports.cache.core

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope

class CoroutineLoader<DATA> internal constructor(
    internal val cacheBlock: suspend () -> DATA,
    internal val apiBlock: suspend () -> DATA
) {

    suspend fun load(loaderArguments: LoaderArguments): DATA {
        return when (loaderArguments) {
            is LoaderArguments.CACHE -> cacheBlock()
            is LoaderArguments.API -> apiBlock()
        }
    }
}

fun <DATA> suspendLoader(
    delegate: suspend (loaderArguments: LoaderArguments) -> DATA
): CoroutineLoader<DATA> {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    return CoroutineLoader(
        cacheBlock = suspend { delegate(LoaderArguments.getLoaderArgument(isCache = true)) },
        apiBlock = suspend { delegate(LoaderArguments.getLoaderArgument(isCache = false)) },
    )
}

fun <FROM, TO> CoroutineLoader<FROM>.map(
    mapper: suspend (FROM) -> TO
): CoroutineLoader<TO> {
    return suspendLoader { loaderArguments ->
        val originalValue = load(loaderArguments = loaderArguments)
        mapper(originalValue)
    }
}

suspend fun <DATA> CoroutineLoader<DATA>.cache(): DATA = cacheBlock()
suspend fun <DATA> CoroutineLoader<DATA>.api(): DATA = apiBlock()

fun <DATA> CoroutineLoader<DATA>.toFlow(
    mergeArguments: MergeArguments
): Flow<ResponseWrapper<DATA>> {
    return flow {
        when (mergeArguments) {
            MergeArguments.ONLY_API -> {
                // do not ignore error from api
                emit(ResponseWrapper(isCache = false, data = api()))
            }

            MergeArguments.ONLY_CACHE -> {
                // do not ignore error from cache
                emit(ResponseWrapper(isCache = true, data = cache()))
            }

            MergeArguments.CACHE_AND_API -> {
                supervisorScope {
                    val cacheAsync = async { cache() }
                    val apiAsync = async { api() }

                    try {
                        emit(ResponseWrapper(isCache = true, data = cacheAsync.await()))
                    } catch (ignore: Exception) {
                        // ignore error from cache
                    }

                    // do not ignore error from api
                    emit(ResponseWrapper(isCache = false, data = apiAsync.await()))
                }
            }

            MergeArguments.CACHE_OR_API -> {
                // ignore error from cache
                try {
                    emit(ResponseWrapper(isCache = true, data = cache()))
                } catch (ignore: Exception) {
                    emit(ResponseWrapper(isCache = false, data = api()))
                }
            }

            MergeArguments.API_OR_CACHE -> {
                // ignore error from cache, throw api error instead
                try {
                    emit(ResponseWrapper(isCache = false, data = api()))
                } catch (ignoreApi: Exception) {
                    try {
                        emit(ResponseWrapper(isCache = true, data = cache()))
                    } catch (ignoreCache: Exception) {
                        throw ignoreApi
                    }
                }
            }
        }
    }
}
