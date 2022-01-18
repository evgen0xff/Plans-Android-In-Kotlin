package com.planscollective.plansapp.adapters

import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.interfaces.PlansItemMoveListener
import com.planscollective.plansapp.viewholders.BaseViewHolder

abstract class PlansRecyclerViewAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>(), PlansItemMoveListener {

}