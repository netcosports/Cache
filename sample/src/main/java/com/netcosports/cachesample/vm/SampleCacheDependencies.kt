package com.netcosports.cachesample.vm

import android.app.Application
import com.google.gson.GsonBuilder
import com.netcosports.cache.shared.SampleDataDI
import com.netcosports.cache.shared.SampleLogger
import com.netcosports.cache.shared.SamplePlatform
import com.netcosports.cachesample.FlipperSetup
import com.netcosports.cachesample.data.SampleCacheInterface
import com.netcosports.cachesample.data.SampleCacheInterfaceWrapper
import com.netcosports.cachesample.data.SampleCacheRepositoryImpl
import com.netcosports.cachesample.domain.SampleCacheRepository
import com.netcosports.okhttp.cache.data.OkHttpClientUtils
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class SampleCacheDependencies(private val app: Application) {

    private val cache by lazy {
        val httpCacheDirectory = File(app.cacheDir, "cache")
        val cacheSize = 10L * 1024 * 1024 // 10 MiB

        Cache(httpCacheDirectory, cacheSize)
    }
    private val platform = SamplePlatform(cache) { isCache ->
        OkHttpClient.Builder().apply {
            if (!isCache) {
                FlipperSetup.setupOkHttpClientBuilder(this)
            }
        }
    }
    private val logger = SampleLogger()
    private val sampleDI = SampleDataDI(platform, logger)
    private val gson by lazy { GsonBuilder().setLenient().create() }

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
    }

    private fun getOkHttpClientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()

    //or
    private val cacheService: SampleCacheInterface by lazy {
        val okHttpClient = OkHttpClientUtils.setupCache(getOkHttpClientBuilder(), cache, onlyCache = true).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

        retrofit.create(SampleCacheInterface::class.java)
    }
    private val apiService: SampleCacheInterface by lazy {
        val okHttpClient = OkHttpClientUtils.setupCache(getOkHttpClientBuilder(), cache, onlyCache = false).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

        retrofit.create(SampleCacheInterface::class.java)
    }

    private val retrofitWrapper: SampleCacheInterfaceWrapper by lazy {
        SampleCacheInterfaceWrapper(cacheService, apiService)
        //or
        SampleCacheInterfaceWrapper.create(retrofitBuilder, getOkHttpClientBuilder(), cache)
    }

    val repository: SampleCacheRepository by lazy {
        SampleCacheRepositoryImpl(
            retrofitWrapper = retrofitWrapper,
            apiDataSource = sampleDI.commonDataDI.provideApiDataSource()
        )
    }
}