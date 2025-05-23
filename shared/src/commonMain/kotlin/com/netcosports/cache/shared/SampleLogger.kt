package com.netcosports.cache.shared

expect class SampleLogger {

    fun logD(message: String)

    fun logD(tag: String, message: String)
}