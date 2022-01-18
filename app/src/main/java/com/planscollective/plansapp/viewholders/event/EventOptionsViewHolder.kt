package com.planscollective.plansapp.viewholders.event

import android.view.View
import androidx.core.view.children
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.CellEventOptionsBinding
import com.planscollective.plansapp.extension.setLayoutMargin
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder

class EventOptionsViewHolder(
    private val itemBinding: CellEventOptionsBinding,
    var listener: PlansActionListener? = null
) : EventBaseViewHolder(itemBinding.root), OnSingleClickListener{

    init {
        itemBinding.let {
            it.layoutEdit.setOnSingleClickListener(this)
            it.layoutYourHere.setOnSingleClickListener (this)
            it.layoutGuestAction.setOnSingleClickListener(this)
            it.layoutInvite.setOnSingleClickListener(this)
            it.layoutDetails.setOnSingleClickListener(this)
            it.layoutChat.setOnSingleClickListener(this)
            it.imvGuideChatWithGuests.setOnSingleClickListener(this)
        }
    }

    override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
        eventModel = item
        if (item != null) {
            updateUnreadMsgCount(item.chatInfo?.countUnreadMessages)
            if (item.userId == UserInfo.userId) {
                setupUIForHost()
            }else {
                setupUIForGuest()
            }
        }
    }

    override fun onSingleClick(v: View?) {
        itemBinding.apply {
            when(v){
                layoutEdit -> {
                    listener?.onClickEditEvent(eventModel)
                }
                layoutYourHere -> {
                    if (eventModel?.userId != UserInfo.userId && eventModel?.isLive == 1) {
                        listener?.onClickGuestAction(tvYourHere.text.toString(), eventModel)
                    }
                }
                layoutGuestAction -> {
                    listener?.onClickGuestAction(tvGuestAction.text.toString(), eventModel)
                }
                layoutInvite -> {
                    listener?.onClickInviteUser(eventModel)
                }
                layoutDetails -> {
                    listener?.onClickDetailsOfEvent(eventModel)
                }
                layoutChat -> {
                    UserInfo.isSeenGuideChatWithEventGuests = true
                    listener?.onClickChatOfEvent(eventModel)
                }
                imvGuideChatWithGuests -> {
                    UserInfo.isSeenGuideChatWithEventGuests = true
                    listener?.onClickChatOfEvent(eventModel)
                }
            }
        }
    }

    private fun updateUnreadMsgCount(count: Int?) {
        val countMsg = count?.takeIf { it > 0 } ?: run {
            itemBinding.tvUnreadMsgCount.visibility = View.GONE
            return
        }
        itemBinding.tvUnreadMsgCount.apply {
            text = "$countMsg"
            visibility = View.VISIBLE
        }
    }

    private fun setupUIForHost() {
        val event = eventModel ?: return
        itemBinding.apply {
            layoutYourHere.visibility = View.GONE
            layoutEdit.visibility = View.VISIBLE
            layoutGuestAction.visibility = View.GONE
            layoutInvite.visibility = View.VISIBLE
            layoutDetails.visibility = View.VISIBLE
            layoutChat.visibility = View.GONE
            imgvDownArrowHere.visibility = View.GONE

            // Other Views
            if (event.isEnded == 1) {
                ///////////////////////////////////// Event Ended
                layoutYourHere.visibility = View.VISIBLE
                tvYourHere.text = "ENDED"
                tvYourHere.setTextColor(PlansColor.PURPLE_JOIN)
                imgvYourHere.setImageResource(R.drawable.ic_flag_purple)

                layoutEdit.visibility = View.GONE
                layoutInvite.visibility = View.GONE
            }else if (event.isActive == false || event.isCancel == true || event.isExpired == true) {
                ///////////////////////////////////// Event Delelted, Cancelled, Expired
                layoutInvite.visibility = View.GONE
                tvEdit.text = "UPDATE"
                tvEdit.setTextColor(PlansColor.BLACK)
                imgvEdit.setImageResource(R.drawable.ic_pencil_black)
            }else if (event.isLive == 1) {
                ///////////////////////////////////// Event Lived
                layoutYourHere.visibility = View.VISIBLE
                layoutEdit.visibility = View.GONE
                if (event.isHostLive == 1) {
                    tvYourHere.text = "YOU'RE HERE"
                    tvYourHere.setTextColor(PlansColor.TEAL_MAIN)
                    imgvYourHere.setImageResource(R.drawable.ic_check_circle_green)
                }else {
                    tvYourHere.text = "NOT HERE"
                    tvYourHere.setTextColor(PlansColor.RED_NOT_HERE)
                    imgvYourHere.setImageResource(R.drawable.ic_minus_circle_red)
                }
            }else {
                ///////////////////////////////////// Before live
                tvEdit.text = "EDIT"
                tvEdit.setTextColor(PlansColor.BLACK)
                imgvEdit.setImageResource(R.drawable.ic_pencil_black)
            }

            // Chat View
            if (event.isGroupChatOn == true) {
                layoutChat.visibility = View.VISIBLE
            }

        }
        updateGuideView()
    }

    private fun setupUIForGuest() {
        val event = eventModel ?: return
        itemBinding.apply {
            layoutYourHere.visibility = View.GONE
            layoutEdit.visibility = View.GONE
            layoutGuestAction.visibility = View.VISIBLE
            layoutInvite.visibility = View.GONE
            layoutDetails.visibility = View.VISIBLE
            layoutChat.visibility = View.GONE
            tvGuestAction.setTextColor(PlansColor.BLACK)
            imgvDownArrow.visibility = View.GONE
            imgvDownArrowHere.visibility = View.GONE

            if (event.isEnded == 1 || event.isActive == false || event.isCancel == true || event.isExpired == true) {
                ///////////////////////////////////// Event Ended, Delelted, Cancelled, Expired
                layoutYourHere.visibility = View.VISIBLE
                tvYourHere.setTextColor(PlansColor.BLACK)
                imgvYourHere.setImageResource(R.drawable.ic_flag_purple)

                if (event.isEnded == 1) {
                    tvYourHere.setTextColor(PlansColor.PURPLE_JOIN)
                    tvYourHere.text = "ENDED"
                    imgvYourHere.setColorFilter(PlansColor.PURPLE_JOIN)
                }else if (event.isActive == false || event.isCancel == true) {
                    tvYourHere.setTextColor(PlansColor.BROWN_CANCELLED)
                    tvYourHere.text = "CANCELED"
                    imgvYourHere.setColorFilter(PlansColor.BROWN_CANCELLED)
                }else if (event.isExpired == true) {
                    tvYourHere.setTextColor(PlansColor.ORANGE_EXPIRED)
                    tvYourHere.text = "EXPIRED"
                    imgvYourHere.setColorFilter(PlansColor.ORANGE_EXPIRED)
                }

                layoutGuestAction.visibility = View.GONE
            }else if (event.isLive == 1) {
                ///////////////////////////////////// Event Lived
                if (event.isJoin == true) {
                    // Attended by host's invitation or by yourself joined
                    if (event.statusJoin == 4) {
                        tvGuestAction.text = "NEXT TIME"
                        imgvGuestAction.setImageResource(R.drawable.ic_x_circle_black)
                        imgvDownArrow.visibility = View.VISIBLE
                        imgvDownArrow.setColorFilter(PlansColor.BLACK)
                    }else if (event.isLiveUser(UserInfo.userId)) {
                        tvYourHere.setTextColor(PlansColor.TEAL_MAIN)
                        tvYourHere.text = "YOU'RE HERE"
                        imgvYourHere.setImageResource(R.drawable.ic_check_circle_green)
                        imgvDownArrowHere.setColorFilter(PlansColor.TEAL_MAIN)
                        imgvDownArrowHere.visibility = View.VISIBLE
                        layoutGuestAction.visibility = View.GONE
                        layoutYourHere.visibility = View.VISIBLE
                    }else {
                        tvYourHere.setTextColor(PlansColor.RED_NOT_HERE)
                        tvYourHere.text = "NOT HERE"
                        imgvYourHere.setImageResource(R.drawable.ic_minus_circle_red)
                        imgvDownArrowHere.setColorFilter(PlansColor.RED_NOT_HERE)
                        imgvDownArrowHere.visibility = View.VISIBLE
                        layoutGuestAction.visibility = View.GONE
                        layoutYourHere.visibility = View.VISIBLE
                    }
                }else {
                    // Not Attended by host's invitation or by yourself joined
                    if (event.isInvite == 1) {
                        tvGuestAction.setTextColor(PlansColor.BLACK)
                        tvGuestAction.text = "PENDING INVITE"
                        imgvGuestAction.setImageResource(R.drawable.ic_clock_black)
                        imgvDownArrow.visibility = View.VISIBLE
                        imgvDownArrow.setColorFilter(PlansColor.BLACK)
                    }else {
                        tvGuestAction.text = "JOIN"
                        imgvGuestAction.setImageResource(R.drawable.ic_plus_black)
                    }
                }
            }else {
                ///////////////////////////////////// Before live
                if (event.isJoin == true) {
                    // Attended by host's invitation or by yourself joined
                    if (event.isInvite == 1) { // By host's invitation
                        if (event.statusJoin == 2) {
                            tvGuestAction.setTextColor(PlansColor.TEAL_MAIN)
                            tvGuestAction.text = "GOING"
                            imgvGuestAction.setImageResource(R.drawable.ic_check_circle_green)
                            imgvDownArrow.visibility = View.VISIBLE
                            imgvDownArrow.setColorFilter(PlansColor.TEAL_MAIN)
                        }else if (event.statusJoin == 3) {
                            tvGuestAction.text = "MAYBE"
                            imgvGuestAction.setImageResource(R.drawable.ic_clock_black)
                            imgvDownArrow.visibility = View.VISIBLE
                            imgvDownArrow.setColorFilter(PlansColor.BLACK)
                        }else if (event.statusJoin == 4) {
                            tvGuestAction.text = "NEXT TIME"
                            imgvGuestAction.setImageResource(R.drawable.ic_x_circle_black)
                            imgvDownArrow.visibility = View.VISIBLE
                            imgvDownArrow.setColorFilter(PlansColor.BLACK)
                        }
                    }else { // By yourself joined
                        tvGuestAction.setTextColor(PlansColor.TEAL_MAIN)
                        tvGuestAction.text = "JOINED"
                        imgvGuestAction.setImageResource(R.drawable.ic_user_check_green)
                    }
                }else {
                    // Not Attended by host's invitation or by yourself joined
                    if (event.isInvite == 1) { // By host's invitation
                        tvGuestAction.setTextColor(PlansColor.BLACK)
                        tvGuestAction.text = "PENDING INVITE"
                        imgvGuestAction.setImageResource(R.drawable.ic_clock_black)
                        imgvDownArrow.visibility = View.VISIBLE
                        imgvDownArrow.setColorFilter(PlansColor.BLACK)
                    }else { // By yourself joined
                        tvGuestAction.text = "JOIN"
                        imgvGuestAction.setImageResource(R.drawable.ic_plus_black)
                    }
                }
            }

            // Chat view
            if ((event.statusJoin == 2 || event.statusJoin == 3) && event.isGroupChatOn == true) {
                layoutChat.visibility = View.VISIBLE
            }
        }

        updateGuideView()
    }

    private fun updateGuideView() {
        itemBinding.apply {
            imvGuideChatWithGuests.visibility = if (!UserInfo.isSeenGuideChatWithEventGuests && layoutChat.visibility == View.VISIBLE) {
                val count = layoutContent.children.filter { it.visibility == View.VISIBLE }.count()
                val marginX = (OSHelper.widthScreen - ( OSHelper.widthScreen.toFloat() / (2f * count.toFloat()) ) - 130.toPx()).toInt()
                imvGuideChatWithGuests.setLayoutMargin(left = marginX)
                View.VISIBLE
            }else View.GONE
        }
    }

}