package com.netcosports.cachesample

import android.app.Application


class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FlipperSetup.setup(this)
    }
}