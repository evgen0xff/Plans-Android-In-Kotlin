package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.parcelize.Parcelize

@Parcelize
class TypingUserModel(
    @Expose
    @SerializedName("userName")
    var userName: String? = null,

    @Expose
    @SerializedName("chatId")
    var chatId: String? = null,

    @Expose
    @SerializedName("userId")
    var userId: String? = null,

    @Expose
    @SerializedName("profileImage")
    var profileImage: String? = null,

    @Expose
    @SerializedName("isTyping")
    var isTyping: Boolean = false,
) : Parcelable {
}

@Parcelize
class TypingModel(
    @Expose
    @SerializedName("chatId")
    var chatId: String? = null,

    @Expose
    @SerializedName("typingUsers")
    var typingUsers: ArrayList<TypingUserModel> = arrayListOf(),
) : Parcelable {

    val lastTypingUser: TypingUserModel?
        get() = typingUsers.lastOrNull()

    val isTyping: Boolean
        get() = typingUsers.any { it.isTyping }

    val textTyping: String
        get() {
            return when(typingUsers.size) {
                0 -> ""
                1 -> "${typingUsers[0].userName ?: ""} is typing..."
                2 -> "${typingUsers[0].userName ?: ""} and ${typingUsers[1].userName ?: ""} are typing..."
                3 -> "${typingUsers[0].userName ?: ""}, ${typingUsers[1].userName ?: ""} and ${typingUsers[2].userName ?: ""} are typing..."
                else -> "Several people are typing..."
            }
        }

    fun updateTyping(typingNew: TypingUserModel?, listBlockedUsers: ArrayList<UserModel>) : Boolean {
        var isChanged = false
        val new = typingNew?.takeIf { it.userId != UserInfo.userId && !listBlockedUsers.any { blocked -> blocked.id == it.userId } } ?: return isChanged

        typingUsers.removeAll { listBlockedUsers.any { blocked -> blocked.id == it.userId } }

        chatId = new.chatId
        typingUsers.indexOfFirst { it.userId == new.userId }.takeIf { it >= 0 }?.also {
            if (!new.isTyping) {
                typingUsers.removeAt(it)
                isChanged = true
            }
        } ?: run {
            if (new.isTyping) {
                typingUsers.add(new)
                isChanged = true
            }
        }

        return isChanged
    }

}

