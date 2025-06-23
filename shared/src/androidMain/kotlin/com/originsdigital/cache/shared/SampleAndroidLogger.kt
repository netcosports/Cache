package com.originsdigital.cache.shared

class SampleAndroidLogger : SampleLogger {

    override fun log(tag: Any, text: Any?, error: Throwable?) {
        val tagValue = if (tag is String) {
            tag
        } else {
            "${tag::class.java.simpleName}(${tag.hashCode()})"
        }
        println(
            "SampleAndroidLogger = $tagValue = ${
                listOfNotNull(
                    text,
                    error?.toString()
                ).joinToString(" = ")
            }"
        )
        error?.printStackTrace()
    }
}