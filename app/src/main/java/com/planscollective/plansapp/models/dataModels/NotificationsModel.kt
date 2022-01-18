package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class NotificationsModel(
    @Expose
    @SerializedName("eventInvitationCount")
    var eventInvitationCount: Int? = null,

    @Expose
    @SerializedName("friendRequestCount")
    var friendRequestCount: Int? = null,

    @Expose
    @SerializedName("eventInvitationList")
    var eventInvitationList: ArrayList<EventModel>? = null,

    @Expose
    @SerializedName("friendRequestList")
    var friendRequestList: ArrayList<FriendRequestModel>? = null,

    @Expose
    @SerializedName("totalCountActivities")
    var totalCountActivities: Int? = null,

    @Expose
    @SerializedName("listActivities")
    var listActivities: ArrayList<NotificationActivityModel>? = null

) : Parcelable {}