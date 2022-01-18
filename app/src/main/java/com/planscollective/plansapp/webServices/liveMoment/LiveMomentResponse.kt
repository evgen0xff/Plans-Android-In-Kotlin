package com.planscollective.plansapp.webServices.liveMoment

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.webServices.base.ErrorResponseModel

class LiveMomentResponse {
    @Expose
    @SerializedName("message")
    var message: String? = null

    @Expose
    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @Expose
    @SerializedName("liveMommentsList")
    var liveMoments: ArrayList<UserLiveMomentsModel>? = null

    fun getLiveMomentList() : ArrayList<UserLiveMomentsModel>?{
        liveMoments?.forEach{it.prepare()}
        liveMoments =  liveMoments?.sortedWith(compareByDescending<UserLiveMomentsModel> { it.user?.id == UserInfo.userId }
            .thenBy { it.isAllSeen }
            .thenByDescending { it.timeLatest?.toInt() })?.toArrayList()

        return liveMoments
    }

}