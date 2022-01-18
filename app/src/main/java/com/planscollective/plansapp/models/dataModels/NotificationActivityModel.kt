package com.planscollective.plansapp.models.dataModels

import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.constants.AppInfo
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.ImageHelper
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.parcelize.Parcelize

@Parcelize
class NotificationActivityModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("userId")
    var userId: String? = null,      // Receiver Id

    @Expose
    @SerializedName("uid")
    var uid: String? = null,         // Sender Id

    @Expose
    @SerializedName("eventId")
    var eventId: String? = null,

    @Expose
    @SerializedName("postId")
    var postId: String? = null,

    @Expose
    @SerializedName("chatId")
    var chatId: String? = null,

    @Expose
    @SerializedName("liveMomentId")
    var liveMomentId: String? = null,

    @Expose
    @SerializedName("message")
    var message: String? = null,

    @Expose
    @SerializedName("attributedMsg")
    var attributedMsg: String? = null,

    @Expose
    @SerializedName("profileImage")
    var profileImage: String? = null,

    @Expose
    @SerializedName("firstName")
    var firstName: String? = null,

    @Expose
    @SerializedName("lastName")
    var lastName: String? = null,

    @Expose
    @SerializedName("image")
    var image: String? = null,

    @Expose
    @SerializedName("notificationType")
    var notificationType: String? = null,

    @Expose
    @SerializedName("eventName")
    var eventName: String? = null,

    @Expose
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @Expose
    @SerializedName("userImage")
    var userImage: String? = null,

    @Expose
    @SerializedName("userImage2")
    var userImage2: String? = null,

    @Expose
    @SerializedName("title")
    var title: String? = null,

    @Expose
    @SerializedName("body")
    var body: String? = null,

    @Expose
    @SerializedName("isActive")
    var isActive: Boolean? = null,

    @Expose
    @SerializedName("isSilent")
    var isSilent: Boolean? = null,

    @Expose
    @SerializedName("isLive")
    var isLive: Int? = null,

    @Expose
    @SerializedName("createdAtTimestamp")
    var createdAtTimestamp: Number? = null,

    @Expose
    @SerializedName("event")
    var event: EventModel? = null,

    @Expose
    @SerializedName("timestamp")
    var timestamp: Long? = null,

) : Parcelable {

    val isNew: Boolean
        get() {
            return (createdAtTimestamp ?: 0).toLong() > UserInfo.lastViewTimeForNotify
        }

    val groupId: String
        get() {
            return chatId?.takeIf { it.isNotEmpty() } ?: eventId ?.takeIf { it.isNotEmpty() } ?: AppInfo.CHANNEL_ID_PLANS
        }

    val groupName: String
        get() {
            return title?.takeIf { it.isNotEmpty() } ?: AppInfo.CHANNEL_NAME_PLANS
        }

    val groupDescription: String
        get() {
            return title?.takeIf { it.isNotEmpty() } ?: AppInfo.CHANNEL_NAME_PLANS
        }

    val isChatMessage: Boolean
        get() {
            return !chatId.isNullOrEmpty()
        }

    val userName: String?
        get() {
            return body?.split(": ")?.takeIf { it.size > 1 }?.firstOrNull()?.takeIf { it.isNotEmpty() } ?: title
        }

    val msgText: String?
        get() {
            return body?.split(": ")?.takeIf { it.size > 1 }?.drop(1)?.joinToString(": ")?.takeIf { it.isNotEmpty() } ?: body
        }

    val personUser: Person?
        get() {
            return userName.takeIf { !it.isNullOrEmpty() }?.let {
                Person.Builder()
                    .setName(userName)
                    .setIcon(IconCompat.createWithBitmap(ImageHelper.getAvatarBitmap(profileImage)))
                    .build()
            }
        }

    val msgNotification: Message?
        get() {
            return if (!msgText.isNullOrEmpty() && timestamp != null && personUser != null) Message(msgText, timestamp!!, personUser) else null
        }

    val groupNumberId: Int?
        get() {
            return groupId.takeIf { it.length > 18 }?.drop(18)?.toIntOrNull(16)
        }


    constructor(bundle: Bundle?) : this() {
        title = bundle?.getString("title")
        body = bundle?.getString("body")
        notificationType = bundle?.getString("notificationType")
        eventId = bundle?.getString("eventId")
        postId = bundle?.getString("postId")
        liveMomentId = bundle?.getString("liveMomentId")
        uid = bundle?.getString("uid")
        userId = bundle?.getString("userId")
        chatId = bundle?.getString("chatId")
        isSilent = bundle?.getString("isSilent")?.takeIf{ it == "true"}?.let { true } ?: false
        profileImage = bundle?.getString("profileImage")
        image = bundle?.getString("image")

    }

    fun getSpannableText(messageAttributed: String? = null, messageOriginal: String? = null, withAgo: Boolean = true, breakLine: Boolean = false) : SpannableStringBuilder? {
        var spannableBuilder: SpannableStringBuilder? = null
        var msgAttributed = messageAttributed ?: attributedMsg
        val msgOriginal = messageOriginal ?: message

        if (!msgAttributed.isNullOrEmpty()) {
            if (notificationType == "Event Reminder" && msgAttributed.contains("tomorrow") && event?.startTime != null) {
                msgAttributed = msgAttributed.dropLast(1)
                msgAttributed += " at " + event!!.startTime!!.toLocalDateTime().toFormatString("h:mm a") + "."
            }

            msgAttributed = msgAttributed?.replace("<bold>", "<b>")?.replace("</bold>", "</b>")
            val spanned = Html.fromHtml(msgAttributed, Html.FROM_HTML_MODE_LEGACY)

            spannableBuilder = SpannableStringBuilder(spanned)
        }else if (!msgOriginal.isNullOrEmpty()) {
            spannableBuilder = SpannableStringBuilder(msgOriginal)
        }

        if (withAgo && createdAtTimestamp != null && spannableBuilder != null) {
            createdAtTimestamp?.toLocalDateTime()?.timeAgoSince()?.also {
                val start = spannableBuilder.length
                val strTime = (if (breakLine) "\n" else " ") + it
                spannableBuilder.append(strTime, AbsoluteSizeSpan(13, true), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableBuilder.setSpan(ForegroundColorSpan(PlansColor.GRAY_SECOND), start, spannableBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return spannableBuilder
    }

    fun getSpannableText(widthMax: Int, textView: TextView?) : SpannableStringBuilder? {
        var spannableBuilder = getSpannableText()

        when(notificationType) {
            "Comment Like", "Like", "Comment" -> {
                textView?.maxLines = 3
                val max = widthMax * 3
                textView?.also { txtView ->
                    var width = txtView.getEditableTextWidth(spannableBuilder) + 75.toPx()
                    var lengthDropped = 0
                    while (width > max) {
                        lengthDropped += 3
                        val msgAttributed = attributedMsg?.dropLast(lengthDropped)?.takeIf { it.isNotEmpty() }?.let { "$it..." }
                        val msgOriginal = message?.dropLast(lengthDropped)?.takeIf { it.isNotEmpty() }?.let { "$it..." }
                        spannableBuilder = getSpannableText(msgAttributed, msgOriginal)
                        width = txtView.getEditableTextWidth(spannableBuilder) + 75.toPx()
                    }
                }
            }
            else -> {
                textView?.maxLines = Integer.MAX_VALUE
            }
        }

        return spannableBuilder
    }

    fun getMessages(): List<Message> {
        val list = ArrayList<Message>()

        return list
    }

    companion object{
        val personMe: Person?
            get() {
                return UserInfo.fullName.takeIf { !it.isNullOrEmpty() }?.let {
                    Person.Builder()
                        .setName(it)
                        .setIcon(IconCompat.createWithBitmap(ImageHelper.getAvatarBitmap(UserInfo.profileUrl)))
                        .build()
                }
            }
    }

}