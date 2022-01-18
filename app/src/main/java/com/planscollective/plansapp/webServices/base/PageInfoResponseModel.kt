package com.planscollective.plansapp.webServices.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PageInfoResponseModel {
    @Expose
    @SerializedName("count")
    var count: Int? = null
}