package com.netcosports.cachesample.core_ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.netcosports.components.views2.base.error.IErrorView

class ErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IErrorView {

    override var refreshListener: () -> Unit = {}

    override var isErrorRefreshing
        get() = refreshLayout.isRefreshing
        set(value) {
            refreshLayout.isRefreshing = value
        }

    private val errorTextView: TextView
    private val refreshLayout: SwipeRefreshLayout = SwipeRefreshLayout(context)

    init {
        errorTextView = AppCompatTextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
        refreshLayout.addView(errorTextView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        refreshLayout.setOnRefreshListener {
            refreshListener()
        }
        addView(refreshLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun setErrorMessage(errorMessage: String) {
        errorTextView.text = errorMessage
    }
}