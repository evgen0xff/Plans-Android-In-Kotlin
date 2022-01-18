package com.planscollective.plansapp.webServices.place

import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.webServices.base.ErrorResponseModel

open class PlaceResponseModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("time")
    var time: String? = null

    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @SerializedName("results")
    var results: ArrayList<PlaceModel>? = null

    @SerializedName("next_page_token")
    var nextPageToken: String? = null

}
