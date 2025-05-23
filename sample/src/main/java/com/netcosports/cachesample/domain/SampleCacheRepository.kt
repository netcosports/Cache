package com.netcosports.cachesample.domain

import com.netcosports.cache.core.CoroutineLoader
import com.netcosports.cache.core.RxLoader
import io.reactivex.Observable

interface SampleCacheRepository {

    fun getRetrofitSingleToLoader(url: String): RxLoader<List<Any>>

    fun getRetrofitSingleToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>,
    ): RxLoader<List<Any>>

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