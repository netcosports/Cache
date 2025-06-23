package com.originsdigital.cache.shared

import com.originsdigital.cache.core.MergeArguments
import com.originsdigital.cache.ktx.CoroutineLoader
import com.originsdigital.cache.ktx.toFlow
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SampleKtorApiDataSource(
    platform: SamplePlatform,
    private val logger: SampleLogger
) {

    private val ktorHttpClientWrapper = platform.createKtorHttpClientWrapper { isCache ->
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        if (!isCache) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        this@SampleKtorApiDataSource.logger.log("Logging", message)
                    }
                }

                level = LogLevel.ALL
            }
        }
    }

    fun getRepositoriesLoader(url: String): CoroutineLoader<List<Any>> {
        return ktorHttpClientWrapper.createLoader {
            this/* Ktor */.get(url).body<List<SampleRepository>>()
        }
    }

    fun getRepositoriesFlow(): Flow<SampleIosResult> {
        return getRepositoriesLoader(url = "https://api.github.com/repositories")
            .toFlow(MergeArguments.CACHE_AND_API)
            .map { responseWrapper ->
                SampleIosResult(
                    text = "${if (responseWrapper.isCache) "CACHE" else "API"} getRepositoriesFlow=${responseWrapper.data.size}"
                )
            }
    }

    @Serializable
    private data class SampleRepository(val id: String)
}

data class SampleIosResult(val text: String)