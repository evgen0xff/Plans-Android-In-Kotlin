package com.planscollective.plansapp.webServices.base

import com.google.gson.annotations.SerializedName

open class ResponseModel<T> {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("time")
    var time: String? = null

    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @SerializedName("response")
    var response: T? = null

    @SerializedName("result")
    var result: T? = null

}