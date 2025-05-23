package com.netcosports.cachesample.data

import com.netcosports.cache.core.CoroutineLoader
import com.netcosports.cache.core.SingleLoader
import com.netcosports.cache.shared.SampleKtorApiDataSource
import com.netcosports.cachesample.domain.SampleCacheRepository
import io.reactivex.Observable

class SampleCacheRepositoryImpl(
    private val manuallyWrappedRetrofit: SampleRetrofitServiceManuallyWrapped<SampleRetrofitService>,
    private val generatedRetrofitWrapper: SampleRetrofitServiceWrapper,
    private val ktorApiDataSource: SampleKtorApiDataSource
) : SampleCacheRepository {

    override fun getRetrofitSingleToLoader(url: String): SingleLoader<List<Any>> {
        return manuallyWrappedRetrofit.single { this/*SampleRetrofitServiceWrapper*/.getResponseSingle(url = url) }
        return generatedRetrofitWrapper.getResponseSingle(url = url)
    }

    override fun getRetrofitSingleToLoader(
        url: String,
        requestA: List<Any>,
        requestB: List<Any>
    ): SingleLoader<List<Any>> {
        return getRetrofitSingleToLoader(url = url)
    }

    override fun getRetrofitObservable(url: String): Observable<List<Any>> {
        return manuallyWrappedRetrofit.apiService.getResponseObservable(url = url)
        return generatedRetrofitWrapper.getResponseObservable(url = url)
    }

    override fun getRetrofitSuspendToLoader(url: String): CoroutineLoader<List<Any>> {
        return manuallyWrappedRetrofit.coroutine { this/*SampleRetrofitServiceWrapper*/.getResponseSuspend(url = url) }
        return generatedRetrofitWrapper.getResponseSuspend(url = url)
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