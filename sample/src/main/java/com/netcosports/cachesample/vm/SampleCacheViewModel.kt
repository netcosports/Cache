package com.netcosports.cachesample.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netcosports.cache.core.*
import com.netcosports.cachesample.core_ui.BaseDataState
import com.netcosports.cachesample.core_ui.hasData
import com.netcosports.cachesample.core_ui.subscribeStateWithCache
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SampleCacheViewModel(app: Application) : AndroidViewModel(app) {

    val baseDataState: BaseDataState<List<String>> = BaseDataState()

    private val dependencies = SampleCacheDependencies(app)

    private var job: Job? = null
    private var disposable: Disposable? = null

    init {
        loadData()
    }

    enum class Run {
        KTOR, SUSPEND, RX
    }

    fun loadData() {
        val zip = false
//        val zip = true
        val run = Run.KTOR
//        val run = Run.SUSPEND
//        val run = Run.RX

        when (run) {
            Run.KTOR -> {
                job?.cancel()
                job = viewModelScope.launch {
                    if (zip) {
                        suspendLoader { loaderArguments ->
                            val firstAsync = viewModelScope.async {
                                dependencies.repository.getKtorResponseSuspended().load(loaderArguments)()
                            }
                            val secondAsync = viewModelScope.async {
                                dependencies.repository.getKtorResponseSuspended().load(loaderArguments)()
                            }
                            val first = firstAsync.await()
                            val second = secondAsync.await()
                            (first to second).first
                        }
                    } else {
                        dependencies.repository.getKtorResponseSuspended()
                    }
                        .toFlow(if (baseDataState.hasData()) MergeArguments.ONLY_API else MergeArguments.CACHE_AND_API)
                        .mapWrapperData { "DATA" }
                        .mapWrapper { mutableListOf(if (it.isCache) "CACHE" else "API") }
                        .subscribeStateWithCache(baseDataState)
                }
            }
            Run.SUSPEND -> {
                job?.cancel()
                job = viewModelScope.launch {
                    if (zip) {
                        suspendLoader { loaderArguments ->
                            val firstAsync = viewModelScope.async {
                                dependencies.repository.getResponseSuspended().load(loaderArguments)()
                            }
                            val secondAsync = viewModelScope.async {
                                dependencies.repository.getResponseSuspended().load(loaderArguments)()
                            }
                            val first = firstAsync.await()
                            val second = secondAsync.await()
                            (first to second).first
                        }
                    } else {
                        dependencies.repository.getResponseSuspended()
                    }
                        .toFlow(if (baseDataState.hasData()) MergeArguments.ONLY_API else MergeArguments.CACHE_AND_API)
                        .mapWrapperData { "DATA" }
                        .mapWrapper { mutableListOf(if (it.isCache) "CACHE" else "API") }
                        .subscribeStateWithCache(baseDataState)
                }
            }
            Run.RX -> {
                val loader = if (zip) {
                    singleLoader { loaderArguments ->
                        Single.zip(
                            dependencies.repository.getResponseSingle().load(loaderArguments),
                            dependencies.repository.getResponseSingle().load(loaderArguments),
                            { first, _ -> first }
                        )
                    }
                } else {
                    dependencies.repository.getResponseSingle()
                }
                disposable?.dispose()
                disposable = loader
                    .toObservable(if (baseDataState.hasData()) MergeArguments.ONLY_API else MergeArguments.CACHE_AND_API)
                    .mapWrapperData { "DATA" }
                    .mapWrapper { mutableListOf(if (it.isCache) "CACHE" else "API") }
                    //will not work without delayError = true
                    .observeOn(AndroidSchedulers.mainThread(), /*delayError =*/true)
                    .subscribeStateWithCache(baseDataState)
            }
        }
    }

    override fun onCleared() {
        job?.cancel()
        disposable?.dispose()
        super.onCleared()
    }
}