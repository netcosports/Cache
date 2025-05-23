package com.netcosports.cache.shared

import kotlin.coroutines.cancellation.CancellationException

fun sampleRethrowIfNeeded(exception: Exception) {
    if (exception is CancellationException) {
        throw exception
    }
}