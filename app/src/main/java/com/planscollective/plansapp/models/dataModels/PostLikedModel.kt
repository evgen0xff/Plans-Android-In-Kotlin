package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class PostLikedModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("postMedia")
    var postMedia: String? = null,

    @Expose
    @SerializedName("eventId")
    var eventId: String? = null,

    @Expose
    @SerializedName("postText")
    var postText: String? = null,

    @Expose
    @SerializedName("postType")
    var postType: String? = null,

    @Expose
    @SerializedName("userId")
    var userId: String? = null,

    @Expose
    @SerializedName("eventName")
    var eventName: String? = null,

    @Expose
    @SerializedName("firstName")
    var firstName: String? = null,

    @Expose
    @SerializedName("lastName")
    var lastName: String? = null,

    @Expose
    @SerializedName("postImageUrl")
    var postImageUrl: String? = null,

    @Expose
    @SerializedName("eventImageUrl")
    var eventImageUrl: String? = null,

    @Expose
    @SerializedName("message")
    var message: String? = null,

    @Expose
    @SerializedName("width")
    var width: Number? = null,

    @Expose
    @SerializedName("height")
    var height: Number? = null,

) : Parcelable {
}