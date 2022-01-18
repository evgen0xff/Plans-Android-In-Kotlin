package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class SettingsModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("userId")
    var userId: String? = null,

    @Expose
    @SerializedName("pushNotifications")
    var pushNotifications: ArrayList<SettingOptionModel>? = null,

) : Parcelable {
}

@Parcelize
class SettingOptionModel (
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("name")
    var name: String? = null,

    @Expose
    @SerializedName("details")
    var details: String? = null,

    @Expose
    @SerializedName("key")
    var key: String? = null,

    @Expose
    @SerializedName("type")
    var type: String? = null,

    @Expose
    @SerializedName("status")
    var status: Boolean? = null
) : Parcelable


