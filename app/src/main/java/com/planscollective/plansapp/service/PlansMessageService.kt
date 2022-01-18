package com.planscollective.plansapp.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.extension.toObject
import com.planscollective.plansapp.helper.NotificationUtils
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel

class PlansMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        UserInfo.deviceToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.takeIf { it.isNotEmpty() }?.let {
            val message = remoteMessage.data.toObject<NotificationActivityModel>(NotificationActivityModel::class.java)
            handleMessage(message)
        }

        remoteMessage.notification?.also {
            println("remoteMessage.notification")
        }
    }


    private fun handleMessage(message: NotificationActivityModel?) {
        // 1. Show Notification
        showNotification(message)

        // 2. Process the notification
        processNotification(message)
    }

    private fun showNotification(message: NotificationActivityModel?) {
        if (message?.isSilent == true || !UserInfo.isLoggedIn) return

        NotificationUtils.showNotification(message, applicationContext)
    }

    private fun processNotification(message: NotificationActivityModel?) {
        var isBadge = true

        // 1. Actions according to Notification Types
        when(message?.notificationType) {
            "All guests left", "A guest lived" -> {
                PLANS_APP.getLivedEventsForEnding()
                isBadge = false
            }
            "Event Deleted", "Event Uninvited" -> {
                PLANS_APP.addDeletedEventIDs(message.eventId)
            }
            "Event Cancelled", "Event Expired" -> {
                PLANS_APP.addCancelExpiredEventIDs(message.eventId)
            }
            "Update Location" -> {
                PLANS_APP.updateLocation()
            }
            "Blocked User", "Deleted User" -> {
                PLANS_APP.addBlockedUserIDs(message.uid)
            }
        }

        // 2. Refresh Contents
        PLANS_APP.refreshCurrentScreen()

        // 3. Set Badge on the notification of bottom menu bar
        if (isBadge) {
            PLANS_APP.updateBadges()
        }
    }


}