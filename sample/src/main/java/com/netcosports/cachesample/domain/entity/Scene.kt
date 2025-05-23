package com.netcosports.cachesample.domain.entity

sealed class Scene<out DATA, out ERROR, out LOADER> {

    abstract val name: String
    abstract val isRefreshing: Boolean

    abstract fun setRefreshing(isRefreshing: Boolean): Scene<DATA, ERROR, LOADER>

    data class Loading<LOADER>(
        override val name: String,
        override val isRefreshing: Boolean = false,
        val loader: LOADER,
    ) : Scene<Nothing, Nothing, LOADER>() {

        override fun setRefreshing(isRefreshing: Boolean): Scene<Nothing, Nothing, LOADER> {
            return copy(isRefreshing = isRefreshing)
        }
    }

    data class Error<ERROR>(
        override val name: String,
        val error: ERROR,
        override val isRefreshing: Boolean,
    ) : Scene<Nothing, ERROR, Nothing>() {

        override fun setRefreshing(isRefreshing: Boolean): Scene<Nothing, ERROR, Nothing> {
            return copy(isRefreshing = isRefreshing)
        }
    }

    data class Data<DATA>(
        override val name: String,
        val data: DATA,
        override val isRefreshing: Boolean,
    ) : Scene<DATA, Nothing, Nothing>() {

        override fun setRefreshing(isRefreshing: Boolean): Scene<DATA, Nothing, Nothing> {
            return copy(isRefreshing = isRefreshing)
        }
    }
}