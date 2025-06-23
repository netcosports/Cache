package com.originsdigital.cachesample.domain

import com.originsdigital.cache.ktx.CoroutineLoader
import com.originsdigital.cache.rx.SingleLoader
import io.reactivex.Observable

interface SampleCacheRepository {

    fun getRetrofitSingleToLoader(url: String): SingleLoader<List<Any>>

    fun getRetrofitSingleToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>,
    ): SingleLoader<List<Any>>

    fun getRetrofitObservable(url: String): Observable<List<Any>>

    fun getRetrofitSuspendToLoader(url: String): CoroutineLoader<List<Any>>

    fun getRetrofitSuspendToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>,
    ): CoroutineLoader<List<Any>>

    fun getKtorLoader(url: String): CoroutineLoader<List<Any>>

    fun getKtorSuspendToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>,
    ): CoroutineLoader<List<Any>>
}