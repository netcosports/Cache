package com.netcosports.cachesample.data

import com.netcosports.cache.core.Loader
import com.netcosports.cache.core.map
import com.netcosports.cache.shared.SampleApiDataSource
import com.netcosports.cache.shared.SampleCacheEntity
import com.netcosports.cachesample.domain.SampleCacheRepository
import io.reactivex.Observable
import io.reactivex.Single

class SampleCacheRepositoryImpl(
    private val retrofitWrapper: SampleCacheInterfaceWrapper,
    private val apiDataSource: SampleApiDataSource
) : SampleCacheRepository {

    override fun getResponseSingle(): Loader<Single<SampleCacheEntity>> {
        return retrofitWrapper.getResponseSingle(TEST_URL)
            .map { SampleCacheEntity("data") }
    }

    override fun getResponseObservable(): Observable<SampleCacheEntity> {
        return retrofitWrapper.getResponseObservable(TEST_URL)
            .map { SampleCacheEntity("data") }
    }

    override fun getResponseSuspended(): Loader<suspend () -> SampleCacheEntity> {
        return retrofitWrapper.getResponseSuspend(TEST_URL)
            .map { SampleCacheEntity("suspended data") }
    }

    override fun getKtorResponseSuspended(): Loader<suspend () -> SampleCacheEntity> {
        return apiDataSource.getResponsesSuspended()
    }

    companion object {
        private const val TEST_URL = "https://api.github.com/repositories"
    }
}