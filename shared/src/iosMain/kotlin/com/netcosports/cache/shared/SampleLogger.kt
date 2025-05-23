package com.netcosports.cache.shared

import platform.Foundation.NSLog

actual class SampleLogger {

    actual fun logD(message: String) {
        NSLog(message)
    }

    actual fun logD(tag: String, message: String) {
        NSLog("$tag $message")
    }
}