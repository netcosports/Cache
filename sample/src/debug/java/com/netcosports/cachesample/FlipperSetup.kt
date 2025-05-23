package com.netcosports.cachesample

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.OkHttpClient


class FlipperSetup {

    companion object {

//        private val networkFlipperPlugin = NetworkFlipperPlugin()
//        private val interceptor = FlipperOkhttpInterceptor(networkFlipperPlugin, true)

        fun setup(app: Application) {
            if (FlipperUtils.shouldEnableFlipper(app)) {
//                SoLoader.init(app, false)
//                val client = AndroidFlipperClient.getInstance(app)
//                client.addPlugin(InspectorFlipperPlugin(app, DescriptorMapping.withDefaults()))
//                client.addPlugin(networkFlipperPlugin)
//                client.start()
            }
        }

        fun setupOkHttpClientBuilder(okHttpClientBuilder: OkHttpClient.Builder) {
//            okHttpClientBuilder.addInterceptor(interceptor)
        }
    }
}