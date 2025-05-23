package com.netcosports.cache.core

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function

class RxLoader<DATA> internal constructor(
    internal val cacheSingle: Single<DATA>,
    internal val apiSingle: Single<DATA>
) {

    fun load(loaderArguments: LoaderArguments): Single<DATA> {
        return when (loaderArguments) {
            is LoaderArguments.CACHE -> cacheSingle
            is LoaderArguments.API -> apiSingle
        }
    }
}

fun <DATA> singleLoader(
    delegate: (loaderArguments: LoaderArguments) -> Single<DATA>
): RxLoader<DATA> {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    return RxLoader(
        cacheSingle = delegate(LoaderArguments.getLoaderArgument(isCache = true)),
        apiSingle = delegate(LoaderArguments.getLoaderArgument(isCache = false))
    )
}

fun <FROM, TO> RxLoader<FROM>.change(
    delegate: Single<FROM>.() -> Single<TO>
): RxLoader<TO> {
    return singleLoader { loaderArguments ->
        val originalSingle = load(loaderArguments = loaderArguments)
        delegate(originalSingle)
    }
}

fun <FROM, TO> RxLoader<FROM>.map(mapper: (FROM) -> TO): RxLoader<TO> {
    return change { /*single.*/map { data -> mapper(data) } }
}

fun <DATA> RxLoader<DATA>.cache(): Single<DATA> = cacheSingle
fun <DATA> RxLoader<DATA>.api(): Single<DATA> = apiSingle

fun <DATA : Any> RxLoader<DATA>.toObservable(
    mergeArguments: MergeArguments
): Observable<ResponseWrapper<DATA>> {
    return when (mergeArguments) {
        MergeArguments.ONLY_API -> {
            // do not ignore error from api
            api()
                .map { apiData -> ResponseWrapper(isCache = false, data = apiData) }
                .toObservable()
        }

        MergeArguments.ONLY_CACHE -> {
            // do not ignore error from cache
            cache()
                .map { cacheData -> ResponseWrapper(isCache = true, data = cacheData) }
                .toObservable()
        }

        MergeArguments.CACHE_AND_API -> {
            val cacheRequest = cache()
                .map { cacheData -> ResponseWrapper(isCache = true, data = cacheData) }
                .toObservable()
                .onErrorResumeNext(Function { Observable.empty() }) // ignore error from cache

            // do not ignore error from api
            val apiRequest = api()
                .map { apiData -> ResponseWrapper(isCache = false, data = apiData) }
                .toObservable()

            Observable.concat(cacheRequest, apiRequest)
        }

        MergeArguments.CACHE_OR_API -> {
            // ignore error from cache
            cache()
                .map { cacheData -> ResponseWrapper(isCache = true, data = cacheData) }
                .toObservable()
                .onErrorResumeNext(Function {
                    api()
                        .map { apiData -> ResponseWrapper(isCache = false, data = apiData) }
                        .toObservable()
                })
        }

        MergeArguments.API_OR_CACHE -> {
            // ignore error from cache, throw api error instead
            api()
                .map { apiData -> ResponseWrapper(isCache = false, data = apiData) }
                .toObservable()
                .onErrorResumeNext(Function { apiThrowable ->
                    cache()
                        .map { cacheData -> ResponseWrapper(isCache = true, data = cacheData) }
                        .toObservable()
                        .onErrorResumeNext(Function { throw apiThrowable })
                })
        }
    }
}