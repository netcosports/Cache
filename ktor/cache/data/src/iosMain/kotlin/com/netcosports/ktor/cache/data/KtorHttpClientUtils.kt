package com.netcosports.ktor.cache.data

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import platform.Foundation.NSURLRequestCachePolicy
import platform.Foundation.NSURLRequestReturnCacheDataDontLoad
import platform.Foundation.NSURLRequestUseProtocolCachePolicy

class KtorHttpClientUtils {

    companion object {

        const val DEFAULT_MEMORY_CACHE = 4L * 1024 * 1024
        const val DEFAULT_DISK_CAPACITY = 20L * 1024 * 1024


        fun createHttpClientWrapper(
            delegate: HttpClientConfig<*>.() -> Unit,
            memoryCache: Long = DEFAULT_MEMORY_CACHE,
            diskCapacity: Long = DEFAULT_DISK_CAPACITY
        ): KtorHttpClientWrapper {
            return createKtorApiWrapper(
                memoryCache = memoryCache,
                diskCapacity = diskCapacity
            ) { _, httpClientEngine ->
                HttpClient(httpClientEngine, delegate)
            }
        }

        fun <T> createKtorApiWrapper(
            memoryCache: Long = DEFAULT_MEMORY_CACHE,
            diskCapacity: Long = DEFAULT_DISK_CAPACITY,
            delegate: (isCached: Boolean, HttpClientEngine) -> T,
        ): KtorApiWrapper<T> {
            val cacheEngine = createHttpClientEngine(memoryCache, diskCapacity, true)
            val apiEngine = createHttpClientEngine(memoryCache, diskCapacity, false)
            return KtorApiWrapper(
                cache = delegate(true, cacheEngine),
                api = delegate(false, apiEngine)
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
//                    setHttpShouldUsePipelining(true) // <-- You can add this now!
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