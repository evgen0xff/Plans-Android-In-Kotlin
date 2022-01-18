package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.R
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("nameGroup")
    var nameGroup:              String? = null,

    @Expose
    @SerializedName("lastMessageTime")
    var lastMessageTime:        Number? = null,

    @Expose
    @SerializedName("members")
    var members:                ArrayList<ChatUserModel>? = null,

    @Expose
    @SerializedName("organizer")
    var organizer:              ChatUserModel? = null,

    @Expose
    @SerializedName("lastMessageId")
    var lastMessageId:          String? = null,

    @Expose
    @SerializedName("eventId")
    var eventId:                String? = null,

    @Expose
    @SerializedName("unreadMessages")
    var unreadMessages:         ArrayList<MessageModel>? = null,

    @Expose
    @SerializedName("allMessages")
    var allMessages:            ArrayList<MessageModel>? = null,

    @Expose
    @SerializedName("peopleEvent")
    var peopleEvent:            ArrayList<UserModel>? = null,

    @Expose
    @SerializedName("unreadCount")
    var countUnreadMessages:    Int? = null,

    @Expose
    @SerializedName("event")
    var event: EventModel? = null,

    @Expose
    @SerializedName("lastMessage")
    var lastMessage:     MessageModel? = null,

) : Parcelable {

    val profileImage: String?
        get() {
            return if (isEventChat) event?.urlCoverImage else profileUser?.profileImage
        }

    val isEventChat : Boolean
        get() {
            return !eventId.isNullOrEmpty() || !event?.id.isNullOrEmpty()
        }

    val profileUser: ChatUserModel?
        get() {
            return if (isGroup) lastUser else members?.firstOrNull { it.id != UserInfo.userId }
        }

    val isGroup: Boolean
        get() {
            return if (isEventChat) true else (members?.size ?: 0) > 2
        }

    val lastUser: ChatUserModel?
        get() {
            return members?.firstOrNull()
        }

    val profileNextUser: ChatUserModel?
        get() {
            return if (!isEventChat && isGroup) nextUser else null
        }

    val nextUser: ChatUserModel?
        get() {
            return if ((members?.size ?: 0) > 1) members?.get(1) else null
        }

    val people: ArrayList<UserModel>?
        get() {
            return (if (isEventChat) peopleEvent else members)?.let {
                if (!isGroup) it.filter { it1 -> it1.id != UserInfo.userId } else it
            }?.toArrayList()
        }

    val titleChat: String?
        get() {
            return if (isEventChat) {
                event?.eventName
            }else if (!isGroup) {
                "${profileUser?.firstName ?: ""} ${profileUser?.lastName ?: ""}"
            }else {
                if (!nameGroup.isNullOrEmpty()) {
                    nameGroup
                }else {
                    people?.filter{ it.id != UserInfo.userId }?.map{ it.firstName }?.let {
                        if (it.size < 3) it.joinToString(", ") else {
                            it.subList(0, 2).joinToString(", ") + " and ${it.size - 2} more"
                        }
                    }
                }
            }
        }

    @IgnoredOnParcel
    var isMuteNotification: Boolean = false
        get() {
            return members?.firstOrNull { it.id == UserInfo.userId }?.isMuteNotification ?: false
        }
        set(value) {
            field = value
            members?.firstOrNull { it.id == UserInfo.userId }?.isMuteNotification = value
        }

    fun getMenuOptions() : ArrayList<MenuModel> {
        val list = ArrayList<MenuModel>()
        if (isMuteNotification) {
            list.add(MenuModel(R.drawable.ic_bell_off_black_menu, "Unmute Notifications"))
        }else {
            list.add(MenuModel(R.drawable.ic_bell_off_black_menu, "Mute Notifications"))
        }

        val isOrganizer = organizer?.id == UserInfo.userId

        // Add People / Leave Chat
        var addPeople: MenuModel? = MenuModel(R.drawable.ic_user_plus_black_2, "Add People")
        var leaveChat = if (isGroup) MenuModel(R.drawable.ic_log_out_black_menu, "Leave Chat")  else null

        if (isEventChat) {
            if (!isOrganizer) {
                leaveChat = if (event?.isLive == 1) MenuModel(R.drawable.ic_log_out_black_menu, "Leave Event") else null
                addPeople =  null
            }else if (event?.isCancel == true ||
                event?.isActive == false ||
                event?.isEnded == 1) {
                addPeople =  null
                leaveChat = null
            }else if (event?.isLive == 1) {
                leaveChat = MenuModel(R.drawable.ic_x_circle_black_menu, "End Event")
                addPeople = MenuModel(R.drawable.ic_user_plus_black_2, "Invite People")
            }else if (event?.isExpired == true){
                leaveChat = MenuModel(R.drawable.ic_x_circle_black_menu, "Cancel Event")
                addPeople = null
            }else {
                leaveChat = MenuModel(R.drawable.ic_x_circle_black_menu, "Cancel Event")
                addPeople = MenuModel(R.drawable.ic_user_plus_black_2, "Invite People")
            }
        }

        addPeople?.also {
            list.add(it)
        }

        list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Chat"))

        leaveChat?.also {
            list.add(it)
        }

        return list
    }
}