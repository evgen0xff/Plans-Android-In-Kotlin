package com.planscollective.plansapp.models.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatUserModel(
    @Expose
    @SerializedName("shownStatus")
    var shownStatus:        Int? = null,          // 1 -> Shown, 2 -> Hidden, 3 -> Deleted

    @Expose
    @SerializedName("isMuteNotification")
    var isMuteNotification: Boolean? = null,           // Push Notify On/Off

    @Expose
    @SerializedName("isJoin")
    var isJoin:             Boolean? = null,

    @Expose
    @SerializedName("lastAccessTime")
    var lastAccessTime:     Number? = null,

    @Expose
    @SerializedName("deletedTime")
    var deletedTime:        Number? = null,

    @Expose
    @SerializedName("isHostChat")
    var isHostChat:         Boolean? = null,           // Check if the user is the host or not.

    @Expose
    @SerializedName("lastMessageId")
    var lastMessageId:      String? = null,

    @Expose
    @SerializedName("lastMessage")
    var lastMessage:        MessageModel? = null,
) : UserModel() {

}