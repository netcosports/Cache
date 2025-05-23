package com.netcosports.cache.shared

import com.netcosports.ktor.cache.data.KtorHttpClientWrapper
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SampleCommonDataDI internal constructor(
    val platform: SamplePlatform,
    val logger: SampleLogger
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun provideApiDataSource(): SampleApiDataSource {
        return SampleApiDataSource(
            httpClientWrapper = provideHttpClient()
        )
    }

    private fun provideHttpClient(): KtorHttpClientWrapper {
        return platform.createHttpClientWrapper {
            install(ContentNegotiation) {
                json(json)
            }
//                install(Logging) {
//                    logger = object : Logger {
//                        override fun log(message: String) {
//                            this@SampleCommonDataDI.logger.logD(message)
//                        }
//                    }
//
//                    level = LogLevel.ALL
//                }
//            defaultRequest {
//                if ("localhost".equals(url.host, true)) {
//                    url.takeFrom(URLBuilder().takeFrom(config.api.baseUrl).apply {
//                        encodedPath += url.encodedPath
//                    })
//                }
//            }
        }
    }
}