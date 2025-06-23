package com.originsdigital.cachesample.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.originsdigital.cachesample.databinding.SampleCacheActivityBinding
import com.originsdigital.cachesample.domain.entity.Scene

class SampleCacheActivity : AppCompatActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = SampleCacheActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.swipeRefreshLayout.setProgressViewOffset(false, 100, 150)

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[SampleCacheViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener { viewModel.loadData() }
        lifecycleScope.launchWhenStarted {
            viewModel.sceneFlow.collect { scene ->
                viewBinding.swipeRefreshLayout.isRefreshing = scene.isRefreshing
                viewBinding.text.text = when (scene) {
                    is Scene.Data -> "Data = ${scene.data}"
                    is Scene.Error -> "Error = ${scene.error}"
                    is Scene.Loading -> "Loading"
                }
            }
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                viewModel.forceReload()
            }
        })
    }
}