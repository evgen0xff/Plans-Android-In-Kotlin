package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class EventLink(
    @Expose
    @SerializedName("invitation")
    var invitation: String? = null,

    @Expose
    @SerializedName("share")
    var share: String? = null,
) : Parcelable {
}
