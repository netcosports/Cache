package com.netcosports.cachesample.data

import com.netcosports.cache.core.CoroutineLoader
import com.netcosports.cache.core.LoaderArguments
import com.netcosports.cache.core.RxLoader
import com.netcosports.cache.core.singleLoader
import com.netcosports.cache.core.suspendLoader
import io.reactivex.Single

class SampleRetrofitServiceManuallyWrapped<SERVICE>(
    private val retrofitDelegate: (isCache: Boolean) -> SERVICE,
) {

    val cacheService: SERVICE by lazy { retrofitDelegate(/*isCache = */true) }
    val apiService: SERVICE by lazy { retrofitDelegate(/*isCache = */false) }

    fun <RESULT : Any> coroutine(delegate: suspend SERVICE.() -> RESULT): CoroutineLoader<RESULT> {
        return suspendLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> delegate(apiService)
                is LoaderArguments.CACHE -> delegate(cacheService)
            }
        }
    }

    fun <RESULT : Any> single(delegate: SERVICE.() -> Single<RESULT>): RxLoader<RESULT> {
        return singleLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> delegate(apiService)
                is LoaderArguments.CACHE -> delegate(cacheService)
            }
        }
    }
}