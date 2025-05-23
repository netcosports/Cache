package com.netcosports.cachesample.data

import com.netcosports.cache.core.CoroutineLoader
import com.netcosports.cache.core.RxLoader
import com.netcosports.cache.shared.SampleKtorApiDataSource
import com.netcosports.cachesample.domain.SampleCacheRepository
import io.reactivex.Observable

class SampleCacheRepositoryImpl(
    private val retrofitServiceWrapper: SampleRetrofitServiceWrapper,
    private val ktorApiDataSource: SampleKtorApiDataSource
) : SampleCacheRepository {

    override fun getRetrofitSingleToLoader(url: String): RxLoader<List<Any>> {
        return retrofitServiceWrapper.getResponseSingle(url = url)
    }

    override fun getRetrofitSingleToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>
    ): RxLoader<List<Any>> {
        return getRetrofitSingleToLoader(url = url)
    }

    override fun getRetrofitObservable(url: String): Observable<List<Any>> {
        return retrofitServiceWrapper.getResponseObservable(url = url)
    }

    override fun getRetrofitSuspendToLoader(url: String): CoroutineLoader<List<Any>> {
        return retrofitServiceWrapper.getResponseSuspend(url = url)
    }

    override fun getRetrofitSuspendToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>
    ): CoroutineLoader<List<Any>> {
        return getRetrofitSuspendToLoader(url = url)
    }

    override fun getKtorLoader(url: String): CoroutineLoader<List<Any>> {
        return ktorApiDataSource.getRepositoriesLoader(url = url)
    }

    override fun getKtorSuspendToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>
    ): CoroutineLoader<List<Any>> {
        return getKtorLoader(url = url)
    }
}