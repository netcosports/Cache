package com.originsdigital.cachesample.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.originsdigital.cache.shared.SampleAndroidLogger
import com.originsdigital.cache.shared.SampleAndroidPlatform
import com.originsdigital.cache.shared.SampleKtorApiDataSource
import com.originsdigital.cachesample.data.SampleCacheRepositoryImpl
import com.originsdigital.cachesample.data.SampleRetrofitService
import com.originsdigital.cachesample.data.SampleRetrofitServiceManuallyWrapped
import com.originsdigital.cachesample.data.SampleRetrofitServiceWrapper
import com.originsdigital.cachesample.domain.SampleCacheRepository
import com.originsdigital.okhttp.cache.data.OkHttpClientUtils
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class SampleCacheDependencies(app: Application) {

    private val cacheOkHttpClient = OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(app.cacheDir, "cache"),
                maxSize = 10L * 1024 * 1024, // 10 MiB
            )
        ).build()
    private val apiOkHttpClient = cacheOkHttpClient.newBuilder()
        .addInterceptor(ChuckerInterceptor(app))
        .build()
    private val gson = GsonBuilder().setLenient().create()

    val logger = SampleAndroidLogger()

    val repository: SampleCacheRepository = SampleCacheRepositoryImpl(
        manuallyWrappedRetrofit = getManuallyWrappedRetrofit(),
        generatedRetrofitWrapper = getGeneratedRetrofitWrapper(),
        ktorApiDataSource = getKtorApiDataSource()
    )

    private fun getKtorApiDataSource(): SampleKtorApiDataSource {
        return SampleKtorApiDataSource(
            platform = SampleAndroidPlatform { isCache ->
                if (isCache) {
                    cacheOkHttpClient
                } else {
                    apiOkHttpClient
                }
            },
            logger = logger
        )
    }

    private fun getManuallyWrappedRetrofit(): SampleRetrofitServiceManuallyWrapped<SampleRetrofitService> {
        return SampleRetrofitServiceManuallyWrapped(
            retrofitDelegate = { isCache ->
                Retrofit.Builder()
                    .baseUrl("https://api.curator.io/v1/")
                    .client(
                        if (isCache) {
                            cacheOkHttpClient
                        } else {
                            apiOkHttpClient
                        }
                    )
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build()
                    .create(SampleRetrofitService::class.java)
            }
        )
    }

    @Suppress("UNREACHABLE_CODE")
    private fun getGeneratedRetrofitWrapper(): SampleRetrofitServiceWrapper {
        // just provide Retrofit.Builder with OkHttpClient and get the Wrapper
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))

        return SampleRetrofitServiceWrapper.create(retrofitBuilder, apiOkHttpClient)

        // or create the Wrapper yourself if you need different interceptors for cache and api and etc
        val cacheService = Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .client(
                OkHttpClientUtils.setupCache(
                    okHttpClient = cacheOkHttpClient,
                    onlyCache = true
                )
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(SampleRetrofitService::class.java)

        val apiService = Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .client(
                OkHttpClientUtils.setupCache(
                    okHttpClient = apiOkHttpClient,
                    onlyCache = false
                )
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(SampleRetrofitService::class.java)

        return SampleRetrofitServiceWrapper(cacheService, apiService)
    }
}