package com.planscollective.plansapp.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.DashboardActivity
import com.planscollective.plansapp.activity.MainActivity
import com.planscollective.plansapp.constants.AppInfo
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.service.MessagingIntentService
import java.util.*

object NotificationUtils {
    val notificationManager = ContextCompat.getSystemService(PLANS_APP, NotificationManager::class.java) as NotificationManager

    fun createNotificationChannel(message: NotificationActivityModel?): String? {
        return createNotificationChannel(AppInfo.CHANNEL_ID_PLANS, AppInfo.CHANNEL_NAME_PLANS, AppInfo.CHANNEL_DESCRIPTION_PLANS)
    }

    fun createNotificationChannel(channelId: String?, channelName: String?, channelDescription: String? = null): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                description = channelDescription
            }

            notificationManager.createNotificationChannel(notificationChannel)
        }
        return channelId
    }

    fun showNotification(message: NotificationActivityModel?, context: Context) {
        message?.timestamp = System.currentTimeMillis()

        saveChatMessageNotification(message)

        if (message?.isChatMessage == true) {
            showChatNotification(message, context)
        }else {
            showDefaultNotification(message, context)
        }
    }

    private fun showDefaultNotification(message: NotificationActivityModel?, context: Context) {

        val groupNumber = message?.groupNumberId ?: return
        val channelId = createNotificationChannel(message)?.takeIf { it.isNotEmpty() } ?: return
        val groupMessages = getOldNotifications(message.groupId)?.takeIf { it.isNotEmpty() } ?: return
        val notificationId = Random().nextInt(60000)

        // Create intent
        val type = if (PLANS_APP.currentActivity == null ) MainActivity::class.java else DashboardActivity::class.java
        val contentIntent = Intent(context, type).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("plans_notification", message)
        }

        // Create PendingIntent
        val flagPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            flagPendingIntent
        )

        // Single Notification
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(message.body)
            .setSummaryText(message.title)
            .setBigContentTitle(message.title)

        // Build a single notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_notification_logo)
            .setContentTitle(message.title)
            .setSubText(message.title)
            .setContentText(message.body)
            .setContentIntent(contentPendingIntent)
            .setStyle(bigTextStyle)
            .setColor(PlansColor.PURPLE_JOIN)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(Notification.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(message.groupId)
            .setAutoCancel(true)

        //Build the INBOX_STYLE.
        val inboxStyle = NotificationCompat.InboxStyle() // This title is slightly different than regular title, since I know INBOX_STYLE is available.
            .setBigContentTitle(message.title)
            .setSummaryText(message.title)

        // Add each summary line of the new emails, you can add up to 5.
        groupMessages.forEach {
            inboxStyle.addLine(it.body)
        }

        // Build the group notification
        val builderGroup = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_notification_logo)
            .setContentTitle(message.title)
            .setSubText(message.title)
            .setContentText(message.body)
            .setStyle(inboxStyle)
            .setColor(PlansColor.PURPLE_JOIN)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(Notification.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setGroup(message.groupId)
            .setGroupSummary(true)
            .setAutoCancel(true)


        ImageHelper.getAvatarBitmap(message.profileImage)?.also {
            builder.setLargeIcon(it)
            builderGroup.setLargeIcon(it)
        }

        notificationManager.notify(notificationId, builder.build())
        notificationManager.notify(groupNumber, builderGroup.build())

    }

    private fun saveChatMessageNotification(message: NotificationActivityModel?) {
        message?.apply {
            val list = UserInfo.listNotifications
            if (list.containsKey(groupId)){
                list[groupId]?.also {
                    it.add(this)
                    if (it.size > 5) {
                        it.removeAt(0)
                    }
                }
            }else {
                list[groupId] = arrayListOf(this)
            }
            UserInfo.listNotifications = list
        }
    }

    private fun getOldNotifications(groupId: String?): List<NotificationActivityModel>? {
        return groupId.takeIf { !it.isNullOrEmpty() }?.let{
            UserInfo.listNotifications[it]
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showChatNotification(message: NotificationActivityModel?, context: Context) {

        val channelId = createNotificationChannel(message)?.takeIf { it.isNotEmpty() } ?: return
        val notificationId = message?.groupNumberId ?: return
        val groupMessages = getOldNotifications(message.groupId)?.takeIf { it.isNotEmpty() } ?: return

        // Create intent
        val type = if (PLANS_APP.currentActivity == null ) MainActivity::class.java else DashboardActivity::class.java
        val contentIntent = Intent(context, type).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("plans_notification", message)
        }

        // Create PendingIntent
        val flagPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

            val contentPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    contentIntent,
                    flagPendingIntent
                )

        // Create MessageStyle
        val messageStyle = NotificationActivityModel.personMe?.let {
            NotificationCompat.MessagingStyle(it)
                .setGroupConversation(true)
                .setConversationTitle(message.title)
        }
        groupMessages.forEach {
            messageStyle?.addMessage(it.msgNotification)
        }

        // Create the RemoteInput specifying the key : EXTRA_REPLY.
        val replyLabel = ConstantTexts.LABEL_REPLY
        val remoteInput = RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
            .setLabel(replyLabel)
            .build()

        // Create Pending Intent for Reply Action
        val intent = Intent(context, MessagingIntentService::class.java).apply {
            action = MessagingIntentService.ACTION_REPLY
            putExtra("plans_notification", message)
        }
        val replyActionPendingIntent = PendingIntent.getService(
            context,
            notificationId,
            intent,
            flagPendingIntent)

        // Create Reply Action with RemoteInput and ReplyPendingIntent
        val replyAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_reply_white_18dp,
            replyLabel,
            replyActionPendingIntent
        )
        .addRemoteInput(remoteInput) // Informs system we aren't bringing up ur own custom UI for a reply
        .setShowsUserInterface(false) // Allows system to generate replies by context of conversation.
        .setAllowGeneratedReplies(true)
        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
        .build()

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_notification_logo)
            .setContentTitle(message.title)
            .setSubText(message.title)
            .setContentText(message.body)
            .setContentIntent(contentPendingIntent)
            .setStyle(messageStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(PlansColor.PURPLE_JOIN)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .addAction(replyAction)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            builder.setLargeIcon(ImageHelper.getAvatarBitmap(message.profileImage))
        }
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(id: Int? = null, isAll: Boolean = false) {
        if (isAll) {
            notificationManager.cancelAll()
        }else {
            id?.also {
                notificationManager.cancel(it)
            }
        }
    }

}





