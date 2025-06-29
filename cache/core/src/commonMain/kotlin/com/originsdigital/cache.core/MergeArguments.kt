package com.originsdigital.cache.core

enum class MergeArguments {
    ONLY_API,
    ONLY_CACHE,
    CACHE_AND_API, // cache if exists, then api
    CACHE_OR_API, // cache if exists, api otherwise
    API_OR_CACHE // api if successful, cache otherwise if exists
}