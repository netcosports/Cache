package com.netcosports.cache.shared

interface SampleLogger {

    fun log(tag: Any, text: Any?, error: Throwable? = null)
}