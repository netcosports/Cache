package com.originsdigital.cachesample.di

import android.app.Application
import com.google.gson.GsonBuilder
import com.originsdigital.cache.shared.SampleAndroidLogger
import com.originsdigital.cache.shared.SampleAndroidPlatform
import com.originsdigital.cache.shared.SampleKtorApiDataSource
import com.originsdigital.cachesample.data.SampleCacheRepositoryImpl
import com.originsdigital.cachesample.data.SampleRetrofitService
import com.originsdigital.cachesample.data.SampleRetrofitServiceManuallyWrapped
import com.originsdigital.cachesample.data.SampleRetrofitServiceWrapper
import com.originsdigital.cachesample.domain.SampleCacheRepository
import com.originsdigital.cache.okhttp.data.OkHttpCacheInterceptor
import com.originsdigital.cache.okhttp.data.setupCache
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class SampleCacheDependencies(app: Application) {

    private val cache = Cache(
        directory = File(app.cacheDir, "cache"),
        maxSize = 10L * 1024 * 1024, // 10 MiB
    )
    private val baseOkHttpClient = OkHttpClient.Builder().build()
    private val cacheOkHttpClient = baseOkHttpClient.newBuilder()
        .setupCache(
            cache = cache,
            onlyCache = true,
            maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
        )
        .build()
    private val apiOkHttpClient = baseOkHttpClient.newBuilder()
        .setupCache(
            cache = cache,
            onlyCache = false,
            maxStale = OkHttpCacheInterceptor.DEFAULT_MAX_STALE
        )
        .build()

    private val gson = GsonBuilder().setLenient().create()
    val logger = SampleAndroidLogger()

    val repository: SampleCacheRepository = SampleCacheRepositoryImpl(
        manuallyWrappedRetrofit = createManuallyWrappedRetrofit(),
        generatedRetrofitWrapper = createGeneratedRetrofitWrapper(),
        ktorApiDataSource = createKtorApiDataSource()
    )

    private fun createKtorApiDataSource(): SampleKtorApiDataSource {
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

    private fun createManuallyWrappedRetrofit(): SampleRetrofitServiceManuallyWrapped<SampleRetrofitService> {
        return SampleRetrofitServiceManuallyWrapped(
            retrofitDelegate = { isCache ->
                createRetrofitClient(
                    if (isCache) {
                        cacheOkHttpClient
                    } else {
                        apiOkHttpClient
                    }
                )
            }
        )
    }

    @Suppress("UNREACHABLE_CODE")
    private fun createGeneratedRetrofitWrapper(): SampleRetrofitServiceWrapper {
        return SampleRetrofitServiceWrapper(
            createRetrofitClient(cacheOkHttpClient),
            createRetrofitClient(apiOkHttpClient)
        )
    }

    private fun createRetrofitClient(okHttpClient: OkHttpClient): SampleRetrofitService {
        return Retrofit.Builder()
            .baseUrl("https://api.curator.io/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(SampleRetrofitService::class.java)
    }
}