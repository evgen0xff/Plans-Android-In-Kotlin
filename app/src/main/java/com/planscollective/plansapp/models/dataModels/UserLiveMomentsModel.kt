package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.extension.toArrayList
import kotlinx.parcelize.Parcelize

@Parcelize
class UserLiveMomentsModel(
    @Expose
    @SerializedName("_id")
    var userId : String? = null,

    @Expose
    @SerializedName("eventId")
    var eventId : String? = null,

    @Expose
    @SerializedName("user")
    var user : UserModel? = null,

    @Expose
    @SerializedName("liveMedia")
    var liveMoments : ArrayList<LiveMomentModel>? = null,

) : Parcelable {

    val timeLatest: Number?
        get(){
            return liveMoments?.firstOrNull()?.createdAt
        }

    val isAllSeen: Boolean
        get() {
            return liveMoments?.firstOrNull()?.isViewed ?: false
        }

    fun prepare() : UserLiveMomentsModel {
        liveMoments = liveMoments?.sortedWith(compareBy<LiveMomentModel>{ it.isViewed }.thenByDescending { it.createdAt?.toInt() })?.toArrayList()
        return this
    }

}