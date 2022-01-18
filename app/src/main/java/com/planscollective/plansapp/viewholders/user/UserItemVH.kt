package com.planscollective.plansapp.viewholders.user

import android.view.View
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.CellUserItemBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.interfaces.PlansItemMoveListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

class UserItemVH(private var itemBinding: CellUserItemBinding,
                 private var listener: PlansActionListener? = null,
                 private var holderType: HolderType = HolderType.PEOPLE_INVITED,
                 private var listenerItemMove: PlansItemMoveListener? = null
) : BaseViewHolder<UserModel>(itemBinding.root) {

    enum class HolderType {
        PEOPLE_INVITED,
        PLANS_USER,
        CONTACTS,
        CHAT_SETTINGS
    }

    private var user: UserModel? = null
    private var event: EventModel? = null
    private var chat: ChatModel? = null

    init {
        itemBinding.apply {
            root.setOnSingleClickListener {
                listener?.onClickedUser(user)
            }
            btnChat.setOnSingleClickListener{
                listener?.onClickedChat(user)
            }
            btnAddFriend.setOnSingleClickListener{
                listenerItemMove?.onItemSelected(layoutPosition, user)
                listener?.onClickedFriendAction(user, tvFriend.text.toString())
            }
            btnMenu.setOnSingleClickListener {
                listener?.onClickedMoreMenuUser(user, event)
            }
        }
    }

    override fun bind(item: UserModel?, data: Any?, isLast: Boolean) {
        user = item
        when(data) {
            is EventModel -> event = data
            is ChatModel -> chat = data
            is HashMap<*, *> -> {
                (data["HOLDER_TYPE"] as? HolderType)?.also {
                    holderType = it
                }
            }
        }

        itemBinding.apply {
            if (holderType == HolderType.CONTACTS && user?.profileImage.isNullOrEmpty() && user?.id.isNullOrEmpty()) {
                imvUserImage.setImageResource(R.drawable.ic_phone_circle_green)
            }else {
                imvUserImage.setUserImage(user?.profileImage)
            }
            tvUserName.text = user?.name ?: user?.fullName ?: ((user?.firstName ?: "") + " " + (user?.lastName ?: ""))
        }
        setupFriendButton(user?.id)
        showHideOptionsUI(user?.id)
        setupBottomUI(isLast)
        adjustUIs()
    }

    private fun adjustUIs() {
        itemBinding.apply {
            var maxAvailableWidth = OSHelper.widthScreen - ((16 * 2) + 40 + 8).toPx()
            if (btnMenu.visibility == View.VISIBLE) {
                maxAvailableWidth -= (24 + 8).toPx()
            }
            if (imvFriend.visibility == View.VISIBLE) {
                maxAvailableWidth -= (24 + 4).toPx()
            }
            if (btnAddFriend.visibility == View.VISIBLE) {
                maxAvailableWidth -= (8.toPx() + tvFriend.getTextWidth())
            }
            if (btnChat.visibility == View.VISIBLE) {
                maxAvailableWidth -= ((24 + 4 + 8).toPx() + tvBtnChat.getTextWidth())
            }
            if (layoutOrganizer.visibility == View.VISIBLE) {
                maxAvailableWidth -= ((5 + 5).toPx() + tvDot.getTextWidth() + tvOrganizerMark.getTextWidth())
            }
            tvUserName.maxWidth = maxAvailableWidth
        }
    }


    private fun showHideOptionsUI(userId: String?) {
        itemBinding.apply {
            tvDescription.visibility = View.GONE
            btnMenu.visibility = View.GONE
            btnAddFriend.visibility = View.GONE
            btnChat.visibility = View.GONE

            var isShownOrganiMark = false
            var textOrganizer = "Organizer"

            if (userId.isNullOrEmpty() || (userId != UserInfo.userId && user?.isActive == true)) {
                when(holderType) {
                    HolderType.PEOPLE_INVITED -> {
                        btnMenu.visibility = if (event?.userId == UserInfo.userId) View.VISIBLE else View.GONE
                        if (user?.friendShipStatus == 1) {
                            btnChat.visibility = View.VISIBLE
                        }else {
                            btnAddFriend.visibility = View.VISIBLE
                        }
                        isShownOrganiMark = userId == event?.userId
                    }
                    HolderType.PLANS_USER -> {
                        btnAddFriend.visibility = View.VISIBLE
                    }
                    HolderType.CONTACTS -> {
                        btnAddFriend.visibility = View.VISIBLE
                        tvDescription.visibility = View.VISIBLE
                        tvDescription.text = user?.mobile?.formatPhoneNumber(
                            PLANS_APP.currentActivity,
                            numberFormat = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL,
                            separator = "-",
                            isRemovedOwnCountryCode = true)
                    }
                    HolderType.CHAT_SETTINGS -> {
                        btnAddFriend.visibility = View.VISIBLE
                        if (chat?.isGroup == true && chat?.organizer?.id == UserInfo.userId) {
                            btnMenu.visibility = View.VISIBLE
                        }
                    }
                }
            }

            when (holderType) {
                HolderType.CHAT_SETTINGS -> {
                    isShownOrganiMark = chat?.organizer?.id == userId
                    textOrganizer = if (chat?.isEventChat == true) "Organizer" else "Admin"
                }
                else -> {}
            }

            updateOrganizerMark(isShownOrganiMark, textOrganizer)
        }
    }

    private fun updateOrganizerMark(isShown: Boolean = false, textOrganizer: String? = "Organizer") {
        itemBinding.apply {

            tvOrganizerMark.text = textOrganizer
            tvOrganizerUpMark.text = textOrganizer

            tvOrganizerUpMark.visibility = View.GONE
            layoutOrganizer.visibility = if (isShown) View.VISIBLE else View.GONE
        }
    }

    private fun setupFriendButton(userId: String?) {
        if (userId == UserInfo.userId) return

        var title: String? = null
        var image: Int? = null
        var color = PlansColor.PURPLE_JOIN

        if (!userId.isNullOrEmpty()) {
            when (user?.friendShipStatus) {
                0 -> {
                    if (user?.friendRequestSender == UserInfo.userId) {
                        // Requested
                        title = "REQUESTED"
                        image = R.drawable.ic_clock_grey
                        color = PlansColor.GRAY_MAIN
                    }else {
                        // Confirm Request
                        title = "CONFIRM REQUEST"
                        image = R.drawable.ic_check_purple
                    }
                }
                1 -> {
                    // Friends
                    title = "FRIENDS"
                    image = R.drawable.ic_users_purple
                }
                5 -> {
                    // Unblock
                    if (user?.friendRequestSender == UserInfo.userId) {
                        title = "UNBLOCK"
                        image = R.drawable.ic_unlock_black
                        color = PlansColor.BLACK
                    }else {
                        title = "BLOCKED"
                        color = PlansColor.GRAY_LABEL
                    }
                }
                else -> {
                    // Add Friend
                    title = "ADD FRIEND"
                    image = R.drawable.ic_plus_purple
                }
            }
        }else {
            user?.invitedTime?.takeIf { it.toLocalDateTime().toEpochSecond(ZoneOffset.UTC) > LocalDateTime.now().minusDays(2).toEpochSecond(
                ZoneOffset.UTC) }?.let {
                // Invited
                title = "INVITED"
                image = R.drawable.ic_check_circle_purple
            } ?: run{
                // Invite
                title = "INVITE"
                image = R.drawable.ic_enter_black
                color = PlansColor.BLACK
            }
        }

        itemBinding.apply {
            tvFriend.text = title
            tvFriend.setTextColor(color)
            imvFriend.visibility = if (image == null) View.GONE else {
                imvFriend.setImageResource(image!!)
                View.VISIBLE
            }
        }

    }

    private fun setupBottomUI(isLast: Boolean = false) {
        itemBinding.apply {
            when(holderType) {
                HolderType.CHAT_SETTINGS -> {
                    separatorBottom.visibility = if (isLast) View.GONE else {
                        separatorBottom.setLayoutMargin(16.toPx())
                        View.VISIBLE
                    }
                }
                HolderType.PEOPLE_INVITED -> {
                    itemView.setLayoutMargin(bottom = if(isLast) 80.toPx() else 0)
                }
                else -> {}
            }
        }
    }



}

