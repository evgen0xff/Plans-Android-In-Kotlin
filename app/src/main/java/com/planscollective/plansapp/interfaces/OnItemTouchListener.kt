package com.planscollective.plansapp.interfaces

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnItemTouchListener {
    fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int){}
    fun onItemClick(position: Int, data: Any?){}
    fun onItemSwipeDown(position: Int, data: Any?){}
}