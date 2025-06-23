package com.originsdigital.cachesample.data

import com.originsdigital.cache.retrofit.data.CacheService
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Url

@CacheService
interface SampleRetrofitService {

    @GET
    fun getResponseSingle(@Url url: String): Single<List<Any>>

    @GET
    fun getResponseObservable(@Url url: String): Observable<List<Any>>

    @GET
    suspend fun getResponseSuspend(@Url url: String): List<Any>
}