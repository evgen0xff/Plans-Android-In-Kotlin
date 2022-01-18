package com.planscollective.plansapp.models.dataModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class UserLikedModel(
    @Expose
    @SerializedName("created", alternate = ["createdAt"])
    var createdAt: Number? = null,

    @Expose
    @SerializedName("userDetails")
    var userDetails: UserModel? = null,

) : UserModel() {
}