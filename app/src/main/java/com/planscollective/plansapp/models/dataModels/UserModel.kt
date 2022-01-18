package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

@Parcelize
open class UserModel(
        @Expose
        @SerializedName("_id", alternate = ["userId", "friendId"])
        var id: String? = null,

        @Expose
        @SerializedName("email")
        var email: String? = null,

        @Expose
        @SerializedName("mobile", alternate = ["mobileNumber"])
        var mobile: String? = null,

        @Expose
        @SerializedName("name")
        var name: String? = null,

        @Expose
        @SerializedName("fullName")
        var fullName: String? = null,

        @Expose
        @SerializedName("firstName")
        var firstName: String? = null,

        @Expose
        @SerializedName("lastName")
        var lastName: String? = null,

        @Expose
        @SerializedName("profileImage")
        var profileImage: String? = null,

        @Expose
        @SerializedName("lat")
        var lat: Number? = null,

        @Expose
        @SerializedName("long")
        var long: Number? = null,

        @Expose
        @SerializedName("dob")
        var dob: Number? = null,

        @Expose
        @SerializedName("socialId")
        var socialId: String? = null,

        @Expose
        @SerializedName("fcmId")
        var fcmId: String? = null,

        @Expose
        @SerializedName("facebookImage")
        var facebookImage: String? = null,

        @Expose
        @SerializedName("userLocation", alternate = ["location"])
        var userLocation: String? = null,

        @Expose
        @SerializedName("password")
        var password: String? = null,

        @Expose
        @SerializedName("accessToken")
        var accessToken: String? = null,

        @Expose
        @SerializedName("isActive")
        var isActive: Boolean? = null,

        @Expose
        @SerializedName("isBlock")
        var isBlock: Boolean? = null,

        @Expose
        @SerializedName("isPrivateAccount")
        var isPrivateAccount: Boolean? = null,

        @Expose
        @SerializedName("lastViewTimeForNotify")
        var lastViewTimeForNotify: Number? = null,

        @Expose
        @SerializedName("userType")
        var userType: Int? = null,

        @Expose
        @SerializedName("bio")
        var bio: String? = null,

        @Expose
        @SerializedName("coinNumber")
        var coinNumber: Int? = null,

        @Expose
        @SerializedName("isFriend")
        var isFriend: Boolean? = null,

        @Expose
        @SerializedName("friendShipStatus")
        var friendShipStatus: Int? = null,

        @Expose
        @SerializedName("friendRequestSender")
        var friendRequestSender: String? = null,

        @Expose
        @SerializedName("invitedTime")
        var invitedTime: Number? = null,

        @Expose
        @SerializedName("eventCount")
        var eventCount: Int? = null,

        @Expose
        @SerializedName("friendsCount")
        var friendsCount: Int? = null,

        @Expose
        @SerializedName("blockedBy")
        var blockedBy: String? = null,

        @Expose
        @SerializedName("deviceType")
        var deviceType: Number? = null,                 // 0 -> iPhone, 1 -> Android
): Parcelable {

        var birthDay: Date? = null
        var countryNameCode: String? = null
        var phoneNumCode: String? = null
        var loginType: String? = null

        fun localMobile() : String? {
              return mobile?.substring(mobile?.length ?: 0)?.trim()
        }

        fun getCountLiveEvents() : String? {
                val coin = coinNumber?.takeIf{ it > 0 } ?: return null
                var count = 0
                when (coin) {
                        1 -> count = 1
                        2 -> count = 10
                        3 -> count = 25
                        4 -> count = 100
                        5 -> count = 200
                        6 -> count = 300
                        7 -> count = 400
                        8 -> count = 500
                        9 -> count = 600
                        10 -> count = 700
                        11 -> count = 800
                        12 -> count = 900
                        13 -> count = 1000
                        14 -> count = 1250
                        15 -> count = 1500
                        16 -> count = 2000
                        17 -> count = 2500
                        18 -> count = 3000
                }

                return "$count+";
        }

        fun getAccessForMe() : Triple<Boolean, Boolean, Boolean>{
                val isPrivate = isPrivateAccount ?: true
                val isFriends = friendShipStatus == 1
                val isBlockedByMe =  if (isBlock == true) true else blockedBy == UserInfo.userId
                val isAccess = if (isBlockedByMe) false else if (isPrivate) isFriends else true

                return Triple(isAccess, isBlockedByMe, isPrivate)
        }

        fun toMap() : MutableMap<String, RequestBody?>? {
                val map: MutableMap<String, RequestBody?> = HashMap()
                id?.also{
                        map["_id"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                firstName?.also{
                        map["firstName"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                lastName?.also{
                        map["lastName"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                bio?.also{
                        map["bio"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                userLocation?.also{
                        map["userLocation"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                long?.also{
                        map["long"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                }
                lat?.also{
                        map["lat"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                }
                isPrivateAccount?.also {
                        map["isPrivateAccount"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                }

                return map
        }

}
