package com.planscollective.plansapp.viewholders.event

import android.view.View
import com.planscollective.plansapp.databinding.CellSearchEventBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setLayoutMargin
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class SearchEventVH(
    private val itemBinding: CellSearchEventBinding,
    var listener: PlansActionListener? = null,
    var cellType: CellType = CellType.SEARCH_EVENT,
) : EventBaseViewHolder(itemBinding.root){

    enum class CellType {
        SEARCH_EVENT,
        SAVED_EVENT
    }

    init {
        itemView.setOnSingleClickListener {
            listener?.onClickedEvent(eventModel)
        }
        itemBinding.btnMenu.setOnSingleClickListener {
            listener?.onClickedMoreMenuSavedEvent(eventModel)
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        if (item != null) {
            itemBinding.apply {
                // Event Cover Image
                imageView.setEventImage(if (item.mediaType == "video") item.thumbnail else item.imageOrVideo )

                // More Menu
                btnMenu.visibility = if (cellType == CellType.SEARCH_EVENT) {
                    tvEventName.setLayoutMargin(right = 0)
                    View.GONE
                } else {
                    tvEventName.setLayoutMargin(right = 28.toPx())
                    View.VISIBLE
                }

                // Event Name
                tvEventName.text = item.eventName


                // Organized by
                tvOrganizedBy.text = "Organized by ${eventModel?.eventCreatedBy?.firstName ?: ""} ${eventModel?.eventCreatedBy?.lastName ?: ""}"

                // Start/End Date time
                val maxWidth = OSHelper.widthScreen - (16 + 80 + 8 + 16 + 4 + 4).toPx()
                tvEventDate.text = item.getStartEndTime(maxWidth, tvEventDate)

                // Bottom separator
                viewBottomBar.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
            }
        }
    }

}
