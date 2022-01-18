package com.planscollective.plansapp.webServices.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.models.dataModels.UserModel

class AuthResponseModel {

    @SerializedName("otp")
    var otp: String? = null

    @SerializedName("isVerified")
    var isVerified: Boolean? = null

    @Expose
    @SerializedName("message")
    var message: String? = null

    @Expose
    @SerializedName("accessToken")
    var accessToken: String? = null

    @Expose
    @SerializedName("userProfile")
    var userProfile: UserModel? = null

    @Expose
    @SerializedName("userDetails")
    var userDetails: UserModel? = null

    @Expose
    @SerializedName("data")
    var data: UserModel? = null
}
