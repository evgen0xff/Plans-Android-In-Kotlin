package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class LiveMomentModel(
    @Expose
    @SerializedName("liveMommentId")
    var id: String? = null,

    @Expose
    @SerializedName("media")
    var media: String? = null,

    @Expose
    @SerializedName("liveThumbnail")
    var liveThumbnail: String? = null,

    @Expose
    @SerializedName("mediaType")
    var mediaType: String? = null,

    @Expose
    @SerializedName("url")
    var imageOrVideo: String? = null,

    @Expose
    @SerializedName("createdAt")
    var createdAt: Number? = null,

    @Expose
    @SerializedName("isViewed")
    var isViewed: Boolean? = null,

) : Parcelable{

}