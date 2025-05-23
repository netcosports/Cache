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
import com.netcosports.components.views2.base.content.IContentView

class SimpleContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IContentView {

    override var refreshListener: () -> Unit = {}

    override var isContentRefreshing: Boolean
        get() = refreshLayout.isRefreshing
        set(value) {
            refreshLayout.isRefreshing = value
        }

    private val contentTextView: TextView
    private val refreshLayout: SwipeRefreshLayout = SwipeRefreshLayout(context)

    init {
        contentTextView = AppCompatTextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 96f)
        }
        refreshLayout.addView(contentTextView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        refreshLayout.setOnRefreshListener {
            refreshListener()
        }
        addView(refreshLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun setData(text: String?) {
        contentTextView.text = text
    }
}