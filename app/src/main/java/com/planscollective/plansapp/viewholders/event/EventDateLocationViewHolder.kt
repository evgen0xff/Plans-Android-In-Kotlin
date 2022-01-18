package com.planscollective.plansapp.viewholders.event

import android.view.View
import com.planscollective.plansapp.databinding.CellEventDateLocationBinding
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class EventDateLocationViewHolder(
    private val itemBinding: CellEventDateLocationBinding,
    var listener: PlansActionListener? = null,
    var type: HolderType = HolderType.EVENT_FEED
) : EventBaseViewHolder(itemBinding.root){

    enum class HolderType {
        EVENT_FEED,
        EVENT_DETAILS,
    }

    init {
        itemBinding.apply {
            layoutDate.setOnSingleClickListener {
                listener?.onClickedDateTime(eventModel)
            }

            layoutLocation.setOnSingleClickListener {
                listener?.onClickedLocation(eventModel)
            }
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        if (item != null) {
            itemBinding.apply {
                // Start/End Date time
                val maxWidth = OSHelper.widthScreen - (32 + 16 + 4).toPx()
                tvEventDate.text = item.getStartEndTime(maxWidth, tvEventDate)

                // Location of Event
                var name = ""
                var address = ""
                var location = "TBD"
                if (!item.address.isNullOrEmpty()) {
                    address = item.address!!
                    location = address
                }
                if (!item.locationName.isNullOrEmpty()){
                    name = item.locationName!!
                    location = name
                }

                if (!item.address.isNullOrEmpty() &&
                    !item.locationName.isNullOrEmpty() &&
                    address.substring(0, name.count()) == name) {
                    location = address
                }
                tvEventLocation.text = location.removeOwnCountry()

                when(type) {
                    HolderType.EVENT_FEED -> {
                        viewBottomSeparator.visibility = View.GONE
                        viewBottomSpace.visibility = View.VISIBLE
                    }
                    HolderType.EVENT_DETAILS -> {
                        viewBottomSeparator.visibility = if (isLast) View.GONE else View.VISIBLE
                        viewBottomSpace.visibility = View.GONE
                    }
                }

            }
        }
    }

}