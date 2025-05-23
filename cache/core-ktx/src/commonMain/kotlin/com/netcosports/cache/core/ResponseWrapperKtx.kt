package com.netcosports.cache.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

suspend fun <FROM, TO> ResponseWrapper<FROM>.mapDataTo(
    mapper: suspend (FROM) -> TO
): ResponseWrapper<TO> {
    return ResponseWrapper(isCache, mapper(data))
}

suspend fun <FROM, TO> ResponseWrapper<FROM>.mapWrapperTo(
    mapper: suspend (ResponseWrapper<FROM>) -> TO
): ResponseWrapper<TO> {
    return ResponseWrapper(isCache, mapper(this))
}

suspend fun <FROM, TO> Flow<ResponseWrapper<FROM>>.mapWrapperData(
    delegate: suspend (FROM) -> TO
): Flow<ResponseWrapper<TO>> {
    return map { it.mapDataTo(delegate) }
}

suspend fun <FROM, TO> Flow<ResponseWrapper<FROM>>.mapWrapper(
    delegate: suspend (ResponseWrapper<FROM>) -> TO
): Flow<ResponseWrapper<TO>> {
    return map { it.mapWrapperTo(delegate) }
}