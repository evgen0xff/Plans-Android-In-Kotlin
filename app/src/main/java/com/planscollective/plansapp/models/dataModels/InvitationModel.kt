package com.planscollective.plansapp.models.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class InvitationModel(
    @Expose
    @SerializedName("status")
    var status: Int? = null, // 1 -> Invited, 2 -> Going, 3 -> Maybe, 4 -> Next Time

    @Expose
    @SerializedName("isLive")
    var isLive: Int? = null,

    @Expose
    @SerializedName("turnOffNoti")
    var turnOffNoti: Boolean? = null,

    @Expose
    @SerializedName("invitationTime")
    var invitationTime: Number? = null,

    @Expose
    @SerializedName("locationArrivedTime")
    var locationArrivedTime: Number? = null,

    @Expose
    @SerializedName("invitedType")
    var invitedType: Int? = null,
) : UserModel() {

    enum class InviteType(val value : Int) {
        FRIEND(0) {
            override var title: String? = "FRIENDS"
        },          // User who is friended with me on Plans
        CONTACT(1) {
            override var title: String? = "CONTACTS"
        },         // User who is added from Phone Contacts
        EMAIL(2) {
            override var title: String? = "EMAIL"
        },           // User who is added through the app by email
        LINK(3) {
            override var title: String? = "LINK"
        },            // User who is invited by Share Link
        MOBILE(4){
                 override  var title: String? = "MOBILE"
        },          // User who is added through the app by mobile number
        PLANS_USER(5){
            override var title: String? = "PLANS USER"
        };       // User who is an user on Plans
        abstract var title: String?
    }

    var typeInvitation : InviteType? = null
        get() = invitedType?.let { it1 ->
            InviteType.values().firstOrNull { it2 -> it2.value == it1  }
        }
        set(value) {
            field = value
            invitedType = value?.value
        }


    constructor(user: UserModel?) : this() {
        id = user?.id
        email = user?.email
        mobile = user?.mobile
        name = user?.name
        fullName = user?.fullName
        firstName = user?.firstName
        lastName = user?.lastName
        profileImage = user?.profileImage
        lat = user?.lat
        long = user?.long
        isActive = user?.isActive
        isBlock = user?.isBlock
        isFriend = user?.isFriend
        friendShipStatus = user?.friendShipStatus
        friendRequestSender = user?.friendRequestSender
    }

}