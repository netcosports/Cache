package com.netcosports.cachesample

import android.app.Application
import okhttp3.OkHttpClient

class FlipperSetup {

    companion object {

        fun setup(app: Application) = Unit

        fun setupOkHttpClientBuilder(okHttpClientBuilder: OkHttpClient.Builder) = Unit
    }
}