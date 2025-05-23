package com.netcosports.cachesample.core_ui

import com.netcosports.cache.core.ResponseWrapper
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException

fun <T> Observable<out ResponseWrapper<T>>.subscribeStateWithCache(
    baseDataState: BaseDataState<T>,
    forceReload: Boolean = false
): Disposable {
    println("Cache_log_test subscribeStateWithCache rx start")
    return this
        .doOnSubscribe {
            println("Cache_log_test subscribeStateWithCache rx doOnSubscribe")
            baseDataState.showLoading(forceReload)
        }
        .subscribe({ (isCache, data) ->
            println("Cache_log_test subscribeStateWithCache rx subscribe isCache=$isCache data=$data")
            if (isCache) {
                if (data.isEmpty()) {
                    //do nothing, wait api
                } else {
                    baseDataState.handleContent(data)
                    baseDataState.showRefresh(true) //show loader for api
                }
            } else {
                baseDataState.handleContent(data)
            }
        }, {
            println("Cache_log_test subscribeStateWithCache rx error $it")
            it.printStackTrace()
            baseDataState.handleError(it)
        }, {
            println("Cache_log_test subscribeStateWithCache rx onComplete")
            baseDataState.showRefresh(false)
        })
}

suspend fun <T> Flow<ResponseWrapper<T>>.subscribeStateWithCache(
    baseDataState: BaseDataState<T>,
    forceReload: Boolean = false
) {
    this
        .onStart {
            println("Cache_log_test subscribeStateWithCache ktx start")
            baseDataState.showLoading(forceReload)
        }
        .catch {
            println("Cache_log_test subscribeStateWithCache ktx catch $it")
            it.printStackTrace()
            baseDataState.handleError(it)
        }
        .onEach { (isCache, data) ->
            println("Cache_log_test subscribeStateWithCache ktx onEach isCache=$isCache data=$data")
            if (isCache) {
                if (data.isEmpty()) {
                    //do nothing, wait api
                } else {
                    baseDataState.handleContent(data)
                    baseDataState.showRefresh(true) //show loader for api
                }
            } else {
                baseDataState.handleContent(data)
            }
        }
        .onCompletion {
            println("Cache_log_test subscribeStateWithCache ktx onCompletion")
            baseDataState.showRefresh(false)
        }
        .collect()
}


fun <T> BaseDataState<T>.showLoading(forceReload: Boolean) {
    if (forceReload || !hasData()) {
        this.showLoadingScene()
    } else {
        this.showRefresh(true)
    }
}

fun <T> BaseDataState<T>.handleError(error: Throwable?) {
    if (!this.hasData()) {
        this.showErrorScene(
            if (error.isInternetConnectionException) {
                "NO CONNECTION"
            } else {
                "API ERROR"
            }
        )
    } else {
        this.showRefresh(false)
    }
}

fun <T> BaseDataState<T>.handleContent(data: T) {
    if (data.isEmpty()) {
        showErrorScene("EMPTY")
    } else {
        showContentScene(data)
    }
}

fun <T> BaseDataState<T>.hasData(): Boolean {
    return contentData.value?.isEmpty() == false
}

private fun Any?.isEmpty(): Boolean {
    return when (this) {
        is Collection<*> -> this.isEmpty()
        else -> this == null
    }
}

val Throwable?.isInternetConnectionException: Boolean
    get() {
        return this is UnknownHostException
            || this is UnknownServiceException
            || this is SocketTimeoutException
    }