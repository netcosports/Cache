package com.netcosports.cachesample.domain

import com.netcosports.cache.core.Loader
import com.netcosports.cache.shared.SampleCacheEntity
import io.reactivex.Observable
import io.reactivex.Single

interface SampleCacheRepository {

    fun getResponseSingle(): Loader<Single<SampleCacheEntity>>

    fun getResponseObservable(): Observable<SampleCacheEntity>

    fun getResponseSuspended(): Loader<suspend () -> SampleCacheEntity>

    fun getKtorResponseSuspended(): Loader<suspend () -> SampleCacheEntity>
}