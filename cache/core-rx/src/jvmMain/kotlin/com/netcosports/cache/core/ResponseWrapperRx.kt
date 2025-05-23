package com.netcosports.cache.core

import io.reactivex.Observable

fun <FROM, TO> Observable<ResponseWrapper<FROM>>.mapWrapperData(
    delegate: (FROM) -> TO
): Observable<ResponseWrapper<TO>> {
    return map { it.mapDataTo(delegate) }
}

fun <FROM, TO> Observable<ResponseWrapper<FROM>>.mapWrapper(
    delegate: (ResponseWrapper<FROM>) -> TO
): Observable<ResponseWrapper<TO>> {
    return map { it.mapWrapperTo(delegate) }
}