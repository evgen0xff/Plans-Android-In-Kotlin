package com.planscollective.plansapp.models.dataModels

import android.graphics.Typeface
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.parcelize.Parcelize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

@Parcelize
open class EventModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("eventId")
    var eventId: String? = null,

    @Expose
    @SerializedName("eventName", alternate = ["eventsName"])
    var eventName: String? = null,

    @Expose
    @SerializedName("address")
    var address: String? = null,

    @Expose
    @SerializedName("caption")
    var caption: String? = null,

    @Expose
    @SerializedName("details")
    var details: String? = null,

    @Expose
    @SerializedName("userId")
    var userId: String? = null,

    @Expose
    @SerializedName("imageOrVideo")
    var imageOrVideo: String? = null,

    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,

    @Expose
    @SerializedName("mediaType")
    var mediaType: String? = null,

    @Expose
    @SerializedName("locationName")
    var locationName: String? = null,

    @Expose
    @SerializedName("chatId")
    var chatId: String? = null,

    @Expose
    @SerializedName("startDate")
    var startDate: Number? = null,

    @Expose
    @SerializedName("startTime")
    var startTime: Number? = null,

    @Expose
    @SerializedName("endDate")
    var endDate: Number? = null,

    @Expose
    @SerializedName("endTime")
    var endTime: Number? = null,

    @Expose
    @SerializedName("startedTime")
    var startedTime: Number? = null,

    @Expose
    @SerializedName("createdAt")
    var createdAt: Number? = null,

    @Expose
    @SerializedName("isEndedTime")
    var endedTime: Number? = null,

    @Expose
    @SerializedName("liveTime")
    var liveTime: Number? = null,

    @Expose
    @SerializedName("long")
    var long: Number? = null,

    @Expose
    @SerializedName("lat")
    var lat: Number? = null,

    @Expose
    @SerializedName("isActive")
    var isActive: Boolean? = null,

    @Expose
    @SerializedName("isCancel")
    var isCancel: Boolean? = null,

    @Expose
    @SerializedName("isPublic")
    var isPublic: Boolean? = null,

    @Expose
    @SerializedName("isJoin")
    var isJoin: Boolean? = null,

    @Expose
    @SerializedName("isExpired")
    var isExpired: Boolean? = null,

    @Expose
    @SerializedName("isViewed")
    var isViewed: Boolean? = null,

    @Expose
    @SerializedName("isChatEnd")
    var isChatEnd: Boolean? = null,

    @Expose
    @SerializedName("isHide")
    var isHide: Boolean? = null,

    @Expose
    @SerializedName("isSaved")
    var isSaved: Boolean? = null,

    @Expose
    @SerializedName("turnOffNoti")
    var turnOffNoti: Boolean? = null,

    @Expose
    @SerializedName("isGroupChatOn")
    var isGroupChatOn: Boolean? = null,

    @Expose
    @SerializedName("isPosting")
    var isPosting: Boolean? = null,

    @Expose
    @SerializedName("isLive")
    var isLive: Int? = null,

    @Expose
    @SerializedName("isInvite")
    var isInvite: Int? = null,

    @Expose
    @SerializedName("isHostLive")
    var isHostLive: Int? = null,

    @Expose
    @SerializedName("isEnded")
    var isEnded: Int? = null,

    @Expose
    @SerializedName("didLeave")
    var didLeave: Int? = null,

    @Expose
    @SerializedName("width")
    var width: Int? = null,

    @Expose
    @SerializedName("height")
    var height: Int? = null,

    @Expose
    @SerializedName("viewedCounts")
    var countViews: Int? = null,

    @Expose
    @SerializedName("postCounts")
    var countPosts: Int? = null,

    @Expose
    @SerializedName("invitationCount")
    var countInvitations: Int? = null,

    @Expose
    @SerializedName("liveMommentsCount")
    var countLiveMoments: Int? = null,

    @Expose
    @SerializedName("joinStatus")
    var statusJoin: Int? = null,

    @Expose
    @SerializedName("checkInRange")
    var checkInRange: Int? = null,

    @Expose
    @SerializedName("eventCreatedBy")
    var eventCreatedBy: UserModel? = null,

    @Expose
    @SerializedName("invitations")
    var invitations: ArrayList<InvitationModel>? = null,

    @Expose
    @SerializedName("invitedPeople")
    var invitedPeople: ArrayList<InvitationModel>? = null,

    @Expose
    @SerializedName("posts")
    var posts: ArrayList<PostModel>? = null,

    @Expose
    @SerializedName("chats")
    var chatInfo: ChatModel? = null,

    @Expose
    @SerializedName("liveMomments")
    var liveMoments: ArrayList<UserLiveMomentsModel>? = null,

    @Expose
    @SerializedName("eventLink")
    var eventLink: EventLink? = null,

    @Expose
    @SerializedName("friendsContactNumbers")
    var friendsContactNumbers: String? = null,

) : Parcelable {

    enum class EventStatus {
        WAITING,
        RUNNING_LATE,
        LIVED,
        ENDED,
        EXPIRED,
        CANCELLED,
    }

    val urlCoverImage: String?
        get() {
            return if (mediaType == "video") thumbnail else imageOrVideo
        }

    val eventStatus: EventStatus
        get() {
            var result = EventStatus.WAITING
            val now = Date().time.toSeconds()
            if (isActive == false || isCancel == true) {
                result = EventStatus.CANCELLED
            }else if (isEnded == 1) {
                result = EventStatus.ENDED
            }else if (isLive == 1) {
                result = EventStatus.LIVED
            }else if (isExpired == true || (endTime != null && endTime!!.toLong() < now && ( startedTime == null || startedTime!!.toLong() == 0.toLong()))){
                result = EventStatus.EXPIRED
            }else if (startTime != null && startTime!!.toLong() < (now - 60)){
                result = EventStatus.RUNNING_LATE
            }
            return result
        }

    fun prepare() : EventModel {

        // Live status for Host and Guests
        if (isLive == 1) {
            // Host
            if (isHostLive == 1 && liveTime != null) {
                var hrs = 3
                if (startedTime != null && endTime != null && (endTime!!.toLong() - startedTime!!.toLong()) > (3600 * 24)) {
                    hrs = 7
                }
                val timeout = (Date().time / 1000) - (3600 * hrs)
                if (liveTime!!.toLong() < timeout) {
                    isHostLive = 0
                }
            }

            // Guests
            invitations?.forEach { guest ->
                if (guest.isLive == 1 && guest.locationArrivedTime != null) {
                    var hrs = 3
                    if (startedTime != null && endTime != null && (endTime!!.toLong() - startedTime!!.toLong()) > (3600 * 24)) {
                        hrs = 7
                    }
                    val timeout = (Date().time / 1000) - (3600 * hrs)
                    if (guest.locationArrivedTime!!.toLong() < timeout) {
                        guest.isLive = 0
                    }
                }
            }
        }else {
            isHostLive = 0
            invitations?.forEach { it.isLive = 0 }
        }

        // Sort for Invitation by Live status
        invitations?.sortByDescending { it.isLive ?: 0 }

        // Prepare Live Moments
        liveMoments?.forEach{it.prepare()}
        liveMoments =  liveMoments?.sortedWith(compareByDescending<UserLiveMomentsModel> { it.user?.id == UserInfo.userId }
            .thenBy { it.isAllSeen }
            .thenByDescending { it.timeLatest?.toInt() })?.toArrayList()

        // Prepare Posts
        posts?.forEach{it.prepare()}


        return this
    }

    fun getFriendsCount() : Int {
        var result = 0
        if (eventCreatedBy?.isFriend == true) {
            result ++
        }

        invitations?.forEach{
            if (it.isFriend == true && (it.status == 2 || it.status == 3)){
                result ++
            }
        }
        return result
    }

    fun getEventStatus() : Triple<String, Int, EventStatus> {
        var title: String = ""
        var color: Int = PlansColor.BlUE_TEXT

        when(eventStatus) {
            EventStatus.WAITING -> {
                title = Date((startTime ?: 0).toMilliseconds()).getStartedString(false)
                color = PlansColor.BlUE_TEXT
            }
            EventStatus.RUNNING_LATE -> {
                title = "Running Late"
                color = PlansColor.PINK_RUNNING
            }
            EventStatus.LIVED -> {
                (startedTime ?: startTime)?.toLong()?.takeIf { it > 0 }?.also {
                    title = Date(it.toMilliseconds()).getStartedString(true)
                }
                color = PlansColor.BlUE_TEXT
            }
            EventStatus.ENDED -> {
                title = "Ended"
                color = PlansColor.PURPLE_JOIN
            }
            EventStatus.CANCELLED -> {
                title = "Cancelled"
                color = PlansColor.BROWN_CANCELLED
            }
            EventStatus.EXPIRED -> {
                title = "Expired"
                color = PlansColor.ORANGE_EXPIRED
            }
        }

        return Triple(title, color, eventStatus)
    }

    fun getStartEndTime(widthMax: Int? = null, textView: TextView? = null) : String {
        var textStart: String? = null
        var textEnd:String? = null
        var textEndTime: String? = null
        var textDate : String? = null

        if (startTime != null)  {
            val dateTime = Date(startTime!!.toMilliseconds())
            textDate = dateTime.toFormatString("E, MMM d, yyyy")
            textStart = if (dateTime.getYearInPlans() != Date().getYearInPlans()) {
                 textDate
            }else {
                dateTime.toFormatString("E, MMM d")
            }
            textStart += " at " + dateTime.toFormatString("h:mm a")
        }
        if (endTime != null)  {
            val dateTime = Date(endTime!!.toMilliseconds())
            textEnd = dateTime.toFormatString("E, MMM d, yyyy")
            textEndTime = dateTime.toFormatString("h:mm a")
            if (textEnd == textDate) {
                textEnd = textEndTime
            }else {
                if (dateTime.getYearInPlans() == Date().getYearInPlans()) {
                    textEnd = dateTime.toFormatString("E, MMM d")
                }
                textEnd += " at $textEndTime"
            }
        }

        var result = "${textStart ?: ""} - ${textEnd ?: ""}"

        if (widthMax != null && textView != null) {
            if (textView.getTextWidth(result) > widthMax) {
                result.split(" ").dropLast(2).joinToString(" ").takeIf { textView.getTextWidth(it) <= widthMax }?.also {
                    result = "$it\n$textEndTime"
                }
            }
        }

        return result
    }

    fun isTurnOffNoti (userId : String?) : Boolean {
        return if (this.userId == userId) {
            turnOffNoti ?: false
        } else {
            invitations?.firstOrNull{ item -> item.id == userId }?.turnOffNoti ?: false
        }
    }

    fun getSpannableStringForEventName (text: String? = null) : SpannableStringBuilder{
        // Event Name
        val spannable = SpannableStringBuilder("${text ?: eventName} ")
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Public/Private Icon
        val icon = if (isPublic == true) R.drawable.icon_public_green else R.drawable.icon_private_green
        spannable.append("ICON", ImageSpan(PLANS_APP, icon), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Public/Private Label
        val textPublic = if (isPublic == true) " Public" else " Private"
        val start = spannable.length
        spannable.append(textPublic, AbsoluteSizeSpan(13, true), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(PlansColor.GRAY_SECOND), start, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }

    fun getSpannableStringForEventName (textView: TextView, maxLines: Int? = null, padding: Int = 0) : SpannableStringBuilder{
        var spannable = getSpannableStringForEventName()
        maxLines?.takeIf { it > 0 }?.also {
            val widthMax = (OSHelper.widthScreen - padding) * it
            var widthText = textView.getEditableTextWidth(spannable) + 40.toPx()
            var last = 3
            while (widthText > widthMax) {
                last ++
                val text = eventName?.dropLast(last) + "..."
                spannable = getSpannableStringForEventName(text)
                widthText = textView.getEditableTextWidth(spannable) + 40.toPx()
            }
        }
        return spannable
    }


    fun isOwnEvent(_userId: String?) : Boolean {
        return _userId == (eventCreatedBy?.id ?: userId)
    }

    fun isPostingForMe() : Boolean {
        return isOwnEvent(UserInfo.userId) || (isPosting == true && (statusJoin == 2 || statusJoin == 3))
    }

    fun isLiveUser(_userId: String?) : Boolean {
        var live = false
        if (userId == _userId){
            live = isHostLive != 0
        }else if (invitations?.any { it.id == _userId } == true){
            live = invitations?.find { it.id == _userId }?.isLive != 0
        }
        return live
    }

    fun isEnableAddLiveMoment() : Boolean {
        return isPostingForMe() && isLiveUser(UserInfo.userId) && (isEnded ?: 0) == 0
    }

    fun getPendingInvitations() : Triple<Int, Int, Int> {
        val invitedEmails = invitedPeople?.filter { it.typeInvitation == InvitationModel.InviteType.EMAIL }?.toMutableList()
        val invitedMobiles = invitedPeople?.filter { it.typeInvitation == InvitationModel.InviteType.MOBILE }?.toMutableList()
        val countLinksInvited = invitations?.filter { it.typeInvitation == InvitationModel.InviteType.LINK }?.size ?: 0

        invitations?.forEach { user ->
            invitedEmails?.removeAll {it.email?.lowercase() == user.email?.lowercase()}
            invitedMobiles?.removeAll { it.mobile == user.mobile }
        }

        return Triple(invitedEmails?.size ?: 0,invitedMobiles?.size ?: 0, countLinksInvited)
    }

    fun getInvitedUserCounts() : Triple<Int, Int, Int> {
        val countFriends = invitations?.size ?: 0
        val countContacts = invitedPeople?.filter { people ->
            (people.typeInvitation == InvitationModel.InviteType.MOBILE) && !(invitations?.any{it.mobile == people.mobile} ?: false)
        }?.size ?: 0
        val countEmails = invitedPeople?.filter { people ->
            (people.typeInvitation == InvitationModel.InviteType.EMAIL) && !(invitations?.any{it.email?.lowercase() == people.email?.lowercase()} ?: false)
        }?.size ?: 0
        return Triple(countFriends, countContacts, countEmails)
    }

    fun getAllPeople() : ArrayList<InvitationModel> {
        val list = arrayListOf<InvitationModel>()
        invitations?.takeIf { it.size > 0 }?.let{ list.addAll(it)}
        invitedPeople?.takeIf { it.size > 0 }?.let{ list.addAll(it)}
        return list
    }

    fun getRemovedUserCounts(friendsNew: ArrayList<InvitationModel>?, peopleNew: ArrayList<InvitationModel>?) : Triple<Int, Int, Int> {
        val countFriends = invitations?.filter { old ->
            !(friendsNew == null || friendsNew.any{it.id == old.id})
        }?.size ?: 0

        val countContacts = invitedPeople?.filter{ people ->
            (people.typeInvitation == InvitationModel.InviteType.MOBILE) && !(invitations?.any { it.mobile == people.mobile} ?: false)
        }?.filter { old ->
            !(peopleNew == null || peopleNew.any { it.mobile == old.mobile })
        }?.size ?: 0


        val countEmails = invitedPeople?.filter{ people ->
            (people.typeInvitation == InvitationModel.InviteType.EMAIL) && !(invitations?.any { it.email?.lowercase() == people.email?.lowercase()} ?: false)
        }?.filter { old ->
            !(peopleNew == null || peopleNew.any { it.email?.lowercase() == old.email?.lowercase() })
        }?.size ?: 0

        return Triple(countFriends, countContacts, countEmails)
    }

    fun toMap() : MutableMap<String, RequestBody?>? {
        val map: MutableMap<String, RequestBody?> = HashMap()
        eventId?.also{
            map["eventId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        eventName?.also{
            map["eventsName"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        details?.also{
            map["details"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        userId?.also{
            map["userId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        address?.also{
            map["address"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        locationName?.also{
            map["locationName"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        long?.also{
            map["long"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        lat?.also{
            map["lat"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        caption?.also{
            map["caption"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        startDate?.also{
            map["startDate"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        startTime?.also{
            map["startTime"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        endDate?.also{
            map["endDate"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        endTime?.also{
            map["endTime"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        checkInRange?.also{
            map["checkInRange"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        isPublic?.also{
            map["isPublic"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        isGroupChatOn?.also{
            map["isGroupChatOn"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        isCancel?.also{
            map["isCancel"] = it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        }
        friendsContactNumbers?.also{
            map["friendsContactNumbers"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        mediaType?.also{
            map["mediaType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        imageOrVideo?.takeIf { it.isNotEmpty() }?.also{
            map["imageOrVideo"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        thumbnail?.takeIf { it.isNotEmpty() }?.also{
            map["thumbnail"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        invitedPeople?.takeIf { it.isNotEmpty() }?.also { people ->
            for (i in 0 until people.size) {
                people[i].firstName?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"firstName\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].lastName?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"lastName\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].name?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"name\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].fullName?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"fullName\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].id?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"userId\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].mobile?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"mobile\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].email?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"email\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].invitedType?.toString()?.also{
                    map["invitedPeople[$i][\"invitedType\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                people[i].profileImage?.takeIf { it.isNotEmpty() }?.also{
                    map["invitedPeople[$i][\"profileImage\"]"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
            }
        }

        return map
    }

    fun isExistLiveGuest() : Boolean {
        return invitations?.any { it.isLive == 1 } ?: false
    }

    fun isExistAcceptGuest() : Boolean {
        return invitations?.any{ (it.status ?: 0) > 1 } ?: false
    }

    fun isEnableToShow(userId: String?): Boolean {
        var result = true

        if (isActive == false) {
            result = false
        }else if (!isOwnEvent(userId)) {
            if (isPublic == false && !isAttended(userId)) {
                result = false
            }
        }

        return result
    }

    fun isAttended(userId: String?): Boolean {
        return if (isOwnEvent(userId)) true else {
            invitations?.any { !it.id.isNullOrEmpty() && it.id == userId } ?: false
        }
    }

    fun getAttendee(userId: String? = null, mobile: String? = null, email: String? = null): InvitationModel? {
        return invitations?.firstOrNull { item ->
            var result = false
            mobile?.takeIf{ it.isNotEmpty() }?.also {
                result = item.mobile == mobile
            }
            email?.takeIf{ !result && it.isNotEmpty() }?.also {
                result = item.email == email
            }
            userId?.takeIf{ !result && it.isNotEmpty() }?.also {
                result = item.id == userId
            }
            result
        }
    }



}
