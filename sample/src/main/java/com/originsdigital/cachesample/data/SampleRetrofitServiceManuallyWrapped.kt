package com.originsdigital.cachesample.data

import com.originsdigital.cache.core.LoaderArguments
import com.originsdigital.cache.ktx.CoroutineLoader
import com.originsdigital.cache.ktx.suspendLoader
import com.originsdigital.cache.rx.SingleLoader
import com.originsdigital.cache.rx.singleLoader
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

    fun <RESULT : Any> single(delegate: SERVICE.() -> Single<RESULT>): SingleLoader<RESULT> {
        return singleLoader { loaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.API -> delegate(apiService)
                is LoaderArguments.CACHE -> delegate(cacheService)
            }
        }
    }
}