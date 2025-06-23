package com.originsdigital.cache.ktor.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLRequestCachePolicy
import platform.Foundation.NSURLRequestReturnCacheDataDontLoad
import platform.Foundation.NSURLRequestUseProtocolCachePolicy

class KtorHttpClientUtils {

    companion object {

        const val DEFAULT_MEMORY_CACHE = 4L * 1024 * 1024
        const val DEFAULT_DISK_CAPACITY = 20L * 1024 * 1024

        fun createKtorHttpClientWrapper(
            memoryCache: Long = DEFAULT_MEMORY_CACHE,
            diskCapacity: Long = DEFAULT_DISK_CAPACITY,
            configDelegate: HttpClientConfig<*>.(isCache: Boolean) -> Unit,
        ): KtorHttpClientWrapper {
            return createKtorApiWrapper(
                memoryCache = memoryCache,
                diskCapacity = diskCapacity
            ) { httpClientEngine, isCache ->
                HttpClient(httpClientEngine) { configDelegate(this, isCache) }
            }
        }

        fun <API> createKtorApiWrapper(
            memoryCache: Long = DEFAULT_MEMORY_CACHE,
            diskCapacity: Long = DEFAULT_DISK_CAPACITY,
            engineDelegate: (httpClientEngine: HttpClientEngine, isCache: Boolean) -> API,
        ): KtorWrapper<API> {
            val cacheEngine = createHttpClientEngine(
                memoryCache = memoryCache,
                diskCapacity = diskCapacity,
                onlyCache = true
            )
            val apiEngine = createHttpClientEngine(
                memoryCache = memoryCache,
                diskCapacity = diskCapacity,
                onlyCache = false
            )
            return KtorWrapper(
                cacheClient = engineDelegate(cacheEngine, true),
                apiClient = engineDelegate(apiEngine, false)
            )
        }

        fun createHttpClientEngine(
            memoryCache: Long,
            diskCapacity: Long,
            onlyCache: Boolean
        ): HttpClientEngine {
            return Darwin.create {
                val cachePolicy: NSURLRequestCachePolicy = if (onlyCache) {
                    NSURLRequestReturnCacheDataDontLoad
                } else {
                    NSURLRequestUseProtocolCachePolicy
                }
                configureRequest {
                    setCachePolicy(cachePolicy)
                }
                configureSession {
                    setURLCache(this.URLCache()?.apply {
                        setMemoryCapacity(memoryCache.toULong())
                        setDiskCapacity(diskCapacity.toULong())
                    })
                    setRequestCachePolicy(cachePolicy)
                }
            }
        }
    }
}