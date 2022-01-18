package com.planscollective.plansapp.customUI

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.viewbinding.ViewBinding
import com.planscollective.plansapp.databinding.ViewLoadingBinding
import com.planscollective.plansapp.databinding.ViewPullToFreshBinding
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

class LoadingView : LinearLayout, RefreshHeader, RefreshFooter {
    enum class LoadingType {
        TYPE_ALL,
        TYPE_PULL_TO_REFRESH
    }
    private var typeLoading: LoadingType = LoadingType.TYPE_ALL
    lateinit var binding: ViewBinding
    lateinit var progressBar: ProgressBar

    constructor(context: Context?, typeLoading: LoadingType = LoadingType.TYPE_ALL) : super(context) {
        this.typeLoading = typeLoading
        onInit(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, typeLoading: LoadingType = LoadingType.TYPE_ALL) : super(context, attrs) {
        this.typeLoading = typeLoading
        onInit(context)
    }

    private fun onInit(context: Context?) {
        binding = when(typeLoading) {
            LoadingType.TYPE_ALL -> {
                val result = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)
                progressBar = result.spinner
                result
            }
            LoadingType.TYPE_PULL_TO_REFRESH -> {
                val result = ViewPullToFreshBinding.inflate(LayoutInflater.from(context), this, true)
                progressBar = result.spinner
                progressBar.isIndeterminate = false
                progressBar.visibility = GONE
                result
            }
        }
    }

    //************************************** RefreshComponent ************************************//
    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            RefreshState.None -> {
                progressBar.isIndeterminate = false
                progressBar.visibility = GONE
            }
            RefreshState.PullDownToRefresh -> {
                progressBar.isIndeterminate = false
                progressBar.visibility = GONE
            }
            RefreshState.Refreshing, RefreshState.RefreshReleased -> {
                progressBar.isIndeterminate = true
                progressBar.visibility = VISIBLE
            }
            RefreshState.ReleaseToRefresh -> {
                progressBar.isIndeterminate = false
                progressBar.visibility = GONE
            }
            RefreshState.ReleaseToTwoLevel -> {
            }
            RefreshState.Loading -> {
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE
            }
        }

    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {
    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        return 500
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    //************************************** RefreshFooter ************************************//
    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        return true
    }
}