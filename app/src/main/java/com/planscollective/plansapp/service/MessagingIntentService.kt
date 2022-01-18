package com.planscollective.plansapp.service

import android.app.IntentService
import android.content.Intent
import androidx.core.app.RemoteInput
import com.planscollective.plansapp.helper.NotificationUtils
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.MessageModel
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.webServices.chat.ChatService
import java.util.*

class MessagingIntentService : IntentService("MessagingIntentService") {
    companion object {
        val TAG = "MessagingIntentService"
        val ACTION_REPLY = "com.planscollective.plansapp.service.action.REPLY"
        val EXTRA_REPLY = "com.planscollective.plansapp.service.extra.REPLY"
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            if (ACTION_REPLY == intent.action) {
                handleActionReply(getReplyText(intent), getMessage(intent))
            }
        }
    }

    private fun getMessage(intent: Intent?): NotificationActivityModel?{
        val bundle = intent?.extras?.takeIf { !it.isEmpty } ?: return null
        return bundle.getParcelable<NotificationActivityModel>("plans_notification")?.takeIf{ !it.notificationType.isNullOrEmpty() }
    }

    private fun getReplyText(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(EXTRA_REPLY)
    }

    /** Handles action for replying to messages from the notification.  */
    private fun handleActionReply(textReply: CharSequence?, message: NotificationActivityModel?) {
        if (textReply.isNullOrEmpty() || message == null) return
        val notificationId = message.groupNumberId ?: return
        var isConnect = false
        ChatService.initialize {
            if (!isConnect) {
                isConnect = true
                val msg = prepareMessage(textReply, message.chatId)
                ChatService.sendMessage(msg)
                NotificationUtils.cancelNotification(notificationId)
            }
        }
    }

    private fun prepareMessage(textReply: CharSequence?, idChat: String?): MessageModel? {
        val model = MessageModel().apply {
            chatId = idChat
            userId = UserInfo.userId
            sendingAt = Date().time / 1000.toLong()
            message = textReply.toString()
            type = MessageModel.MessageType.TEXT
        }
        return model
    }



}
