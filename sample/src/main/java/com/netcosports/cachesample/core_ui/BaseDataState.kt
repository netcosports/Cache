package com.netcosports.cachesample.core_ui

import androidx.lifecycle.MutableLiveData

open class BaseDataState<DATA> {

    val contentData: MutableLiveData<DATA> = MutableLiveData()
    val errorData: MutableLiveData<String> = MutableLiveData<String>()
    val swipeRefreshData: MutableLiveData<Boolean> = MutableLiveData()
    val sceneData: MutableLiveData<Scene> = MutableLiveData<Scene>().apply {
        this.value = Scene.LOADING
    }

    fun showContentScene(data: DATA?) {
        this.contentData.value = data
        swipeRefreshData.value = false
        sceneData.value = Scene.CONTENT
    }

    fun showErrorScene(error: String) {
        swipeRefreshData.value = false
        errorData.value = error
        sceneData.value = Scene.ERROR
    }

    fun showLoadingScene() {
        swipeRefreshData.value = false
        sceneData.value = Scene.LOADING
    }

    fun showRefresh(show: Boolean) {
        swipeRefreshData.value = show
    }

    enum class Scene {
        LOADING, CONTENT, ERROR
    }
}