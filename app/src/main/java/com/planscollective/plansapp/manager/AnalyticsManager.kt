package com.planscollective.plansapp.manager

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {

    enum class EventType (var value: String) {
        APP_OPEN("app_open"),
        SIGN_UP("sign_up"),
        LOGIN("login"),
        LOGOUT("logout"),
        CREATE_EVENT("create_event"),
        JOIN_EVENT_PUBLIC("join_event_public"),
        JOIN_EVENT_PRIVATE("join_event_private"),
        INVITATIONS_EMAIL("invitations_email"),
        INVITATIONS_SMS("invitations_sms"),
        INVITATIONS_LINK("invitations_link"),
        LIVE_USER("live_user"),
        CHAT_MESSAGE("chat_message"),
        FRIEND_ADD("friend_add"),
        FRIEND_REQUEST("friend_request"),
        STORY_ADD("story_add"),
        POST_ADD("post_add"),
        INVITE_LINK("invite_link"),
        SCREEN_VIEW("screen_view"),
    }

    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics = Firebase.crashlytics

    fun setUserId(userId: String?) {
        val id = userId.takeIf { !it.isNullOrEmpty() } ?: UserInfo.deviceToken
        analytics.setUserId(id)

        id.takeIf { !it.isNullOrEmpty() }?.also{
            crashlytics.setUserId(it)
        }
    }

    fun logEvent(typeEvent: EventType? = null,
        itemID: String? = null,
        itemName: String? = null,
        content: String? = null
    ) {
        val type = typeEvent ?: return

        print("AnalyticsManager logEvent: ${type.value}")

        analytics.logEvent(type.value){
            itemID.takeIf { !it.isNullOrEmpty() }?.also {
                param(FirebaseAnalytics.Param.ITEM_ID, it)
            }
            itemName.takeIf { !it.isNullOrEmpty() }?.also {
                param(FirebaseAnalytics.Param.ITEM_NAME, it)
            }
            content.takeIf { !it.isNullOrEmpty() }?.also {
                param(FirebaseAnalytics.Param.CONTENT, it)
            }
        }
    }

    fun logScreenView(screenName: String?, className: String? = null) {
        screenName?.takeIf { it.isNotEmpty() }?.also {
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, it)
                className.takeIf { !it.isNullOrEmpty() }?.also {
                    param(FirebaseAnalytics.Param.SCREEN_CLASS, it)
                }
            }
        }
    }


}