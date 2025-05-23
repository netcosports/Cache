package com.netcosports.cache.core

data class ResponseWrapper<out DATA>(val isCache: Boolean, val data: DATA) {

    fun <RESULT> mapDataTo(mapper: (DATA) -> RESULT): ResponseWrapper<RESULT> {
        return ResponseWrapper(isCache, mapper(data))
    }

    fun <RESULT> mapWrapperTo(mapper: (ResponseWrapper<DATA>) -> RESULT): ResponseWrapper<RESULT> {
        return ResponseWrapper(isCache, mapper(this))
    }
}