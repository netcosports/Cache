package com.netcosports.cache.core

sealed class LoaderArguments {
    class CACHE internal constructor(private val name: String = "CACHE") : LoaderArguments()
    class API internal constructor(private val name: String = "API") : LoaderArguments()

    companion object {
        /*You don't need to create the LoaderArguments yourself. You can only get it via
        *
        * loader { loaderArguments: LoaderArguments ->
            when (loaderArguments) {
                is LoaderArguments.CACHE -> TODO(cache)
                is LoaderArguments.API -> TODO(api)
            }
        * }
        *
        * or suspendLoader (cache-core-ktx)
        * or singleLoader (cache-core-rx)
        * or from your own
        */
        internal fun getLoaderArgument(isCache: Boolean): LoaderArguments {
            return if (isCache) {
                CACHE()
            } else {
                API()
            }
        }
    }
}