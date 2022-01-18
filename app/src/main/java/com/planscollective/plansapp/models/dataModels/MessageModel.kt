package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageModel(
    @Expose
    @SerializedName("_id")
    var id:     String? = null,

    @Expose
    @SerializedName("chatId")
    var chatId:     String? = null,

    @Expose
    @SerializedName("userId")
    var userId:     String? = null,

    @Expose
    @SerializedName("message")
    var message:    String? = null,

    @Expose
    @SerializedName("imageUrl")
    var imageUrl:   String? = null,

    @Expose
    @SerializedName("videoFile")
    var videoFile:  String? = null,

    @Expose
    @SerializedName("createdAt")
    var createdAt:  Number? = null,

    @Expose
    @SerializedName("sendingAt")
    var sendingAt:  Number? = null,

    @SerializedName("user")
    var user:       UserModel? = null,

    @Expose
    @SerializedName("type")
    private var _type:       String? = null,

) : Parcelable {

    enum class MessageType(var value: String){
        TEXT("text"),
        IMAGE("image"),
        VIDEO("video"),
        DATE("date")
    }

    @IgnoredOnParcel
    @SerializedName("type_main")
    var type: MessageType? = null
        get() {
            return _type?.takeIf { it.isNotEmpty() }?.let { value ->
                MessageType.values().firstOrNull { it.value == value }
            }
        }
        set(value) {
            field = value
            _type = value?.value
        }

    @IgnoredOnParcel
    @SerializedName("shapeModel")
    var shapeModel: MessageShapeModel? = null

}