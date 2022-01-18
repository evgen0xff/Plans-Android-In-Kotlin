package com.planscollective.plansapp.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.models.dataModels.EventModel

open class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(item: T? = null, data: Any? = null, isLast: Boolean = false){}
}

open class EventBaseViewHolder(itemView: View) : BaseViewHolder<EventModel>(itemView) {
    var  eventModel: EventModel? = null
}
