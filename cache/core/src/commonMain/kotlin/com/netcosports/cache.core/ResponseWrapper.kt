package com.netcosports.cache.core

data class ResponseWrapper<out DATA>(val isCache: Boolean, val data: DATA)

inline fun <FROM, TO> ResponseWrapper<FROM>.map(
    mapper: (FROM) -> TO
): ResponseWrapper<TO> {
    return ResponseWrapper(
        isCache = isCache,
        data = mapper(data),
    )
}

inline fun <FROM, TO> ResponseWrapper<FROM>.mapWrapper(
    mapper: (ResponseWrapper<FROM>) -> TO
): ResponseWrapper<TO> {
    return ResponseWrapper(
        isCache = isCache,
        data = mapper(this),
    )
}