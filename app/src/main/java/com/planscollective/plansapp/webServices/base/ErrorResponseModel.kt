package com.planscollective.plansapp.webServices.base

import com.google.gson.annotations.SerializedName

class ErrorResponseModel {
    @SerializedName("message")
    val message: String? = null

    @SerializedName("errorCode")
    val errorCode: String? = null
}