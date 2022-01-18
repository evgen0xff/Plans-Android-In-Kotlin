package com.planscollective.plansapp.extension

import android.app.Activity
import android.graphics.Rect
import android.view.View
import com.planscollective.plansapp.helper.OSHelper

fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    val screenHeight = OSHelper.heightScreen
    val keypadHeight = screenHeight - visibleBounds.bottom
    return keypadHeight > screenHeight * 0.15
}

fun Activity.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}

fun Activity.getRootView(): View {
    return findViewById<View>(android.R.id.content)
}