package com.planscollective.plansapp.models.dataModels

import android.graphics.Color
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.constants.PlansColor
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class MessageShapeModel(
    @Expose
    @SerializedName("isHiddenProfileImage")
    var isHiddenProfileImage: Boolean = false,

    @Expose
    @SerializedName("isHiddenOwnerName")
    var isHiddenOwnerName: Boolean = false,

    @Expose
    @SerializedName("colorBackGround")
    var colorBackGround: Int = Color.WHITE,

    @Expose
    @SerializedName("colorMessage")
    var colorMessage: Int = Color.BLACK,

    @Expose
    @SerializedName("colorTime")
    var colorTime: Int = PlansColor.GRAY_LABEL,

    @Expose
    @SerializedName("cornersNonRounding")
    var cornersNonRounding: ArrayList<CornerType> = arrayListOf(),

 ) : Parcelable {
    enum class OwnerType {
        DATE,
        MINE,
        OTHER
    }

    enum class CornerType {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    enum class PositionType {
        NORMAL,
        START,
        MEDIUM,
        END
    }

    @IgnoredOnParcel
    var ownerType: OwnerType = OwnerType.OTHER
        set(value) {
            field = value
            colorBackGround = if (value == OwnerType.MINE) PlansColor.TEAL_MAIN else Color.WHITE
            colorMessage = if (value == OwnerType.MINE) Color.WHITE else Color.BLACK
        }

    @IgnoredOnParcel
    var positionType: PositionType = PositionType.NORMAL
        set(value) {
            field = value
            isHiddenProfileImage = true
            isHiddenOwnerName = true
            cornersNonRounding.clear()
            when(value) {
                PositionType.NORMAL -> {
                    if (ownerType == OwnerType.OTHER) {
                        isHiddenProfileImage = false
                        isHiddenOwnerName = false
                    }
                }
                PositionType.START -> {
                    if (ownerType == OwnerType.MINE) {
                        cornersNonRounding.add(CornerType.BOTTOM_RIGHT)
                    }else if (ownerType == OwnerType.OTHER) {
                        isHiddenOwnerName = false
                        cornersNonRounding.add(CornerType.BOTTOM_LEFT)
                    }
                }
                PositionType.MEDIUM -> {
                    if (ownerType == OwnerType.MINE) {
                        cornersNonRounding.addAll(arrayOf(CornerType.TOP_RIGHT, CornerType.BOTTOM_RIGHT))
                    }else if (ownerType == OwnerType.OTHER) {
                        cornersNonRounding.addAll(arrayOf(CornerType.TOP_LEFT, CornerType.BOTTOM_LEFT))
                    }
                }
                PositionType.END -> {
                    if (ownerType == OwnerType.MINE) {
                        cornersNonRounding.add(CornerType.TOP_RIGHT)
                    }else if (ownerType == OwnerType.OTHER) {
                        isHiddenProfileImage = false
                        cornersNonRounding.add(CornerType.TOP_LEFT)
                    }
                }

            }
        }



}