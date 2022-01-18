package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class FriendRequestModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("friendShip")
    var friendShip: Int? = null,

    @Expose
    @SerializedName("senderId")
    var senderId: String? = null,

    @Expose
    @SerializedName("receiverId")
    var receiverId: String? = null,

    @Expose
    @SerializedName("createAt")
    var createAt: Number? = null,

    @Expose
    @SerializedName("senderDetail")
    var senderDetail: UserModel? = null,

) : Parcelable {
}