package com.planscollective.plansapp.viewholders.event

import com.planscollective.plansapp.databinding.CellEventHiddenEventsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class EventHiddenViewHolder(private val itemBinding: CellEventHiddenEventsBinding, listener: PlansActionListener? = null) : EventBaseViewHolder(itemBinding.root){
    init {
        itemBinding.apply {
            layoutHiddenEvents.setOnSingleClickListener {
                listener?.onClickedHiddenEvents()
            }
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        data?.let {
            if (it is ArrayList<*>) {
                itemBinding.tvHiddenEvents.text = "HIDDEN EVENTS (${it.size})"
            }
        }
    }

}