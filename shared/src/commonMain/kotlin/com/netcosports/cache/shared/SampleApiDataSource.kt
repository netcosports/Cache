package com.netcosports.cache.shared

import com.netcosports.cache.core.*
import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow

class SampleApiDataSource(
    private val httpClientWrapper: KtorHttpClientWrapper
) {

    suspend fun getResponseCache(): SampleCacheEntity {
        return getSample(httpClientWrapper.cache)
            .let { SampleCacheEntity("getResponseCache") }
    }

    suspend fun getResponseApi(): SampleCacheEntity {
        return getSample(httpClientWrapper.api)
            .let { SampleCacheEntity("getResponseApi") }
    }

    fun getResponsesFlow(): Flow<ResponseWrapper<SampleCacheEntity>> {
        return httpClientWrapper.createLoader {
            getSample(this)
        }
            .map { SampleCacheEntity("getResponsesSuspended") }
            .toFlow(MergeArguments.CACHE_AND_API)
    }

    fun getResponsesSuspended(): Loader<suspend () -> SampleCacheEntity> {
        return httpClientWrapper.createLoader {
            getSample(this)
        }
            .map { SampleCacheEntity("getResponsesSuspended") }
    }

    private suspend fun getSample(httpClient: HttpClient): HttpResponse {
        return httpClient.get(TEST_URL)
    }

    companion object {
        private const val TEST_URL = "https://api.github.com/repositories"
    }
}