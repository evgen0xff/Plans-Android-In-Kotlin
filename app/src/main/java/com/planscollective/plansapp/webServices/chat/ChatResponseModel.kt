package com.planscollective.plansapp.webServices.chat

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatResponseModel(
    @Expose
    @SerializedName("imageUrl")
    var imageUrl: String? = null,

    @Expose
    @SerializedName("videoUrl")
    var videoUrl: String? = null,

) : Parcelable {
}