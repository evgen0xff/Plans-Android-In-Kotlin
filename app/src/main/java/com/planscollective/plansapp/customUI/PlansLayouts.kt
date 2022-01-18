package com.planscollective.plansapp.customUI

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.RelativeLayout

class PlansInsetsLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context,attrs,defStyle) {}
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return super.onApplyWindowInsets(
            insets.replaceSystemWindowInsets(
                0, 0, 0,
                insets.systemWindowInsetBottom
            )
        )
    }
}

class PlansInsetsRelativeLayout : RelativeLayout {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context,attrs,defStyle) {}
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return super.onApplyWindowInsets(
            insets.replaceSystemWindowInsets(
                0, 0, 0,
                insets.systemWindowInsetBottom
            )
        )
    }
}