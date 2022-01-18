package com.planscollective.plansapp.manager

import android.location.Address
import android.net.Uri
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.PrefHelper
import com.planscollective.plansapp.models.dataModels.MessageModel
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.dataModels.UserModel

object UserInfo {

    var userId : String?
        get() = PrefHelper.readPrefString(Keys.USER_ID)
        set(value) = PrefHelper.savePrefString(Keys.USER_ID, value)

    var mobile : String?
        get() = PrefHelper.readPrefString(Keys.MOBILE)
        set(value) {
            PrefHelper.savePrefString(Keys.MOBILE, value)
        }
    var email : String?
        get() = PrefHelper.readPrefString(Keys.EMAIL)
        set(value) {
            PrefHelper.savePrefString(Keys.EMAIL, value)
        }
    var firstName : String?
        get() = PrefHelper.readPrefString(Keys.FIRST_NAME)
        set(value) {
            PrefHelper.savePrefString(Keys.FIRST_NAME, value)
        }
    var lastName : String?
        get() = PrefHelper.readPrefString(Keys.LAST_NAME)
        set(value) {
            PrefHelper.savePrefString(Keys.LAST_NAME, value)
        }
    var fullName : String?
        get() {
            var name = PrefHelper.readPrefString(Keys.FULL_NAME)
            if (name.isNullOrEmpty()) {
                name = firstName?.takeIf { it.isNotEmpty() }?.let {
                    if (!lastName.isNullOrEmpty()) "$it $lastName" else it
                }
            }
            return name
        }
        set(value) = PrefHelper.savePrefString(Keys.FULL_NAME, value)

    var profileUrl : String?
        get() = PrefHelper.readPrefString(Keys.PROFILE_URL)
        set(value) {
            PrefHelper.savePrefString(Keys.PROFILE_URL, value)
        }
    var accessToken : String?
        get() = PrefHelper.readPrefString(Keys.ACCESS_TOKEN)
        set(value) {
            PrefHelper.savePrefString(Keys.ACCESS_TOKEN, value)
        }
    var deviceToken : String?
        get() = PrefHelper.readPrefString(Keys.DEVICE_TOKEN)
        set(value) {
            PrefHelper.savePrefString(Keys.DEVICE_TOKEN, value)
        }
    var isLoggedIn : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_LOGGED_IN)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_LOGGED_IN, value)
        }

    var coinNumber : Int
        get() = PrefHelper.readPrefInt(Keys.COIN_NUMBER)
        set(value) {
            PrefHelper.savePrefInt(Keys.COIN_NUMBER, value)
        }

    var countUnviewedNotify : Int
        get() = PrefHelper.readPrefInt(Keys.COUNT_UNVIEWED_NOTIFY)
        set(value) {
            PrefHelper.savePrefInt(Keys.COUNT_UNVIEWED_NOTIFY, value)
        }

    var countUnviewedChatMsgs : Int
        get() = PrefHelper.readPrefInt(Keys.COUNT_UNVIEWED_CHAT_MSGS)
        set(value) {
            PrefHelper.savePrefInt(Keys.COUNT_UNVIEWED_CHAT_MSGS, value)
        }

    var lastViewTimeForNotify : Long
        get() = PrefHelper.readPrefLong(Keys.LAST_VIEW_TIME_NOTIFY)
        set(value) {
            PrefHelper.savePrefLong(Keys.LAST_VIEW_TIME_NOTIFY, value)
        }
    var isSeenYouLiveAt : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_YOU_LIVE_AT)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_SEEN_YOU_LIVE_AT, value)
        }
    var isSeenGuideWelcome : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_WELCOME)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_WELCOME, value)
        }
    var isSeenGuideWatchMoment : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_WATCH_MOMENT)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_WATCH_MOMENT, value)
        }
    var isSeenGuideGuestList : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_GUEST_LIST)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_GUEST_LIST, value)
        }

    var isSeenGuideFriends : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_FRIENDS)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_FRIENDS, value)

    var isSeenGuidePosts : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_POSTS)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_POSTS, value)

    var isSeenGuideChatWithEventGuests : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_CHAT_EVENT_GUESTS)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_CHAT_EVENT_GUESTS, value)

    var isSeenGuideAddEventPostNow : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_ADD_EVENT_POSTS_NOW)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_ADD_EVENT_POSTS_NOW, value)

    var isSeenGuideTapHoldNotification : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_TAP_HOLD_NOTIFICATION)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_TAP_HOLD_NOTIFICATION, value)

    var isSeenGuideTapHoldChat : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_TAP_HOLD_CHAT)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_TAP_HOLD_CHAT, value)

    var isSeenGuideTapViewEvent : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_TAP_VIEW_EVENT)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_TAP_VIEW_EVENT, value)

    var isSeenGuideLocationDiscovery : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_SEEN_GUIDE_LOCATION_DISCOVERY)
        set(value) = PrefHelper.savePrefBoolean(Keys.IS_SEEN_GUIDE_LOCATION_DISCOVERY, value)

    var enableLocationUpdates : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.ENABLE_LOCATION_UPDATES)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.ENABLE_LOCATION_UPDATES, value)
        }

    var isPrivateAccount : Boolean
        get() = PrefHelper.readPrefBoolean(Keys.IS_PRIVATE_ACCOUNT, true)
        set(value) {
            PrefHelper.savePrefBoolean(Keys.IS_PRIVATE_ACCOUNT, value)
        }

    var latitude : Double
        get() = PrefHelper.readPrefDouble(Keys.LOCATION_ME_LAT)
        set(value) {
            PrefHelper.savePrefDouble(Keys.LOCATION_ME_LAT, value)
        }

    var longitude : Double
        get() = PrefHelper.readPrefDouble(Keys.LOCATION_ME_LONG)
        set(value) {
            PrefHelper.savePrefDouble(Keys.LOCATION_ME_LONG, value)
        }

    var userLocationName: String?
        get() = PrefHelper.readPrefString(Keys.USER_LOCATION_NAME)
        set(value) = PrefHelper.savePrefString(Keys.USER_LOCATION_NAME, value)

    var userAddressLocality: String?
        get() = PrefHelper.readPrefString(Keys.USER_ADDRESS_LOCALITY)
        set(value) = PrefHelper.savePrefString(Keys.USER_ADDRESS_LOCALITY, value)

    var userAddressAdminArea: String?
        get() = PrefHelper.readPrefString(Keys.USER_ADDRESS_ADMIN_AREA)
        set(value) = PrefHelper.savePrefString(Keys.USER_ADDRESS_ADMIN_AREA, value)

    var userAddress: String?
        get() = PrefHelper.readPrefString(Keys.USER_ADDRESS)
        set(value) = PrefHelper.savePrefString(Keys.USER_ADDRESS, value)

    var countryOwn: String?
        get() = PrefHelper.readPrefString(Keys.COUNTRY_OWN)
        set(value) = PrefHelper.savePrefString(Keys.COUNTRY_OWN, value)

    var address: Address?
        get() {
            return PrefHelper.readPrefObject<Address>(Keys.LOCATION_ME_ADDRESS)?.apply {
                if (!hasLatitude()) {
                    latitude = this@UserInfo.latitude
                }
                if (!hasLongitude()) {
                    longitude = this@UserInfo.longitude
                }
                if (featureName.isNullOrEmpty()) {
                    featureName = this@UserInfo.userLocationName
                }
                if (maxAddressLineIndex < 0 && !userAddress.isNullOrEmpty()) {
                    setAddressLine(0, userAddress)
                }
            }
        }
        set(value) = PrefHelper.savePrefObject(Keys.LOCATION_ME_ADDRESS, value)

    val userPlace : PlaceModel
        get() {
            return PlaceModel(
                latitude,
                longitude,
                userLocationName,
                userAddress
            )
        }

    var mobileList: ArrayList<String>
        get() = PrefHelper.readPrefString(Keys.MOBILE_LIST)?.split(",")?.toArrayList() ?: ArrayList()
        set(value) {
            val joined = if (value.isEmpty()) null else {
                value.joinToString (",")
            }
            PrefHelper.savePrefString(Keys.MOBILE_LIST, joined)
        }

    var emailList: ArrayList<String>
        get() = PrefHelper.readPrefString(Keys.EMAIL_LIST)?.split(",")?.toArrayList() ?: ArrayList()
        set(value) {
            val joined = if (value.isEmpty()) null else {
                value.joinToString (",")
            }
            PrefHelper.savePrefString(Keys.EMAIL_LIST, joined)
        }

    var chatMessagesUnsent: HashMap<String, ArrayList<MessageModel>>
        get() =  PrefHelper.readPrefObject<HashMap<String, ArrayList<MessageModel>>>(Keys.CHAT_MESSAGES_UNSENT) ?: HashMap()
        set(value) = PrefHelper.savePrefObject(Keys.CHAT_MESSAGES_UNSENT, value)

    var listShareContents: ArrayList<HashMap<String, String?>>
        get() = PrefHelper.readPrefObject(Keys.LIST_SHARE_CONTENTS, arrayListOf()) ?: arrayListOf()
        set(value) = PrefHelper.savePrefObject(Keys.LIST_SHARE_CONTENTS, value)

    var listNotifications: HashMap<String, ArrayList<NotificationActivityModel>>
        get() =  PrefHelper.readPrefObject<HashMap<String, ArrayList<NotificationActivityModel>>>(Keys.LIST_CHAT_MESSAGE_NOTIFICATIONS) ?: HashMap()
        set(value) = PrefHelper.savePrefObject(Keys.LIST_CHAT_MESSAGE_NOTIFICATIONS, value)

    var listAllEventAttendIDs: ArrayList<String>
        get() = PrefHelper.readPrefString(Keys.LIST_ID_ALL_EVENTS_ATTENDED)?.split(",")?.toArrayList() ?: ArrayList()
        set(value) {
            val joined = if (value.isEmpty()) null else {
                value.joinToString (",")
            }
            PrefHelper.savePrefString(Keys.LIST_ID_ALL_EVENTS_ATTENDED, joined)
        }

    val isClickedByAppLink: Boolean
        get() = containsShareContents(hashMapOf("type" to "app_share")) != null


    fun initializeForLaunch () {
        isSeenYouLiveAt = false
        AnalyticsManager.setUserId(userId)
        AnalyticsManager.logEvent(AnalyticsManager.EventType.APP_OPEN)
    }

    fun initForLogin(userModel: UserModel?) {
        isLoggedIn = true
        updateUserInfo(userModel)
    }

    fun updateUserInfo (user: UserModel?) {
        userId = user?.id
        mobile = user?.mobile
        email =  user?.email
        firstName = user?.firstName
        lastName = user?.lastName
        profileUrl = user?.profileImage
        accessToken = user?.accessToken
        lastViewTimeForNotify = user?.lastViewTimeForNotify?.toLong() ?: 0.toLong()
        AnalyticsManager.setUserId(userId)
    }

    fun updateUserInfoForEditProfile(user: UserModel?) {
        user?.firstName?.also {
            firstName = it
        }
        user?.lastName?.also {
            lastName = it
        }
        user?.profileImage?.also {
            profileUrl = it
        }
    }

    fun initializeUserInfo() {
        isLoggedIn = false
        isSeenYouLiveAt = false
        accessToken = null
        chatMessagesUnsent = HashMap()
        listShareContents = arrayListOf()
        listNotifications = hashMapOf()
        countUnviewedNotify = 0
        countUnviewedChatMsgs = 0

        updateUserInfo(null)
    }

    fun addShareContent(uri: Uri?) : HashMap<String, String?>? {
        var result : HashMap<String, String?>? = null

        if (uri == null) return result

        println("UserInfo addShareContent - $uri")

        val mapContents = HashMap<String, String?>()

        if (uri.queryParameterNames.contains("type")) {
            uri.queryParameterNames.forEach {
                mapContents[it] = uri.getQueryParameter(it)
            }
        }else if (uri.queryParameterNames.contains("deep_link_id")) {
            val linkDeep = Uri.parse(uri.getQueryParameter("deep_link_id"))
            linkDeep.queryParameterNames.forEach {
                mapContents[it] = linkDeep.getQueryParameter(it)
            }
        }

        if (mapContents.isNullOrEmpty()) return result

        val list = listShareContents
        if (!list.any { item ->
                var isEqual = true
                mapContents.forEach { entry ->
                    if (item[entry.key] != entry.value) {
                        isEqual = false
                    }
                }
                isEqual
        }) {
            list.add(mapContents)
            listShareContents = list
            result = mapContents
        }

        return result
    }

    fun removeShareContent(content: HashMap<String, String?>?) {
        if (content == null) return

        val list = listShareContents
        containsShareContents(content)?.also {
            list.removeAt(it)
            listShareContents = list
        }
    }

    fun containsShareContents(content: HashMap<String, String?>?): Int? {
        return content?.takeIf{ it.isNotEmpty()}?.let {
            listShareContents.indexOfFirst { item ->
                var isEqual = true
                it.forEach { entry ->
                    if (item[entry.key] != entry.value) {
                        isEqual = false
                    }
                }
                isEqual
            }
        }?.takeIf{ it >= 0 }
    }


}