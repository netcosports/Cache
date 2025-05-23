package com.netcosports.cachesample.domain.utils

import com.netcosports.cachesample.domain.entity.Scene

val <DATA> Scene<DATA, *, *>.dataOrNull: DATA?
    get() {
        return when (this) {
            is Scene.Loading, is Scene.Error -> null
            is Scene.Data -> this.data
        }
    }

val Scene<*, *, *>.isLoading: Boolean get() = this is Scene.Loading
val Scene<*, *, *>.isError: Boolean get() = this is Scene.Error
val Scene<*, *, *>.isData: Boolean get() = this is Scene.Data
val Scene<*, *, *>.isEmpty: Boolean get() = dataOrNull.isEmpty

private val Any?.isEmpty: Boolean
    get() {
        return when (this) {
            null -> true
            is Collection<*> -> isEmpty()
            is Map<*, *> -> isEmpty()
            else -> false
        }
    }


fun <DATA, ERROR, LOADER> Scene<DATA, ERROR, LOADER>.toLoading(
    loader: LOADER,
    force: Boolean = isEmpty
): Scene<DATA, ERROR, LOADER> {
    return when (this) {
        is Scene.Loading -> this
        is Scene.Error -> {
            if (force) {
                Scene.Loading(name = name, loader = loader)
            } else {
                this.copy(isRefreshing = true)
            }
        }

        is Scene.Data -> {
            if (force) {
                Scene.Loading(name = name, loader = loader)
            } else {
                this.copy(isRefreshing = true)
            }
        }
    }
}

fun <DATA, ERROR, LOADER> Scene<DATA, ERROR, LOADER>.toError(
    error: ERROR,
    force: Boolean = isEmpty,
): Scene<DATA, ERROR, LOADER> {
    return when (this) {
        is Scene.Loading, is Scene.Error -> {
            Scene.Error(name = name, error = error, isRefreshing = false)
        }

        is Scene.Data -> if (force) {
            Scene.Error(name = name, error = error, isRefreshing = false)
        } else {
            this.copy(isRefreshing = false)
        }
    }
}

fun <DATA, ERROR, LOADER> Scene<DATA, ERROR, LOADER>.toData(
    data: DATA,
    isRefreshing: Boolean = false
): Scene<DATA, ERROR, LOADER> {
    return when (this) {
        is Scene.Loading, is Scene.Error -> {
            Scene.Data(name = name, data = data, isRefreshing = isRefreshing)
        }

        is Scene.Data -> this.copy(data = data, isRefreshing = isRefreshing)
    }
}