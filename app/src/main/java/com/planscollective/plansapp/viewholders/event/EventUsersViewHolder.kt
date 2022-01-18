package com.planscollective.plansapp.viewholders.event

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.EventUserListAdapter
import com.planscollective.plansapp.databinding.CellEventUsersBinding
import com.planscollective.plansapp.extension.setLayoutMargin
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import kotlin.math.roundToInt

class EventUsersViewHolder(
    private val itemBinding: CellEventUsersBinding,
    var listener: PlansActionListener? = null,
    var type: HolderType = HolderType.EVENT_FEED
) : EventBaseViewHolder(itemBinding.root){

    enum class HolderType {
        EVENT_FEED,
        EVENT_DETAILS,
    }

    private var adapterUsers = EventUserListAdapter()

    init {
        val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        itemBinding.apply {
            adapterUsers.typeAdapter = if (type == HolderType.EVENT_FEED) EventUserListAdapter.AdapterType.EVENT_FEED else EventUserListAdapter.AdapterType.EVENT_DETAILS
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapterUsers
            imvGuideGuestList.setOnSingleClickListener {
                UserInfo.isSeenGuideGuestList = true
                listener?.onClickedMoreUsers(eventModel)
            }
        }
        adapterUsers.listener = listener
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        adapterUsers.updateAdapter(item)
        itemBinding.apply {
            when(type) {
                HolderType.EVENT_FEED -> {
                    layoutHeader.visibility = View.GONE
                    viewBottomSeparator.visibility = View.GONE
                    imvGuideGuestList.visibility = View.GONE
                }
                HolderType.EVENT_DETAILS -> {
                    layoutHeader.visibility = View.VISIBLE
                    tvHeader.text = "People"
                    viewBottomSeparator.visibility = if (isLast) View.GONE else View.VISIBLE
                    imvGuideGuestList.visibility = if (UserInfo.isSeenGuideGuestList) View.GONE else {
                        adapterUsers.itemCount.takeIf { it > 0 }?.let {
                            var marginStart = (it * EventUserListAdapter.widthItemCell - (EventUserListAdapter.widthItemCell / 2f)).toInt()
                            val countHalf = (EventUserListAdapter.countItems / 2f).roundToInt()
                            var resorceId = 0
                            marginStart -= if (it > countHalf) {
                                resorceId = R.drawable.im_tap_to_view_guest_list_right
                                125.toPx()
                            }else {
                                resorceId = R.drawable.im_tap_to_view_guest_list_left
                                24.toPx()
                            }
                            imvGuideGuestList.setImageResource(resorceId)
                            imvGuideGuestList.setLayoutMargin(left = marginStart)
                            View.VISIBLE
                        } ?: View.GONE
                    }
                }
            }
        }
    }

}