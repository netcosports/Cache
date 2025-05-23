package com.netcosports.cache.shared

import platform.Foundation.NSLog

class SampleIosLogger : SampleLogger {

    override fun log(tag: Any, text: Any?, error: Throwable?) {
        val tagValue = if (tag is String) {
            tag
        } else {
            "${tag::class.simpleName}(${tag.hashCode()})"
        }
        NSLog(
            "SampleIosLogger = $tagValue = ${
                listOfNotNull(
                    text,
                    error?.toString()
                ).joinToString(" = ")
            }"
        )
        error?.stackTraceToString()?.let { NSLog(it) }
    }
}