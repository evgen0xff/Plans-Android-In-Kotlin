package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.*
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.*
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.webServices.user.UserWebService
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NotificationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class HolderType {
        INVITATION,
        SECTION_HEADER_ACTIVITY,
        SECTION_HEADER_NEW,
        ACTIVITY,
        EMPTY_SPACE,
    }

    private var listener: PlansActionListener? = null
    private var listItems = ArrayList<HashMap<String, Any?>>()

    private var notifications: NotificationsModel? = null
    private var indexOfFirstSeenNotify: Int? = null
    private var indexOfFirstNotify: Int? = null
    private var indexOfFirstEventImage: Int? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(notifications: NotificationsModel? = null,
                      listener: PlansActionListener? = null
    ) {
        notifications?.also {
            this.notifications = it
        }
        listener?.also {
            this.listener = it
        }

        indexOfFirstSeenNotify = null

        updateNotifications()
        updateLastViewTimeForNotify()

        notifyDataSetChanged()
    }

    private fun updateNotifications() {
        listItems.clear()

        if ((notifications?.eventInvitationCount ?: 0) + (notifications?.friendRequestCount ?: 0) > 0) {
            listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.INVITATION, "DATA" to notifications))
            listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.EMPTY_SPACE, "DATA" to notifications))
        }

        if (notifications?.listActivities?.isNotEmpty() == true ) {
            listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.SECTION_HEADER_ACTIVITY, "DATA" to notifications))
        }

        notifications?.listActivities?.filter { it.isNew }?.takeIf { it.isNotEmpty() }?.also {
            listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.SECTION_HEADER_NEW, "DATA" to notifications))
            for (i in it.indices) {
                val isLast = i == it.size - 1
                listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.ACTIVITY,
                    "DATA" to it[i],
                    "IS_LAST" to isLast))
            }
            listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.EMPTY_SPACE, "DATA" to notifications))
        }

        notifications?.listActivities?.filter { !it.isNew }?.takeIf { it.isNotEmpty() }?.also {
            indexOfFirstSeenNotify = listItems.size
            for (i in it.indices) {
                val isLast = i == it.size - 1
                listItems.add(hashMapOf("HOLDER_TYPE" to HolderType.ACTIVITY,
                    "DATA" to it[i],
                    "IS_LAST" to isLast))
            }
        }

        indexOfFirstNotify = listItems.indexOfFirst { it["HOLDER_TYPE"] == HolderType.ACTIVITY }.takeIf { it > -1 }?.let {
            listItems[it]["IS_FIRST_NOTIFY"] = true
            it
        }

        indexOfFirstEventImage = listItems.indexOfFirst { it["HOLDER_TYPE"] == HolderType.ACTIVITY && !(it["DATA"] as? NotificationActivityModel)?.image.isNullOrEmpty()}
            .takeIf { it > -1 }?.let {
            listItems[it]["IS_FIRST_EVENT_IMAGE"] = true
            it
        }

    }

    private fun updateLastViewTimeForNotify(position: Int? = null) {
        val time = Date().time / 1000.toLong()
        if (position == null) {
            UserInfo.lastViewTimeForNotify = time
        }
        if (indexOfFirstSeenNotify == position) {

            UserWebService.updateLastViewTimeForNotify(time){ user, _ ->
                if (user != null) {
                    UserInfo.lastViewTimeForNotify = time
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            HolderType.INVITATION.ordinal -> {
                val itemBinding = CellNotifyInvitationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                InvitationsVH(itemBinding)
            }
            HolderType.EMPTY_SPACE.ordinal -> {
                val itemBinding = CellEmptySpaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder<Any>(itemBinding.root)
            }
            HolderType.SECTION_HEADER_ACTIVITY.ordinal -> {
                val itemBinding = CellSectionHeaderActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder<Any>(itemBinding.root)
            }
            HolderType.SECTION_HEADER_NEW.ordinal -> {
                val itemBinding = CellSectionHeaderNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder<Any>(itemBinding.root)
            }
            HolderType.ACTIVITY.ordinal -> {
                val itemBinding = CellNotifyActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ActivityNotificationVH(itemBinding)
            }
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listItems[position]
        when(holder) {
            is InvitationsVH -> {
                (holder as? InvitationsVH)?.bind(item["DATA"] as? NotificationsModel)
            }
            is ActivityNotificationVH -> {
                (holder as? ActivityNotificationVH)?.bind(item["DATA"] as? NotificationActivityModel,
                    data = hashMapOf("IS_FIRST_NOTIFY" to (item["IS_FIRST_NOTIFY"] as? Boolean ?: false), "IS_FIRST_EVENT_IMAGE" to (item["IS_FIRST_EVENT_IMAGE"] as? Boolean ?: false)),
                    isLast = item["IS_LAST"] as? Boolean ?: false)
            }
        }

        updateLastViewTimeForNotify(position)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return (listItems[position]["HOLDER_TYPE"] as? HolderType)?.ordinal ?: 0
    }

    //***************************** Invitations ViewHolder ***********************************//
    inner class InvitationsVH(var itemBinding: CellNotifyInvitationsBinding) : BaseViewHolder<NotificationsModel>(itemBinding.root) {
        private var notifyModel: NotificationsModel? = null

        private var listEvents = ArrayList<EventModel>()

        private var usersInvited = ArrayList<UserModel>()
        private var usersInvitedLast3 = ArrayList<UserModel>()

        private var listFriendRequest = ArrayList<FriendRequestModel>()
        private var listFriendRequestLast3 = ArrayList<FriendRequestModel>()
        private var listImageLayouts = ArrayList<ViewGroup>()
        private var listImageViews = ArrayList<ImageView>()

        init {
            itemBinding.apply {
                layoutInvitations.setOnSingleClickListener {
                    if (usersInvited.size > 0) {
                        listener?.onClickedEventInvitations()
                    }else {
                        listener?.onClickedFriendRequests()
                    }
                }
                layoutInvitationsEvent.setOnSingleClickListener {
                    listener?.onClickedEventInvitations()
                }
                layoutInvitationsFriend.setOnSingleClickListener {
                    listener?.onClickedFriendRequests()
                }
                listImageLayouts.add(layoutUser1)
                listImageLayouts.add(layoutUser2)
                listImageLayouts.add(layoutUser3)

                listImageViews.add(imvUser1)
                listImageViews.add(imvUser2)
                listImageViews.add(imvUser3)
            }
        }

        override fun bind(item: NotificationsModel?, data: Any?, isLast: Boolean) {
            notifyModel = item
            updateData()
            updateUI()
        }

        private fun updateData() {
            // Event Invitations
            listEvents.clear()
            usersInvited.clear()
            usersInvitedLast3.clear()
            notifyModel?.eventInvitationList?.takeIf{it.isNotEmpty()}?.also{
                listEvents.addAll(it)
            }
            listEvents.forEach { event ->
                event.takeIf { it.eventCreatedBy != null && !usersInvited.any { it1 -> it1.id == it.eventCreatedBy?.id } }?.also {
                    usersInvited.add(it.eventCreatedBy!!)
                }
            }
            usersInvited.sortByDescending { user ->
                val inviteTime = listEvents.firstOrNull { it.eventCreatedBy?.id == user.id }?.let { event ->
                    event.invitations?.firstOrNull { it.id == UserInfo.userId }?.invitationTime?.toLong()
                } ?: 0
                inviteTime
            }
            usersInvitedLast3.addAll(usersInvited.take(3))

            // Friend Requests
            listFriendRequest.clear()
            listFriendRequestLast3.clear()
            notifyModel?.friendRequestList?.takeIf { it.isNotEmpty() }?.also {
                listFriendRequest.addAll(it.reversed())
                listFriendRequestLast3.addAll(listFriendRequest.take(3))
            }
        }

        private fun updateUI() {
            itemBinding.apply {
                layoutInvitationsEventFriend.visibility = if (usersInvited.isNotEmpty() && listFriendRequest.isNotEmpty()) {
                    updateEventFriendUI()
                    layoutInvitations.visibility = View.GONE
                    View.VISIBLE
                }else {
                    updateInvitationsUI()
                    layoutInvitations.visibility = View.VISIBLE
                    View.GONE
                }
            }
        }

        private fun updateEventFriendUI() {
            itemBinding.apply {
                imvInvitationsEvent.setUserImage(usersInvited.firstOrNull()?.profileImage)
                tvCountEventInvite.text = "${notifyModel?.eventInvitationCount ?: 0}"

                imvInvitationsFriend.setUserImage(listFriendRequest.firstOrNull()?.senderDetail?.profileImage)
                tvCountFriendInvite.text = "${notifyModel?.friendRequestCount ?: 0}"
            }
        }

        private fun updateInvitationsUI() {
            var html = ""
            val listTexts = ArrayList<String>()
            var title = ""
            listImageLayouts.forEach { it.visibility = View.GONE }

            if (usersInvitedLast3.isNotEmpty()) {
                title = "Event Invitations"
                for (i in usersInvitedLast3.indices) {
                    val fullName = "${usersInvitedLast3[i].firstName ?: ""} ${usersInvitedLast3[i].lastName ?: ""}"
                    listTexts.add(fullName)
                    listImageViews[i].setUserImage(usersInvitedLast3[i].profileImage)
                    listImageLayouts[i].visibility = View.VISIBLE
                }
                html = if (usersInvited.size > 3) {
                    "<b>${listTexts[0]}, ${listTexts[1]}</b> and <b>${usersInvited.size - 2} others</b> sent you an event invitation."
                }else if (usersInvited.size == 3) {
                    "<b>${listTexts[0]}, ${listTexts[1]}</b> and <b>1 other</b> sent you an event invitation."
                }else if (usersInvited.size == 2) {
                    "<b>${listTexts[0]}</b> and <b>${listTexts[1]}</b> sent you an event invitation."
                }else {
                    "<b>${listTexts[0]}</b> sent you an event invitation."
                }
            }else if (listFriendRequestLast3.isNotEmpty()) {
                title = "Friend Requests"
                for (i in listFriendRequestLast3.indices) {
                    val fullName = "${listFriendRequestLast3[i].senderDetail?.firstName ?: ""} ${listFriendRequestLast3[i].senderDetail?.lastName ?: ""}"
                    listTexts.add(fullName)
                    listImageViews[i].setUserImage(listFriendRequestLast3[i].senderDetail?.profileImage)
                    listImageLayouts[i].visibility = View.VISIBLE
                }
                html = if (listFriendRequest.size > 3) {
                    "<b>${listTexts[0]}, ${listTexts[1]}</b> and <b>${listFriendRequest.size - 2} others</b> sent you a friend request."
                }else if (listFriendRequest.size == 3) {
                    "<b>${listTexts[0]}, ${listTexts[1]}</b> and <b>1 other</b> sent you a friend request."
                }else if (listFriendRequest.size == 2) {
                    "<b>${listTexts[0]}</b> and <b>${listTexts[1]}</b> sent you a friend request."
                }else {
                    "<b>${listTexts[0]}</b> sent you a friend request."
                }
            }

            itemBinding.apply {
                tvInvitations.text = title
                tvInvitationsMsg.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                else
                    Html.fromHtml(html)
            }
        }
    }

    //***************************** Activity Notification ViewHolder **************************//
    fun updateGuideNotification(isNotifyGuide: Boolean = false, isEventImageGuide: Boolean = false) {
        if (isNotifyGuide && !UserInfo.isSeenGuideTapHoldNotification) {
            UserInfo.isSeenGuideTapHoldNotification = true
            indexOfFirstNotify?.takeIf { it > -1 && it < listItems.size}?.also {
                notifyItemChanged(it)
            }
        }

        if (isEventImageGuide && !UserInfo.isSeenGuideTapViewEvent) {
            UserInfo.isSeenGuideTapViewEvent = true
            indexOfFirstEventImage?.takeIf { it > -1 && it < listItems.size }?.also {
                notifyItemChanged(it)
            }
        }
    }

    inner class ActivityNotificationVH(var itemBinding: CellNotifyActivityBinding): BaseViewHolder<NotificationActivityModel>(itemBinding.root){
        var notify: NotificationActivityModel? = null

        init {
            itemView.setOnLongClickListener {
                updateGuideNotification(isNotifyGuide = true)
                listener?.onLongClickNotification(notify)
                true
            }

            itemView.setOnSingleClickListener {
                listener?.onClickNotification(notify)
            }

            itemBinding.imvPhoto.setOnSingleClickListener {
                updateGuideNotification(isEventImageGuide = true)
                listener?.onClickedEvent(eventId = notify?.eventId)
            }

            itemBinding.imvGuideTapViewEvent.setOnSingleClickListener {
                updateGuideNotification(isEventImageGuide = true)
                listener?.onClickedEvent(eventId = notify?.eventId)
            }
        }

        override fun bind(item: NotificationActivityModel?, data: Any?, isLast: Boolean) {
            notify = item
            val isFirstNotify = (data as? HashMap<*, *>)?.get("IS_FIRST_NOTIFY") as? Boolean ?: false
            val isFirstEventImage = (data as? HashMap<*, *>)?.get("IS_FIRST_EVENT_IMAGE") as? Boolean ?: false

            itemBinding.apply {
                // Bottom Separator
                separatorBottom.visibility = if (isLast) View.GONE else View.VISIBLE

                // Profile Images
                val mainProfile = notify?.userImage ?: notify?.profileImage
                layoutUser1.visibility = if (mainProfile == null) View.GONE else{
                    imvUser1.setUserImage(mainProfile)
                    View.VISIBLE
                }

                layoutUser2.visibility = if (notify?.userImage2 == null) View.GONE else{
                    imvUser2.setUserImage(notify?.userImage2)
                    View.VISIBLE
                }

                layoutUsers.visibility = if (layoutUser1.visibility == View.GONE && layoutUser2.visibility == View.GONE) View.GONE else View.VISIBLE

                // Live Mark
                imvMarkLive.visibility = if (notify?.isLive == 1) View.VISIBLE else View.GONE


                // Photo Image
                imvPhoto.visibility = if (notify?.image.isNullOrEmpty()) View.GONE else{
                    imvPhoto.setEventImage(notify?.image)
                    View.VISIBLE
                }

                // Message
                tvMessage.updateNotify(notify)

                // Guide
                imvGuideTapHoldNotification.visibility = if (isFirstNotify && !UserInfo.isSeenGuideTapHoldNotification) View.VISIBLE else View.GONE
                imvGuideTapViewEvent.visibility = if (isFirstEventImage && !UserInfo.isSeenGuideTapViewEvent && imvPhoto.visibility == View.VISIBLE) View.VISIBLE else View.GONE
            }
        }

    }


}