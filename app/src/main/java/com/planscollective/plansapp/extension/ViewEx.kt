package com.planscollective.plansapp.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.classes.SingleClickListener

private fun getOrCreateBadge(bottomBar: View, tabResId: Int): TextView? {
    val parentView = bottomBar.findViewById<ViewGroup>(tabResId)
    return parentView?.let {
        var badge = parentView.findViewById<TextView>(R.id.menuItemBadge)
        if (badge == null) {
            LayoutInflater.from(parentView.context).inflate(R.layout.bottom_nav_badge, parentView, true)
            badge = parentView.findViewById(R.id.menuItemBadge)
        }
        badge
    }
}

fun View.setLayoutWidth(newWidth: Int) {
    val lp = layoutParams
    lp.width = newWidth
    layoutParams = lp
    requestLayout()
}

fun View.setLayoutHeight(newHeight: Int) {
    val lp = layoutParams
    lp.height = newHeight
    layoutParams = lp
    requestLayout()
}

fun View.setLayoutSize(newWidth: Int, newHeight: Int) {
    val lp = layoutParams
    lp.height = newHeight
    lp.width = newWidth
    layoutParams = lp
    requestLayout()
}

fun View.setLayoutMargin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val lp = (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        left?.let {
            marginStart = it
            leftMargin = it
        }
        top?.let {
            topMargin = it
        }
        right?.let {
            marginEnd = it
            rightMargin = it
        }
        bottom?.let {
            bottomMargin = it
        }
    }
    layoutParams = lp
    requestLayout()
}

fun BottomNavigationView.setBadge(tabResId: Int, badgeValue: Int) {
    getOrCreateBadge(this, tabResId)?.let { badge ->
        badge.visibility = if (badgeValue > 0) {
            badge.text = "$badgeValue"
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

fun View.setOnSingleClickListener(defaultInterval: Int = 1000, onClick: (View?) -> Unit) {
    val singleClickListener = object : SingleClickListener(defaultInterval) {
        override fun onSingleClick(v: View?) {
            onClick(v)
        }
    }
    setOnClickListener(singleClickListener)
}

fun View.setOnSingleClickListener(listener: OnSingleClickListener?, defaultInterval: Int = 1000) {
    val singleClickListener = object : SingleClickListener(defaultInterval) {
        override fun onSingleClick(v: View?) {
            listener?.onSingleClick(v)
        }
    }
    setOnClickListener(singleClickListener)
}

