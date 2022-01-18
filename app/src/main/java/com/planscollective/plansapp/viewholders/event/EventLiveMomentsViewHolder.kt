package com.planscollective.plansapp.viewholders.event

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.LiveMomentsAdapter
import com.planscollective.plansapp.databinding.CellEventLivemomentsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class EventLiveMomentsViewHolder(
    private val itemBinding: CellEventLivemomentsBinding,
    var listener: PlansActionListener? = null,
    var type: HolderType = HolderType.EVENT_FEED
) : EventBaseViewHolder(itemBinding.root){

    enum class HolderType {
        EVENT_FEED,
        EVENT_DETAILS,
    }

    private var adapterLiveMoments = LiveMomentsAdapter()

    init {
        val layoutManager = object : LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return true
            }
        }
        itemBinding.apply {
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapterLiveMoments
            btnShowAll.setOnSingleClickListener{
                listener?.onClickShowAllLiveMoments(eventModel)
            }
            imvGuidePost.setOnSingleClickListener {
                UserInfo.isSeenGuidePosts = true
                listener?.onClickAddLiveMoment(eventModel)
            }
        }
        adapterLiveMoments.listener = listener
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        adapterLiveMoments.updateAdapter(item)

        itemBinding.apply {
            // Guide for Post
            imvGuidePost.visibility = if (!UserInfo.isSeenGuidePosts && (eventModel?.isEnableAddLiveMoment() == true)) View.VISIBLE else View.GONE

            // Show All Button
            btnShowAll.visibility = if ((item?.liveMoments?.size ?: 0) > 3) View.VISIBLE else View.GONE

            // Bottom Separator View
            viewBottomSeparator.visibility = if (isLast) View.GONE else View.VISIBLE

            when(type) {
                HolderType.EVENT_FEED -> {
                }
                HolderType.EVENT_DETAILS -> {
                }
            }
        }
    }

}