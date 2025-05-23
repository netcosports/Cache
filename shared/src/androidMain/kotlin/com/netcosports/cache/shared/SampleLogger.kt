package com.netcosports.cache.shared

import android.util.Log

actual class SampleLogger {

    actual fun logD(message: String) {
        println(message)
    }

    actual fun logD(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun log(tag: Any, prefix: Any?, text: Any?, postfix: Any?, error: Throwable?) {
        val tagValue = if (tag is String) {
            tag
        } else {
            "${tag::class.java.simpleName}(${tag.hashCode()})"
        }
        println(
            "SampleLogger = $tagValue = ${
                listOfNotNull(
                    prefix,
                    text,
                    postfix,
                    error?.toString()
                ).joinToString(" = ")
            }"
        )
        error?.printStackTrace()
    }
}