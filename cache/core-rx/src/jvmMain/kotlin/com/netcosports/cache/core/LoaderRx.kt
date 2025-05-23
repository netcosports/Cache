@file:Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER")

package com.netcosports.cache.core

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function

fun <T> singleLoader(delegate: (loaderArguments: LoaderArguments) -> Single<T>): Loader<Single<T>> {
    return loader { loaderArguments ->
        delegate(loaderArguments)
    }
}

fun <FROM, TO> Loader<Single<FROM>>.change(delegate: (Single<FROM>) -> Single<TO>): Loader<Single<TO>> {
    return singleLoader { loaderArguments ->
        delegate(load(loaderArguments = loaderArguments))
    }
}

fun <FROM, TO> Loader<Single<FROM>>.map(mapper: (FROM) -> TO): Loader<Single<TO>> {
    return change { single -> single.map { data -> mapper(data) } }
}

fun <T> Loader<Single<T>>.cache(): Single<T> {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_REFERENCE", "INVISIBLE_SETTER")
    return load(loaderArguments = LoaderArguments.getLoaderArgument(isCache = true))
}

fun <T> Loader<Single<T>>.api(): Single<T> {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_REFERENCE", "INVISIBLE_SETTER")
    return load(loaderArguments = LoaderArguments.getLoaderArgument(isCache = false))
}

fun <T : Any> Loader<Single<T>>.toObservable(mergeArguments: MergeArguments): Observable<ResponseWrapper<T>> {
    return when (mergeArguments) {
        MergeArguments.ONLY_API -> { //do not ignore error
            api()
                .map { ResponseWrapper(isCache = false, data = it) }
                .toObservable()
        }
        MergeArguments.ONLY_CACHE -> { //do not ignore error
            cache()
                .map { ResponseWrapper<T>(isCache = true, data = it) }
                .toObservable()
        }
        MergeArguments.CACHE_AND_API -> {
            val apiRequestWrapped = api()
                .map { ResponseWrapper(isCache = false, data = it) }
                .toObservable()
//                .onErrorResumeNext(Function { Observable.empty() }) //do not ignore error from api
            val cacheRequestWrapper = cache()
                .map { ResponseWrapper<T>(isCache = true, data = it) }
                .toObservable()
                .onErrorResumeNext(Function { Observable.empty() }) //ignore error from cache

            Observable.concat(cacheRequestWrapper, apiRequestWrapped)
        }
        MergeArguments.CACHE_OR_API -> {
            cache()
                .map { ResponseWrapper(isCache = true, data = it) }
                .toObservable()
                .onErrorResumeNext(Function {
                    api()
                        .map { ResponseWrapper(isCache = false, data = it) }
                        .toObservable()
                })
        }
        MergeArguments.API_OR_CACHE -> {
            api()
                .map { ResponseWrapper(isCache = false, data = it) }
                .toObservable()
                .onErrorResumeNext(Function { apiThrowable ->
                    cache()
                        .map { ResponseWrapper(isCache = true, data = it) }
                        .toObservable()
                        .onErrorResumeNext(Function { throw apiThrowable })
                })
        }
    }
}