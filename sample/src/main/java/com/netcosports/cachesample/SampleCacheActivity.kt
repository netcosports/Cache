package com.netcosports.cachesample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.netcosports.cachesample.core_ui.BaseDataState
import com.netcosports.cachesample.core_ui.ErrorView
import com.netcosports.cachesample.core_ui.SimpleContentView
import com.netcosports.cachesample.databinding.SampleCacheActivityBinding
import com.netcosports.cachesample.vm.SampleCacheViewModel

class SampleCacheActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = SampleCacheActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(SampleCacheViewModel::class.java)

        val baseDataState = viewModel.baseDataState

        viewBinding.apply {
            containerView.setRefreshListener { viewModel.loadData() }

            baseDataState.sceneData.observe(this@SampleCacheActivity, Observer { scene ->
                when (scene ?: BaseDataState.Scene.LOADING) {
                    BaseDataState.Scene.LOADING -> containerView.showLoader()
                    BaseDataState.Scene.ERROR -> containerView.showError()
                    BaseDataState.Scene.CONTENT -> containerView.showContent()
                }
            })
            baseDataState.swipeRefreshData.observe(this@SampleCacheActivity, Observer {
                containerView.toggleRefresh(it)
            })
            baseDataState.errorData.observe(this@SampleCacheActivity, Observer {
                (containerView.errorView as ErrorView).setErrorMessage(it)
            })
            baseDataState.contentData.observe(this@SampleCacheActivity, Observer {
                (containerView.contentView as SimpleContentView).setData(it.joinToString(", "))
            })
        }
    }
}