package com.originsdigital.cache.core

sealed class LoaderArguments {
    data class CACHE internal constructor(private val name: String = "CACHE") : LoaderArguments()
    data class API internal constructor(private val name: String = "API") : LoaderArguments()

    companion object {
        /*You don't need to create the LoaderArguments yourself. You can only get it via
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