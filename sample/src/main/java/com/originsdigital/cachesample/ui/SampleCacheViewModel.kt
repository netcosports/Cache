package com.originsdigital.cachesample.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.originsdigital.cache.core.MergeArguments
import com.originsdigital.cache.core.ResponseWrapper
import com.originsdigital.cache.core.api
import com.originsdigital.cache.core.cache
import com.originsdigital.cache.core.map
import com.originsdigital.cache.core.singleLoader
import com.originsdigital.cache.core.suspendLoader
import com.originsdigital.cache.core.toFlow
import com.originsdigital.cache.core.toObservable
import com.originsdigital.cachesample.di.SampleCacheDependencies
import com.originsdigital.cachesample.domain.entity.Scene
import com.originsdigital.cachesample.domain.utils.isEmpty
import com.originsdigital.cachesample.domain.utils.toData
import com.originsdigital.cachesample.domain.utils.toError
import com.originsdigital.cachesample.domain.utils.toLoading
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SampleCacheViewModel(app: Application) : AndroidViewModel(app) {

    private val dependencies = SampleCacheDependencies(app)
    private val repository = dependencies.repository
    private val logger = dependencies.logger

    val sceneFlow = MutableStateFlow<Scene<String, Throwable, Unit>>(
        Scene.Loading(
            name = "items",
            isRefreshing = false,
            loader = Unit
        )
    )
    private val mergeArguments
        get() = if (sceneFlow.value.isEmpty) {
            MergeArguments.CACHE_AND_API
        } else {
            MergeArguments.ONLY_API
        }

    private var job: Job? = null
    private var disposable: Disposable? = null
    private val testFlatMapToApiOnly = false

    fun forceReload() {
        sceneFlow.value = sceneFlow.value.toLoading(Unit, true)
        loadData()
    }

    fun loadData() {
        loadKtorSuspendToLoader()
//        loadRetrofitSuspendToLoader()
//        loadRetrofitSingleToLoader()
//        loadRetrofitObservable()
    }

    private fun loadKtorSuspendToLoader() {
        log("loadKtorSuspendToLoader")
        job?.cancel()
        handleLoading()
        job = viewModelScope.launch {
            log("loadKtorSuspendToLoader viewModelScope.launch")
            suspendLoader { loaderArguments ->
                log("loadKtorSuspendToLoader suspendLoader $loaderArguments")
                // example if you need to zip 2 loaders
                val (requestA, requestB) = coroutineScope {
                    val requestAAsync = async {
                        repository.getKtorLoader(url = REPOSITORIES) // request A
                            .load(loaderArguments)
                    }
                    val requestBAsync = async {
                        repository.getKtorLoader(url = USERS) // request B
                            .load(loaderArguments)
                    }
                    val requestA = requestAAsync.await()
                    val requestB = requestBAsync.await()
                    requestA to requestB
                }
                log("loadKtorSuspendToLoader suspendLoader $loaderArguments requests A B requestA=$requestA requestB=$requestB")

                // example if you need to call a loader with data from previous loaders
                val requestC = repository.getKtorSuspendToLoader( // request C
                    url = EVENTS,
                    requestA = requestA,
                    requestB = requestB
                ).load(loaderArguments)
                log("loadKtorSuspendToLoader suspendLoader $loaderArguments requestC=$requestC")

                Triple(requestA, requestB, requestC)
            }
                .toFlow(mergeArguments)
                .combine(
                    flow = if (testFlatMapToApiOnly) {
                        // example if you need to combine loaders chain with an API request
                        flow {
                            val requestD = repository.getKtorLoader(url = GISTS) // request D
                                .api()
                            log("loadKtorSuspendToLoader suspendLoader zip requestD=$requestD")
                            emit(requestD)
                        }
                    } else {
                        flowOf(emptyList())
                    },
                    transform = { responseWrapper, requestD ->
                        responseWrapper.map { data ->
                            Result(
                                requestA = data.first,
                                requestB = data.second,
                                requestC = data.third,
                                requestD = requestD,
                            )
                        }
                    }
                )
                .map { responseWrapper -> mapRepositoriesToData(responseWrapper = responseWrapper) }
                .catch { error ->
                    log("loadKtorSuspendToLoader catch", error)
                    handleError(error)
                }
                .collect { data ->
                    log("loadKtorSuspendToLoader collect $data")
                    handleData(data)
                }
        }
    }

    private fun loadRetrofitSuspendToLoader() {
        log("loadRetrofitSuspendToLoader")
        job?.cancel()
        handleLoading()
        job = viewModelScope.launch {
            log("loadRetrofitSuspendToLoader viewModelScope.launch")
            suspendLoader { loaderArguments ->
                log("loadRetrofitSuspendToLoader suspendLoader $loaderArguments")
                // example if you need to zip 2 loaders
                val (requestA, requestB) = coroutineScope {
                    val requestAAsync = async {
                        repository.getRetrofitSuspendToLoader(url = REPOSITORIES) // request A
                            .load(loaderArguments)
                    }
                    val requestBAsync = async {
                        repository.getRetrofitSuspendToLoader(url = USERS) // request B
                            .load(loaderArguments)
                    }
                    val requestA = requestAAsync.await()
                    val requestB = requestBAsync.await()
                    requestA to requestB
                }
                log("loadRetrofitSuspendToLoader suspendLoader $loaderArguments requests A B requestA=$requestA requestB=$requestB")

                // example if you need to call a loader with data from previous loaders
                val requestC =
                    repository.getRetrofitSuspendToLoader( // request C
                        url = EVENTS,
                        requestA = requestA,
                        requestB = requestB
                    )
                        .load(loaderArguments)
                log("loadRetrofitSuspendToLoader suspendLoader $loaderArguments requestC=$requestC")

                Triple(requestA, requestB, requestC)
            }
                .toFlow(mergeArguments)
                .combine(
                    flow = if (testFlatMapToApiOnly) {
                        // example if you need to combine loaders chain with an API request
                        flow {
                            val requestD = repository.getKtorLoader(url = GISTS) // request D
//                            .api()
                                .cache()
                            log("loadRetrofitSuspendToLoader suspendLoader zip requestD=$requestD")
                            emit(requestD)
                        }
                    } else {
                        flowOf(emptyList())
                    },
                    transform = { responseWrapper, requestD ->
                        responseWrapper.map { data ->
                            Result(
                                requestA = data.first,
                                requestB = data.second,
                                requestC = data.third,
                                requestD = requestD,
                            )
                        }
                    }
                )
                .map { responseWrapper -> mapRepositoriesToData(responseWrapper = responseWrapper) }
                .catch { error ->
                    log("loadRetrofitSuspendToLoader catch", error)
                    handleError(error)
                }
                .collect { data ->
                    log("loadRetrofitSuspendToLoader collect $data")
                    handleData(data)
                }
        }
    }

    private fun loadRetrofitSingleToLoader() {
        log("loadRetrofitSingleToLoader")
        disposable?.dispose()
        handleLoading()
        disposable = Observable.combineLatest(
            singleLoader { loaderArguments ->
                log("loadRetrofitSingleToLoader singleLoader $loaderArguments")
                // example if you need to zip 2 loaders
                Single.zip(
                    repository.getRetrofitSingleToLoader(url = REPOSITORIES) // request A
                        .load(loaderArguments),
                    repository.getRetrofitSingleToLoader(url = USERS) // request B
                        .load(loaderArguments)
                ) { requestA, requestB ->
                    log("loadRetrofitSingleToLoader singleLoader $loaderArguments requests A B requestA=$requestA requestB=$requestB")
                    requestA to requestB
                }
                    .flatMap { (requestA, requestB) ->
                        // example if you need to call a loader with data from previous loaders
                        repository.getRetrofitSingleToLoader( // request C
                            url = EVENTS,
                            requestA = requestA,
                            requestB = requestB
                        )
                            .load(loaderArguments)
                            .map { requestC ->
                                log("loadRetrofitSingleToLoader singleLoader $loaderArguments requestC=$requestC")
                                Triple(requestA, requestB, requestC)
                            }
                    }
            }
                .toObservable(mergeArguments),
            if (testFlatMapToApiOnly) {
                // example if you need to combine loaders chain with an API request
                repository.getRetrofitSingleToLoader(url = GISTS) // request D
                    .api()
                    .doOnSuccess { log("loadRetrofitSingleToLoader singleLoader zip requestD=$it") }
                    .toObservable()
            } else {
                Observable.just(emptyList<Any>())
            }
        ) { responseWrapper, requestD ->
            responseWrapper.map { data ->
                Result(
                    requestA = data.first,
                    requestB = data.second,
                    requestC = data.third,
                    requestD = requestD,
                )
            }
        }
            .map { responseWrapper -> mapRepositoriesToData(responseWrapper = responseWrapper) }
            .subscribe(
                { data ->
                    log("loadRetrofitSingleToLoader subscribe data $data")
                    handleData(data)
                },
                { error ->
                    log("loadRetrofitSingleToLoader subscribe error", error)
                    handleError(error)
                }
            )
    }

    private fun loadRetrofitObservable() {
        disposable?.dispose()
        handleLoading()
        disposable = Observable.zip(
            repository.getRetrofitObservable(url = REPOSITORIES), // request A
            repository.getRetrofitObservable(url = USERS), // request B
            repository.getRetrofitSingleToLoader(url = EVENTS) // request C, use only api
                .api().toObservable(),
            repository.getRetrofitSingleToLoader(url = GISTS) // request D, use only api
                .api().toObservable(),
        ) { requestA, requestB, requestC, requestD ->
            log("loadRetrofitObservable zip requests A B C D requestA=$requestA requestB=$requestB requestC=$requestC requestD=$requestD")
            ResponseWrapper(
                isCache = false,
                data = Result(
                    requestA = requestA,
                    requestB = requestB,
                    requestC = requestC,
                    requestD = requestD,
                )
            )
        }
            .map { responseWrapper -> mapRepositoriesToData(responseWrapper = responseWrapper) }
            .subscribe(
                { data ->
                    log("loadRetrofitObservable subscribe data $data")
                    handleData(data)
                },
                { error ->
                    log("loadRetrofitObservable subscribe error", error)
                    handleError(error)
                }
            )
    }

    private fun mapRepositoriesToData(responseWrapper: ResponseWrapper<Result>): ResponseWrapper<String> {
        return responseWrapper.map { data ->
            val result = "\nzipA=${data.requestA.size}" +
                    "\nzipB=${data.requestB.size}" +
                    "\nflatMapC=${data.requestC.size}" +
                    "\ncombineApiD=${data.requestD.size}"
            if (responseWrapper.isCache) {
                "CACHE repositories = $result"
            } else {
                "API repositories = $result"
            }
        }
    }

    private fun handleLoading() {
        sceneFlow.value = sceneFlow.value.toLoading(loader = Unit)
    }

    private fun handleError(error: Throwable) {
        sceneFlow.value = sceneFlow.value.toError(error = error)
    }

    private fun handleData(responseWrapper: ResponseWrapper<String>) {
        sceneFlow.value = sceneFlow.value.toData(
            data = responseWrapper.data,
            // set isRefreshing = true if its CACHE, because in our case we are still waiting for
            // an API response or API error, where we will set isRefreshing = false
            isRefreshing = responseWrapper.isCache,
        )
    }

    private fun log(text: String, error: Throwable? = null) {
        logger.log(
            tag = "SCVM",
            text = text.replace("com.originsdigital.cache.core.LoaderArguments", "LoaderArguments"),
            error = error
        )
    }

    override fun onCleared() {
        job?.cancel()
        disposable?.dispose()
        super.onCleared()
    }

    data class Result(
        val requestA: List<Any>,
        val requestB: List<Any>,
        val requestC: List<Any>,
        val requestD: List<Any>,
    )

    companion object {
        private const val REPOSITORIES = "https://api.github.com/repositories"
        private const val USERS = "https://api.github.com/users"
        private const val GISTS = "https://api.github.com/gists"
        private const val EVENTS = "https://api.github.com/events"
    }
}