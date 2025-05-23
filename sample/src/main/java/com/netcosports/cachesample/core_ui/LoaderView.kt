package com.netcosports.cachesample.core_ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.netcosports.cachesample.databinding.CommonIncludeLoaderViewBinding
import com.netcosports.components.views2.base.loader.ILoaderView

class LoaderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ILoaderView {

    private val viewBinding: CommonIncludeLoaderViewBinding = CommonIncludeLoaderViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    //there is a bug in ContentLoadingProgressBar, call a 'show' or 'hide' before onAttachedToWindow
    //or after onDetachedFromWindow will break it
    override fun onShow() {
        viewBinding.loaderViewProgress.post {
            viewBinding.loaderViewProgress.show()
        }
    }

    override fun onHide() {
        viewBinding.loaderViewProgress.post {
            viewBinding.loaderViewProgress.hide()
        }
    }
}