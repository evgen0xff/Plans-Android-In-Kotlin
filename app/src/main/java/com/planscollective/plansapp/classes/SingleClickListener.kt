package com.planscollective.plansapp.classes

import android.os.SystemClock
import android.view.View

interface OnSingleClickListener {
    fun onSingleClick(v: View?)
}

abstract class SingleClickListener(
    private var defaultInterval: Int = 1000
) : View.OnClickListener, OnSingleClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSingleClick(v)
    }
}