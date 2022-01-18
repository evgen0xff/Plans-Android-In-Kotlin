package com.planscollective.plansapp.viewholders.event

import android.view.View
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.databinding.CellEventHeaderBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.timeAgoSince
import com.planscollective.plansapp.extension.toLocalDateTime
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class EventHeaderViewHolder(
    private val itemBinding: CellEventHeaderBinding,
    var listener: PlansActionListener? = null,
    var type: HeaderType = HeaderType.EVENT_FEED
) : EventBaseViewHolder(itemBinding.root), OnSingleClickListener {

    enum class HeaderType {
        EVENT_FEED,
        EVENT_DETAILS
    }

    init {
        if (type == HeaderType.EVENT_FEED) {
            itemView.setOnSingleClickListener(this)
        }
        itemBinding.let {
            it.imvUserImage.setOnSingleClickListener(this)
            it.btnJoin.setOnSingleClickListener(this)
            it.btnUnjoin.setOnSingleClickListener(this)
            it.btnMenu.setOnSingleClickListener(this)
            it.tvEventCaption.setOnSingleClickListener(this)
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        if (item != null) {
            itemBinding.apply {
                // User Image
                imvLingLive.visibility = if (item.isHostLive == 1) View.VISIBLE else View.INVISIBLE
                imvUserImage.setUserImage(item.eventCreatedBy?.profileImage)

                // User Name
                val userName = item.eventCreatedBy?.firstName + " " + item.eventCreatedBy?.lastName
                tvUserName.text = userName

                // Private / Public
                if (item.isPublic == true) {
                    imvPublicPrivate.setImageResource(R.drawable.ic_public_green_3x)
                }else {
                    imvPublicPrivate.setImageResource(R.drawable.ic_private_green_3x)
                }

                // Created Time
                tvCreatedAt.text = eventModel?.createdAt?.toLocalDateTime()?.timeAgoSince()

                // Event Name
                tvEventName.text = item.eventName
                if (type == HeaderType.EVENT_DETAILS) {
                    tvEventName.maxLines = Int.MAX_VALUE
                }else {
                    tvEventName.maxLines = 2
                }

                when(type) {
                    HeaderType.EVENT_FEED -> {
                        // Join Button
                        var titleJoin: String? = null
                        if (item.userId == UserInfo.userId) {
                            titleJoin = null
                        }else if (item.isPublic == true) {
                            if (item.isInvite == 1){
                                titleJoin = null
                            }else if (item.didLeave == 1 && item.isLive == 1) {
                                titleJoin = "JOIN"
                            }else if (item.isJoin == true && item.isLive == 0) {
                                titleJoin = "UNJOIN"
                            }else if (item.isJoin == false){
                                titleJoin = "JOIN"
                            }
                        }

                        btnJoin.visibility = if (titleJoin == null || titleJoin == "UNJOIN") View.GONE else View.VISIBLE
                        btnUnjoin.visibility = if (titleJoin == null || titleJoin == "JOIN") View.GONE else View.VISIBLE

                        // Menu button
                        btnMenu.visibility = View.VISIBLE

                        // Event Caption
                        layoutEventCaption.visibility = if (item.caption.isNullOrEmpty()) View.GONE else {
                            tvEventCaption.text = item.caption
                            View.VISIBLE
                        }

                        // Bottom Separator
                        viewBottomSeparator.visibility = View.GONE
                    }
                    HeaderType.EVENT_DETAILS -> {
                        btnJoin.visibility = View.GONE
                        btnUnjoin.visibility = View.GONE
                        btnMenu.visibility = View.GONE
                        layoutEventCaption.visibility = View.GONE
                        viewBottomSeparator.visibility = if (isLast) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onSingleClick(v: View?) {
        itemBinding.apply {
            when(v){
                itemView -> {
                    listener?.onClickedEvent(eventModel)
                }
                imvUserImage -> {
                    listener?.onClickedUser(eventModel?.eventCreatedBy, eventModel)
                }
                btnJoin -> {
                    listener?.onClickedJoin(eventModel)
                }
                btnUnjoin -> {
                    listener?.onClickedUnjoin(eventModel)
                }
                btnMenu -> {
                    listener?.onClickedMoreMenu(eventModel)
                }
                tvEventCaption -> {
                    if (tvEventCaption.isShownReadMore) {
                        listener?.onClickDetailsOfEvent(eventModel)
                    }else {
                        listener?.onClickedEvent(eventModel)
                    }
                }
            }
        }
    }

}