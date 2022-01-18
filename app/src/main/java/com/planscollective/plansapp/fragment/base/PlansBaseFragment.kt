package com.planscollective.plansapp.fragment.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.constants.Urls
import com.planscollective.plansapp.constants.iOSInfo
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.extension.toMilliseconds
import com.planscollective.plansapp.extension.urlQueryString
import com.planscollective.plansapp.fragment.users.FriendsSelectionFragment
import com.planscollective.plansapp.fragment.utils.PlansDialogFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.FileHelper
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnSelectedMenuItem
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.*
import com.planscollective.plansapp.models.viewModels.EditInvitationVM
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import com.planscollective.plansapp.webServices.chat.ChatService
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.friend.FriendWebService
import com.planscollective.plansapp.webServices.liveMoment.LiveMomentWebService
import com.planscollective.plansapp.webServices.post.PostWebService
import com.planscollective.plansapp.webServices.report.ReportWebService
import com.planscollective.plansapp.webServices.user.UserWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class PlansBaseFragment<VBinding: ViewBinding> : BaseFragment<VBinding>(),
    OnSingleClickListener,
    PlansActionListener,
    OnSelectedMenuItem
{

    open var actionEnterKey : ((view: View) -> Unit)? = {
        hideKeyboard()
        refreshAll()
    }

    //******************************* OnSingleClickListener ***************************************//
    override fun onSingleClick(v: View?) {

    }

    //********************************* Plans Action Listener **************************************//
    override fun onClickedEvent(event: EventModel?, eventId: String?) {
        gotoEventDetails(event, eventId)
    }

    override fun onClickedUser(user: UserModel?, event: EventModel?) {
        gotoUserProfile(user)
    }

    override fun onClickedJoin(event: EventModel?) {
        joinOnOffEvent(event)
    }

    override fun onClickedUnjoin(event: EventModel?) {
        joinOnOffEvent(event, false)
    }

    override fun onClickedMoreMenu(event: EventModel?, menuType: MenuOptionHelper.MenuType) {
        MenuOptionHelper.showPlansMenu(event, menuType, this, this)
    }

    override fun onClickedMoreMenuSavedEvent(event: EventModel?) {
        MenuOptionHelper.showPlansMenu(event, MenuOptionHelper.MenuType.EVENT_SAVED, this, this)
    }


    override fun onClickedMoreUsers(event: EventModel?) {
        gotoEventGuests(event)
    }

    override fun onClickedDateTime(event: EventModel?) {
        gotoCalendarEvent(event)
    }

    override fun onClickedLocation(event: EventModel?) {
        gotoGoogleMap(event)
    }

    override fun onClickedHiddenEvents() {
        gotoHiddenEvents()
    }

    override fun onClickedBack() {
        gotoBack()
    }

    override fun onClickEditEvent(event: EventModel?){
        gotoEditEvent(event, false)
    }

    override fun onClickInviteUser(event: EventModel?){
        gotoEditInvitation(event)
    }

    override fun onClickDetailsOfEvent(event: EventModel?){
        gotoDetailsOfEvent(event)
    }

    override fun onClickChatOfEvent(event: EventModel?){
        gotoChatting(event)
    }

    override fun onClickGuestAction(actionName: String?, event: EventModel?){
        when(actionName) {
            "JOIN" -> {
                joinOnOffEvent(event, true)
            }
            "JOINED" -> {
                MenuOptionHelper.showPlansMenu(event, MenuOptionHelper.MenuType.EVENT_JOIN, this, this)
            }
            "YOU'RE HERE", "NOT HERE" -> {
                MenuOptionHelper.showPlansMenu(event, MenuOptionHelper.MenuType.EVENT_LEAVE, this, this)
            }
            "GOING", "MAYBE", "NEXT TIME", "PENDING INVITE" -> {
                MenuOptionHelper.showPlansMenu(event, MenuOptionHelper.MenuType.EVENT_PENDING, this, this)
            }
        }
    }

    override fun onClickEventInvitationAction(actionName: String?, event: EventModel?) {
        when(actionName) {
            "GOING" -> {
                goingMaybeNextTime(event, 2)
            }
            "MAYBE" -> {
                goingMaybeNextTime(event, 3)
            }
            "NEXT TIME" -> {
                goingMaybeNextTime(event, 4)
            }
        }
    }

    override fun onClickAddLiveMoment(event: EventModel?){
        gotoLiveMomentCamera(event)
    }

    override fun onClickWatchUserLiveMoments(userLiveMoments: UserLiveMomentsModel?, event: EventModel?){
        gotoWatchLiveMoments(event, userLiveMoments?.user)
    }

    override fun onClickShowAllLiveMoments(event: EventModel?){
        gotoLiveMomentsAll(event)
    }

    override fun onClickPost(post: PostModel?, event: EventModel?, postId: String?, eventId: String?){
        gotoPostComment(post, event, postId, eventId)
    }

    override fun onClickMoreMenuForContent(
        content: PostModel?,
        post: PostModel?,
        event: EventModel?
    ) {
        val dic = hashMapOf("content" to content, "post" to post, "event" to event)
        MenuOptionHelper.showPlansMenu(dic, MenuOptionHelper.MenuType.POST_COMMENT, this, this)
    }

    override fun onClickOpenPhoto(urlPhoto: String?, data: Any?){
        gotoOpenPhoto(urlPhoto, data)
    }

    override fun onClickPlayVideo(urlVideo: String?, data: Any?){
        gotoPlayVideo(urlVideo, data)
    }

    override fun onClickLikeContent(
        content: PostModel?,
        post: PostModel?,
        event: EventModel?,
        isLike: Boolean
    ) {
        likeContent(content, post, event, isLike)
    }

    override fun onClickLikePost(postId: String?, eventId:String?, isLike: Boolean){
        likePost(postId, eventId, isLike)
    }

    override fun onClickMoreUsersLiked(post: PostModel?, event: EventModel?){
        gotoUsersLiked(post, event)
    }

    override fun onClickedFriendAction(user: UserModel?, action: String?){
        actionFriends(user, action)
    }

    override fun onClickedChat(user: UserModel?){
        gotoChatting(user = user)
    }

    override fun onClickedUserImage(user: UserModel?){
        gotoOpenPhoto(user?.profileImage, user)
    }

    override fun onClickedCoinStar(user: UserModel?){
        gotoCoinStar(user)
    }

    override fun onClickedFriends(user: UserModel?){
        gotoFriendList(user)
    }

    override fun onClickedCreateEvent(){
        gotoCreateEvent()
    }


    //********************************* Menu options Listener **************************************//
    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        when(data) {
            is EventModel -> {
                actionEvent(menuItem?.titleText, data as? EventModel)
            }
            is HashMap<*, *> -> {
                val content = data["content"]
                when(content) {
                    is PostModel -> {
                        actionPostComment(menuItem?.titleText, content as? PostModel, data["post"] as? PostModel, data["event"] as? EventModel)
                    }
                    is LiveMomentModel -> {
                        actionLiveMoment(menuItem?.titleText, content as? LiveMomentModel, data["userLiveMoment"] as? UserLiveMomentsModel, data["event"] as? EventModel)
                    }
                }
            }
            is UserModel -> {
                actionUser(menuItem?.titleText, data as? UserModel)
            }
            is ChatModel -> {
                actionChat(menuItem?.titleText, data as? ChatModel)
            }
        }
    }

    open fun actionChat(
        actionName: String?,
        chat: ChatModel?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        when (actionName) {
            "Mute Notifications" -> {
                muteChatNotify(chat, true, complete = complete)
            }
            "Unmute Notifications" -> {
                muteChatNotify(chat, false, complete = complete)
            }
            "Invite People" -> {
                gotoEditInvitation(chat?.event)
            }
            "Add People" -> {
                gotoFriendsSelections(FriendsSelectionFragment.SelectType.CHAT_ADD_PEOPLE, chat?.people, chat?.id)
            }
            "Cancel Event" -> {
                cancelEvent(chat?.event, complete = complete)
            }
            "End Event" -> {
                endEvent(chat?.event, complete = complete)
            }
            "Leave Event" -> {
                leaveEvent(chat?.event, complete = complete)
            }
            "Delete Chat", "Hide Chat" -> {
                updateChatHidden(chat, true, complete = complete)
            }
            "Leave Chat" -> {
                val userId = UserInfo.userId
                if (chat?.organizer?.id == userId && chat?.isGroup == true && !chat.isEventChat) {
                    leaveChatWithAssignAdmin(chat.id)
                }else {
                    removeUserInChat(chat, userId = userId, complete = complete)
                }
            }
        }

    }

    private fun actionUser(actionName: String?, user: UserModel?) {
        val userModel = user ?: return
        val action = actionName?.takeIf{it.isNotEmpty()} ?: return
        when(action) {
            "Block User" -> {
                blockUser(userModel)
            }
            "Send Message" -> {
                sendMessage(userModel)
            }
            "Report" -> {
                reportUser(userModel)
            }
        }
    }

    private fun actionLiveMoment(actionName: String?,
                                  liveMoment: LiveMomentModel?,
                                  userLiveMoment: UserLiveMomentsModel? = null,
                                  eventModel: EventModel? = null) {
        val moment = liveMoment ?: return
        val action = actionName.takeIf { !it.isNullOrEmpty() } ?: return

        when(action) {
            "Report" -> {
                reportContent(moment)
            }
            "Delete" -> {
                deleteLiveMoment(moment, eventModel)
            }
        }
    }

    private fun actionPostComment(actionName: String?,
                                  contentModel: PostModel?,
                                  postModel: PostModel? = null,
                                  eventModel: EventModel? = null) {
        val content = contentModel ?: return
        val action = actionName.takeIf { !it.isNullOrEmpty() } ?: return
        when(action) {
            "Share" -> {
                sharePostComment(content, eventModel, postModel)
            }
            "Download" -> {
                downloadContent(content)
            }
            "Report" -> {
                reportContent(content)
            }
            "Delete" -> {
                deleteContent(content, postModel, eventModel)
            }
        }

    }

    private fun actionEvent(actionName: String?, eventModel: EventModel?) {
        val event = eventModel ?: return
        val action = actionName.takeIf { !it.isNullOrEmpty() } ?: return
        when(action) {
            "Edit Event", "Update Event" -> {
                gotoEditEvent(event)
            }
            "Turn On Posting" -> {
                turnOnOffPosting(event)
            }
            "Turn Off Posting" -> {
                turnOnOffPosting(event, false)
            }
            "Mute Notifications" -> {
                muteOnOffNotification(event)
            }
            "Unmute Notifications" -> {
                muteOnOffNotification(event, false)
            }
            "Share Event" -> {
                shareEvent(event)
            }
            "Duplicate Event" -> {
                gotoEditEvent(event, true)
            }
            "Delete Event" -> {
                deleteEvent(event)
            }
            "End Event" -> {
                endEvent(event)
            }
            "Cancel Event" -> {
                cancelEvent(event)
            }
            "Save Event" -> {
                saveOnOffEvent(event)
            }
            "Unsave Event" -> {
                saveOnOffEvent(event, false)
            }
            "Hide Event" -> {
                hideOnOffEvent(event)
            }
            "Unhide Event" -> {
                hideOnOffEvent(event, false)
            }
            "Leave Event" -> {
                leaveEvent(event)
            }
            "Report" -> {
                reportEvent(event)
            }
            "Join" -> {
                joinOnOffEvent(event, true)
            }
            "Unjoin" -> {
                joinOnOffEvent(event, false)
            }
            "Going" -> {
                goingMaybeNextTime(event, 2)
            }
            "Maybe" -> {
                goingMaybeNextTime(event, 3)
            }
            "Next Time" -> {
                goingMaybeNextTime(event, 4)
            }
            "Add to Calendar" -> {
                gotoInsertEventInCalendar(event)
            }
        }

    }


    //************************************ Go to other screens *****************************************//
    open fun gotoDashboard() {
        gotoBack(R.id.dashboardFragment)
    }

    // Event List
    open fun gotoHiddenEvents() {
        navigate(R.id.action_global_hiddenEventsFragment)
    }

    open fun gotoSearchEvents() {
        navigate(R.id.action_global_searchEventFragment)
    }

    open fun gotoChats(notification: NotificationActivityModel? = null) {
        navigate(R.id.action_global_chatsFragment)
        notification?.chatId?.takeIf { it.isNotEmpty() }?.also {
            lifecycleScope.launch(Dispatchers.IO){
                delay(300)
                withContext(Dispatchers.Main) {
                    gotoChatting(chatId = it)
                }
            }
        }
    }


    // Events related
    open fun gotoCreateEvent(place: PlaceModel? = null) {
        val action = NavDashboardDirections.actionGlobalCreateEventFragment()
        action.placeModel = place
        navigate(directions = action)
    }

    open fun gotoEditEvent(event: EventModel?, isDuplicate: Boolean = false) {
        val eventId = event?.id?.takeIf{it.isNotEmpty()} ?: return
        val action = NavDashboardDirections.actionGlobalEditEventFragment(eventId)
        action.isDuplicate = isDuplicate
        action.eventModel = event
        navigate(directions = action)
    }

    open fun gotoEventDetails(event: EventModel? = null, eventId: String? = null) {
        val id = event?.id ?: eventId ?: return
        val action = NavDashboardDirections.actionGlobalEventDetailsFragment(id)
        navigate(directions = action)
    }

    open fun gotoInviteByLink(event: EventModel?) {
        if (event == null ) return
        val action = NavDashboardDirections.actionGlobalInviteByLinkFragment(event)
        navigate(directions = action)
    }

    open fun gotoDetailsOfEvent(event: EventModel?) {
        val eventId = event?.id ?: return
        val action = NavDashboardDirections.actionGlobalDetailsOfEventFragment(eventId)
        navigate(directions = action)
    }

    open fun gotoEditInvitation(event: EventModel? = null,
                                editMode: EditInvitationVM.EditMode = EditInvitationVM.EditMode.EDIT,
                                usersSelected: ArrayList<InvitationModel>? = null,
                                eventId: String? = null
    ) {
        val event_id = event?.id ?: eventId
        val action = NavDashboardDirections.actionGlobalEditInvitationFragment()
        action.eventId = event_id
        action.editMode = editMode.name
        action.usersSelected = usersSelected?.toTypedArray() ?: event?.getAllPeople()?.toTypedArray()

        navigate(directions = action)
    }

    open fun gotoEventGuests(event: EventModel? = null, eventId: String? = null) {
        val event_id = event?.id ?: eventId ?: return
        val action = NavDashboardDirections.actionGlobalPeopleInvitedFragment(event_id)
        navigate(directions = action)
    }

    // Chat related
    open fun gotoChatting(event: EventModel? = null,
                          user: UserModel? = null,
                          chatModel: ChatModel? = null,
                          chatId: String? = null) {
        val members = if (user != null) arrayListOf(UserModel(id = UserInfo.userId), user) else null
        val chat = chatModel ?: ChatModel(id = chatId, event = event).let {
            if (it.id.isNullOrEmpty() && it.event?.chatId.isNullOrEmpty() && members.isNullOrEmpty()) null else {
                it.id = it.id ?: it.event?.chatId
                it.eventId = it.eventId ?: it.event?.id
                it.members = members.takeIf { !it.isNullOrEmpty() }?.map {
                    ChatUserModel().apply {
                        id = it.id
                        firstName = it.firstName
                        lastName = it.lastName
                        email = it.email
                        mobile = it.mobile
                        profileImage = it.profileImage
                    }
                }?.toArrayList()
                it
            }
        } ?: return

        val action = NavDashboardDirections.actionGlobalChattingFragment(chat)
        navigate(directions = action)
    }

    open fun gotoChatSettings(chat: ChatModel? = null, chatId: String? = null){
        val chat_id = chat?.id ?: chatId ?: return
        val action = NavDashboardDirections.actionGlobalChatSettingsFragment(chat_id)
        action.chatId = chat_id
        action.chatModel = chat
        navigate(directions = action)
    }

    // User Profile related
    open fun gotoUserProfile(user: UserModel? = null, userId: String? = null) {
        val user_id = (user?.id ?: userId)?.takeIf { it != UserInfo.userId } ?: return
        if (user != null && user.isActive == false) return

        val action = NavDashboardDirections.actionGlobalUserProfileFragment(user_id)
        action.userModel = user
        navigate(directions = action)
    }

    // Calendar
    open fun gotoCalendarEvent(event: EventModel? = null) {
        val action = NavDashboardDirections.actionGlobalCalendarFragment()
        action.eventModel = event
        navigate(directions = action)
    }

    // Map
    open fun gotoGoogleMap(event: EventModel?) {
        gotoGoogleMap(event?.lat?.toDouble(), event?.long?.toDouble(), event?.address)
    }

    open fun gotoGoogleMap(lat: Double?, long: Double?, address: String? = null) {
        val latitude = lat ?: return
        val longitude = long ?: return

        if (lat == 0.0 && long == 0.0) return

        var uriString = "geo:$latitude,$longitude"
        address?.takeIf { it.isNotEmpty() }?.also {
            uriString += "?q=${Uri.encode(it)}"
        }

        // Create a Uri from an intent string. Use the result to create an Intent.
        val gmmIntentUri = Uri.parse(uriString)

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps")

        mapIntent.resolveActivity(requireActivity().packageManager)?.let {
            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent)
        }
    }

    open fun gotoPhoneCall(number: String?) {
        number?.takeIf { it.isNotEmpty() }?.also {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$it")
            startActivity(dialIntent)
        }
    }

    open fun gotoOpenUrl(url: String?) {
        url?.takeIf { it.isNotEmpty() }?.let {
            if (!it.startsWith("https://") && !it.startsWith("http://")) {
                "http://" + it
            }else it
        }?.let {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(browserIntent)
        }
    }

    // Live moment camera
    open fun gotoLiveMomentCamera(event: EventModel?) {
        val eventId = event?.id ?: return
        val eventName = event?.eventName ?: return

        val action = NavDashboardDirections.actionGlobalLiveMomentCameraFragment(eventId, eventName)
        action.isLive = event?.isLive == 1

        navigate(directions = action)
    }

    open fun gotoWatchLiveMoments(event: EventModel? = null,
                                  user: UserModel? = null,
                                  liveMoment: LiveMomentModel? = null,
                                  eventId: String? = null,
                                  liveMomentId: String? = null
    ) {
        val event_id = event?.id ?: eventId ?: return
        val action = NavDashboardDirections.actionGlobalWatchLiveMomentsFragment(event_id)
        action.userId = user?.id
        action.liveMomentId = liveMoment?.id ?: liveMomentId

        navigate(directions = action)
    }

    open fun gotoLiveMomentsAll(event: EventModel? = null, eventId: String? = null) {
        val event_id = event?.id ?: eventId ?: return
        val action = NavDashboardDirections.actionGlobalLiveMomentsFragment(event_id)
        navigate(directions = action)
    }

    // Post Comment
    open fun gotoPostComment(post: PostModel? = null, event: EventModel? = null, postId: String? = null, eventId: String? = null){
        val post_Id = post?.id ?: postId ?: return
        val event_Id = event?.id ?: eventId ?: return
        val action = NavDashboardDirections.actionGlobalPostDetailsFragment(event_Id, post_Id)
        navigate(directions = action)
    }

    open fun gotoUsersLiked(post: PostModel?, event: EventModel?) {
        val postId = post?.id?.takeIf { it.isNotEmpty() } ?: return
        val eventId = event?.id?.takeIf { it.isNotEmpty() } ?: return

        val action = NavDashboardDirections.actionGlobalUsersLikedPostFragment(eventId, postId)
        navigate(directions = action)
    }

    // Open Image
    open fun gotoOpenPhoto(urlPhoto: String?, data: Any? = null) {
        val urlString = urlPhoto?.takeIf { it.isNotEmpty() } ?: return
        val action = NavDashboardDirections.actionGlobalOpenImageFragment(urlString)
        when(data) {
            is EventModel -> {
                action.title = (data as? EventModel)?.eventName?.takeIf { it.isNotEmpty() }
            }
        }
        navigate(directions = action)
    }

    // Play Video screen
    open fun gotoPlayVideo(urlVideo: String?, data: Any? = null) {
        if (urlVideo.isNullOrEmpty()) return

        val action = NavDashboardDirections.actionGlobalVideoPlayerFragment(urlVideo)
        navigate(directions = action)
    }

    // go to Phone Settings
    open fun gotoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // go to Country Picker
    open fun gotoCountryPicker() {
        navigate(R.id.action_global_countryCodeListFragment3)
    }

    // go to Location selection
    open fun gotoLocationSelection(typeSelection: LocationSelectionVM.LocationSelectionType = LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) {
        val action = NavDashboardDirections.actionGlobalLocationSelectionFragment()
        action.typeSelection = typeSelection.name
        navigate(directions = action)
    }

    // go to Find Places
    open fun gotoFindPlaces(typeSelection: LocationSelectionVM.LocationSelectionType = LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) {
        val action = NavDashboardDirections.actionGlobalLocationDiscoveryFragment()
        action.typeSelection = typeSelection.name
        navigate(directions = action)
    }

    open fun gotoCoinStar(user: UserModel? = null, userId: String? = null) {
        val user_id = user?.id ?: userId ?: return
        val action = NavDashboardDirections.actionGlobalCoinsFragment(user_id)
        action.userModel = user
        navigate(directions = action)
    }

    open fun gotoFriendList(user: UserModel? = null, userId: String? = null) {
        val user_id = (user?.id ?: userId)?.takeIf { it.isNotEmpty() } ?: return
        val action = NavDashboardDirections.actionGlobalFriendsListFragment(user_id)
        action.userModel = user
        navigate(directions = action)
    }

    open fun gotoAddFriends(user: UserModel? = null, userId: String? = null) {
        val user_id = (user?.id ?: userId ?: UserInfo.userId)?.takeIf { it.isNotEmpty() } ?: return
        val action = NavDashboardDirections.actionGlobalAddFriendsFragment()
        action.userId =  user_id
        action.userModel = user
        navigate(directions = action)
    }

    open fun gotoFriendsSelections(
        type: FriendsSelectionFragment.SelectType = FriendsSelectionFragment.SelectType.CHAT_START,
        usersSelected: ArrayList<UserModel>? = null,
        chatId: String? = null
    ) {
        val action = NavDashboardDirections.actionGlobalFriendsSelectionFragment()

        action.type = type.name
        action.usersSelected = usersSelected?.toTypedArray()
        action.chatId = chatId

        navigate(directions = action)
    }

    open fun gotoEventInvitations() {
        navigate(R.id.action_global_eventInvitationsFragment)
    }

    open fun gotoFriendRequests() {
        navigate(R.id.action_global_friendRequestsFragment)
    }

    open fun gotoInsertEventInCalendar(event: EventModel?){
        val eventInfo = event ?: return
        val startMillis = eventInfo.startTime?.toLong()?.toMilliseconds() ?: return
        val endMillis = eventInfo.endTime?.toLong()?.toMilliseconds() ?: return
        val eventName = eventInfo.eventName?.takeIf { it.isNotEmpty() } ?: return

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, eventName)
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

        eventInfo.details?.takeIf { it.isNotEmpty() }?.also {
            intent.putExtra(CalendarContract.Events.DESCRIPTION, it)
        }

        eventInfo.address?.removeOwnCountry()?.takeIf { it.isNotEmpty() }?.also {
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, it)
        }

        startActivity(intent)
    }

    //************************************ Event Actions *****************************************//
    open fun turnOnOffPosting(event: EventModel?,
                              isOn: Boolean = true,
                              isShownLoading: Boolean = true
    ){
        if (isShownLoading)
            BusyHelper.show(requireContext())

        EventWebService.turnOnOffPosting(event?.id, isOn){ success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll(isShownLoading)
            }
            ToastHelper.showMessage(message)
        }
    }

    open fun muteOnOffNotification(event: EventModel?, isOn: Boolean = true, isShownLoading: Boolean = true) {
        if (isShownLoading)
            BusyHelper.show(requireContext())

        EventWebService.muteOnOffNotification(event?.id, isOn){ success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll(isShownLoading)
            }
            ToastHelper.showMessage(message)
        }
    }

    open fun shareEvent(event: EventModel?, isInviting: Boolean = false) {
        val eventName = event?.eventName?.takeIf { it.isNotEmpty() } ?: return

        var url = ""
        val text = if (isInviting) {
            url = event.eventLink?.invitation ?: ""
            "Hi, I'm inviting you to \"$eventName\". Download the Plans app to attend and share now!\n$url"
        }else {
            url = event.eventLink?.share ?: ""
            "Check out the event \"$eventName\" on the Plans app!\n$url"
        }

        val type = event.mediaType
        val urlMedia = event.imageOrVideo

        shareContent(text, type, urlMedia)
    }

    open fun shareContent(
        text: String? = null,
        typeMedia: String? = null,
        urlMedia: String? = null,
        urlAdditional: String? = null,
    ) {
        if (text.isNullOrEmpty() && (typeMedia.isNullOrEmpty() || urlMedia.isNullOrEmpty()) && urlAdditional == null) return

        var itemText : String? = text
        val type = typeMedia.takeIf { !it.isNullOrEmpty() } ?: "text"
        val urlDownload : String? = urlMedia

        urlAdditional?.takeIf { it.isNotEmpty() }?.also{
            if (!itemText.isNullOrEmpty()) {
                itemText += "\n"
            }
            itemText += it
        }

        if (type == "text" && !itemText.isNullOrEmpty()) {
            showShareComponent(itemText)
        }else if (!urlDownload.isNullOrEmpty()) {
            ToastHelper.showWaiting("Loading...")
            FileHelper.downloadFileFrom(urlDownload, type){
                file, message ->
                ToastHelper.hideWaiting()
                if (file?.exists() == true) {
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().packageName + ".provider",
                        file
                    )
                    if (type == "image") {
                        showShareComponent(itemText, uri)
                    }else if (type == "video") {
                        showShareComponent(itemText, videoUri = uri)
                    }
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }
    }

    open fun showShareComponent(text: String? = null, imageUri: Uri? = null, videoUri: Uri? = null) {

        if (text.isNullOrEmpty() && imageUri == null && videoUri == null) return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            text?.takeIf { it.isNotEmpty() }?.also {
                putExtra(Intent.EXTRA_TEXT, it)
                type = "text/plain"
            }

            imageUri?.also {
                putExtra(Intent.EXTRA_STREAM, it)
                type = "image/*"
            }

            videoUri?.also {
                putExtra(Intent.EXTRA_STREAM, it)
                type = "video/*"
            }
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        requireActivity().startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    open fun sharePostComment(content: PostModel?, event: EventModel? = null, post: PostModel? = null) {
        val contentPost = content ?: return
        BusyHelper.show(requireContext())
        createShareLink(contentPost, event, post) {
            uri, message ->
            BusyHelper.hide()
            if (uri == null) {
                ToastHelper.showMessage(message)
            }else {
                var text = ""
                var typeMedia: String? = null
                var urlMedia : String? = null

                if (contentPost.isComment) {
                    contentPost.commentText?.takeIf { it.isNotEmpty() }?.also {
                        text = "\"$it\""
                    }
                    if (text.isNotEmpty()) {
                        text += "\n"
                    }
                    text += "Check out this comment from the event \"${event?.eventName ?: ""}\" on the Plans app!"
                    typeMedia = contentPost.commentType
                    urlMedia = contentPost.commentMedia
                }else {
                    contentPost.postText?.takeIf { it.isNotEmpty() }?.also {
                        text = "\"$it\""
                    }
                    if (text.isNotEmpty()) {
                        text += "\n"
                    }
                    text += "Check out this post from the event \"${event?.eventName ?: ""}\" on the Plans app!"
                    typeMedia = contentPost.postType
                    urlMedia = contentPost.postMedia
                }

                shareContent(text, typeMedia, urlMedia, uri.toString())
            }
        }
    }

    open fun downloadContent(content: PostModel?) {
        val url = content?.urlMedia ?: return
        val type = content.type?.takeIf { it.isNotEmpty() } ?: return

        ToastHelper.showLoadingAlerts(ToastHelper.LoadingToastType.DOWNLOADING)
        FileHelper.downloadFileToPhotoAlbum(url, type){
            success, message ->
            ToastHelper.hideLoadingAlerts(ToastHelper.LoadingToastType.DOWNLOADING)
            ToastHelper.showMessage(message)
        }
    }

    open fun createShareLink(
        content: Any?,
        event: EventModel? = null,
        post: PostModel? = null,
        complete: ((uri: Uri?, message: String?) -> Unit)?
    ) {
        var dicParams : HashMap<String, String>? = null
        var title : String? = null
        var text: String? = null
        var description: String? = null
        var imageUrl: String? = null

        if (content != null && content is EventModel) {
            val contentEvent = content as? EventModel
            dicParams = HashMap()
            dicParams["type"] = "event_share"
            dicParams["eventId"] = contentEvent?.id ?: ""
            text = contentEvent?.eventName
            description = "Check out the event \"${contentEvent?.eventName ?: ""}\" on the Plans app!"
            imageUrl = if (contentEvent?.mediaType == "video") contentEvent.thumbnail else contentEvent?.imageOrVideo
        }else if (content != null && content is PostModel && event != null) {
            val contentPost = content as? PostModel
            dicParams = HashMap()
            if (contentPost?.isComment == true && post != null) {
                dicParams["type"] = "comment_share"
                dicParams["eventId"] = event.id ?: ""
                dicParams["postId"] = post.id ?: ""
                dicParams["commentId"] = contentPost.id ?: ""
                text = contentPost.commentText
                description = "Check out this comment from the event \"${event.eventName ?: ""}\" on the Plans app!"
            }else {
                dicParams["type"] = "post_share"
                dicParams["eventId"] = event.id ?: ""
                dicParams["postId"] = contentPost?.id ?: ""
                text = contentPost?.postText
                description = "Check out this post from the event \"${event.eventName ?: ""}\" on the Plans app!"
                if (contentPost?.postType == "video") {
                    imageUrl = contentPost.postThumbnail
                }else if (contentPost?.postType == "image") {
                    imageUrl = contentPost.postMedia
                }
            }
        }

        text?.takeIf { it.isNotEmpty() }?.also {
            title = "\"$it\""
        }

        val queryString = dicParams?.urlQueryString()?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also {it(null, "Failed to create a share link, Invalid parameters")}
        }

        val linkString = Urls.APP_LINKS_WEB_BASE + queryString
        val domainURIPrefixString = if (dicParams?.get("type") == "event_invitation") Urls.DYNAMIC_LINK_PREFIX_INVITE else Urls.DYNAMIC_LINK_PREFIX_SHARE

        Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
            link = Uri.parse(linkString)
            domainUriPrefix = domainURIPrefixString
            navigationInfoParameters {
                forcedRedirectEnabled = true
            }
            iosParameters(iOSInfo.BUNDLE_ID) {
                appStoreId = iOSInfo.APPSTORE_ID
                minimumVersion = iOSInfo.VERSION_MINIMUM
            }
            androidParameters(requireContext().packageName) {
                minimumVersion = 1
            }
            socialMetaTagParameters {
                (title ?: description)?.takeIf { it.isNotEmpty() }?.also {
                    this.title = it
                }
                description?.takeIf { it.isNotEmpty() && !title.isNullOrEmpty() }?.also {
                    this.description = it
                }
                imageUrl?.takeIf { it.isNotEmpty() }?.also {
                    this.imageUrl = Uri.parse(it)
                }
            }
        }.addOnSuccessListener{ (shortLink, flowChartLink) ->
            complete?.also {it(shortLink, "Succeeded to create a share link")}
        }.addOnFailureListener{
            it.printStackTrace()
            complete?.also {it(null, "Failed to create a share link")}
        }
    }

    open fun deleteEvent(event: EventModel?, isShownLoading: Boolean = true) {
        val dialog = PlansDialogFragment(ConstantTexts.ASK_DELETE_EVENT){
            if (isShownLoading)
                BusyHelper.show(requireContext())

            EventWebService.deleteEvent(event?.id){ success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll(isShownLoading)
                }
                ToastHelper.showMessage(message)
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun deleteContent(contentModel: PostModel?,
                           post: PostModel? = null,
                           event: EventModel? = null) {
        val content = contentModel ?: return
        val msg = if (content.isComment) {
            ConstantTexts.ASK_DELETE_COMMENT
        } else {
            ConstantTexts.ASK_DELETE_POST
        }
        val dialog = PlansDialogFragment(msg){
            if (content.isComment) {
                deleteComment(content.id, post?.id, event?.id)
            }else {
                deletePost(content.id, event?.id)
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun deleteComment(commentId: String?, postId: String?, eventId: String?) {
        BusyHelper.show(requireContext())
        PostWebService.deleteComment(commentId, postId, eventId){ success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    open fun deletePost(postId: String?, eventId: String?) {
        BusyHelper.show(requireContext())
        PostWebService.deletePost(postId, eventId){ success, message ->
            BusyHelper.hide()
            if (success) {
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    open fun deleteLiveMoment(liveMoment: LiveMomentModel?, event: EventModel?, complete: (() -> Unit)? = null) {
        val momentModel = liveMoment ?: return
        val eventModel = event ?: return
        val msg =  ConstantTexts.ASK_DELETE_LIVE_MOMENT
        val dialog = PlansDialogFragment(msg, actionNo = {
            complete?.let { it()}
        }){
            BusyHelper.show(requireContext())
            LiveMomentWebService.deleteLiveMoment(momentModel.id, eventModel.id) { success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll()
                }else {
                    ToastHelper.showMessage(message)
                }
                complete?.let { it()}
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }


    open fun endEvent(
        event: EventModel?,
        isShownLoading: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        val dialog = PlansDialogFragment(ConstantTexts.ASK_END_EVENT){
            if (isShownLoading)
                BusyHelper.show(requireContext())

            EventWebService.endEvent(event?.id){ success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll(isShownLoading)
                }
                ToastHelper.showMessage(message)
                complete?.also { it(success, message) }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun cancelEvent(
        event: EventModel?,
        isShownLoading: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        val dialog = PlansDialogFragment(ConstantTexts.ASK_CANCEL_EVENT){
            if (isShownLoading)
                BusyHelper.show(requireContext())

            EventWebService.cancelEvent(event?.id){ success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll(isShownLoading)
                }
                ToastHelper.showMessage(message)
                complete?.also { it(success, message) }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)

    }

    open fun saveOnOffEvent(event: EventModel?, isOn: Boolean = true, isShownLoading: Boolean = true){
        if (isShownLoading)
            BusyHelper.show(requireContext())

        EventWebService.saveOnOffEvent(event?.id, isOn){ success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll(isShownLoading)
            }
            ToastHelper.showMessage(message)
        }

    }

    open fun hideOnOffEvent(event: EventModel?, isOn: Boolean = true, isShownLoading: Boolean = true) {
        if (isShownLoading)
            BusyHelper.show(requireContext())

        EventWebService.hideOnOffEvent(event?.id, isOn){ success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll(isShownLoading)
            }
            ToastHelper.showMessage(message)
        }
    }

    open fun leaveEvent(
        event: EventModel?,
        isShownLoading: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        val dialog = PlansDialogFragment(ConstantTexts.ASK_LEAVE_EVENT){
            if (isShownLoading)
                BusyHelper.show(requireContext())

            EventWebService.leaveEvent(event?.id){ success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll(isShownLoading)
                }
                ToastHelper.showMessage(message)
                complete?.also { it(success, message) }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun reportEvent(event: EventModel?, isShownLoading: Boolean = true) {
        val dialog = PlansDialogFragment(ConstantTexts.ASK_REPORT_EVENT){
            reportEntity(event?.id, "event", isShownLoading)
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun reportContent(content: Any?, complete: (() -> Unit)? = null) {
        val contentReport = content ?: return
        var type = ""
        var msg = ""
        var id = ""

        when(contentReport) {
            is PostModel -> {
                val post = contentReport as? PostModel ?: return
                id = post.id ?: return
                msg = if (post.isComment) {
                    type = "comment"
                    ConstantTexts.ASK_REPORT_COMMENT
                } else {
                    type = "post"
                    ConstantTexts.ASK_REPORT_POST
                }
            }
            is LiveMomentModel -> {
                val liveMoment = contentReport as? LiveMomentModel ?: return
                id = liveMoment.id ?: return
                type = "liveMoment"
                msg = ConstantTexts.ASK_LIVE_MOMENT
            }
            else -> return
        }

        val dialog = PlansDialogFragment(msg, actionNo = {
            complete?.let { it()}
        }){
            reportEntity(id, type, complete = complete)
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }



    open fun joinOnOffEvent(event: EventModel?, isOn: Boolean = true, isShownLoading: Boolean = true) {
        if (isShownLoading)
            BusyHelper.show(requireContext())

        EventWebService.joinOnOffEvent(event?.id, isOn){ success, message ->
            BusyHelper.hide()
            if (success) {
                if ( isOn ) logJoinEvent(event)

                refreshAll(isShownLoading)
            }
            ToastHelper.showMessage(message)
        }
    }

    open fun goingMaybeNextTime(event: EventModel?, status: Int) {
        val eventId = event?.id ?: return
        BusyHelper.show(requireContext())
        EventWebService.goingMaybeNextTime(eventId, status) { success, message ->
            BusyHelper.hide()
            val msg = if (success){
                refreshAll()
                logJoinEvent(event, status)
                ConstantTexts.YOU_RESPONDED_TO_EVENT
            }else {
                message
            }
            ToastHelper.showMessage(msg)
        }
    }

    fun logJoinEvent(model: EventModel?, status: Int? = null) {
        val eventId = model?.id ?: return

        if (status == null || status == 2 || status == 3) {
            val eventType1 = if (model.isPublic == true)
                AnalyticsManager.EventType.JOIN_EVENT_PUBLIC
            else
                AnalyticsManager.EventType.JOIN_EVENT_PRIVATE

            AnalyticsManager.logEvent(eventType1, eventId)

            val eventType2: AnalyticsManager.EventType? =
                when (model.getAttendee(UserInfo.userId, UserInfo.mobile, UserInfo.email)?.typeInvitation) {
                    InvitationModel.InviteType.CONTACT, InvitationModel.InviteType.MOBILE -> AnalyticsManager.EventType.INVITATIONS_SMS
                    InvitationModel.InviteType.EMAIL -> AnalyticsManager.EventType.INVITATIONS_EMAIL
                    InvitationModel.InviteType.LINK -> AnalyticsManager.EventType.INVITATIONS_LINK
                    else -> null
                }

            AnalyticsManager.logEvent(eventType2, eventId)
        }
    }

    open fun removeGuestFromEvent(user: UserModel?,
                                  event: EventModel? = null,
                                  eventId: String? = null,
                                  actionName: String? = null,
                                  complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        val event_id = event?.id ?: eventId ?: return
        if (user?.id.isNullOrEmpty() && user?.email.isNullOrEmpty() && user?.mobile.isNullOrEmpty()) return

        val msg = "Are you sure you want to ${actionName ?: "remove"} this guest?"
        val dialog = PlansDialogFragment(msg, actionNo = {
            complete?.also {
                it(false, null)
            }
        }){
            BusyHelper.show(requireContext())
            EventWebService.removeGuestFromEvent(event_id, user?.id, user?.email, user?.mobile) {
                success, message ->
                BusyHelper.hide()
                complete?.also {
                    it(success, message)
                }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    open fun likeContent(content: PostModel?,
                         postModel: PostModel? = null,
                         eventModel: EventModel? = null,
                         isLike: Boolean = true) {
        if (content?.isComment == true) {
            likeComment(content?.id, postModel?.id, eventModel?.id, isLike)
        }else {
            likePost(content?.id, eventModel?.id, isLike)
        }
    }

    open fun likeComment(commentId: String?,
                         postId: String?,
                         eventId: String?,
                         isLike: Boolean = true) {
        PostWebService.likeUnlikeComment(commentId, postId, eventId, isLike) { success, message ->
            if (success) {
                refreshAll()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    open fun likePost(postId: String?,
                      eventId: String?,
                      isLike: Boolean = true) {
        PostWebService.likeUnlikePost(postId, eventId, isLike) { success, message ->
            if (success) {
                refreshAll()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //************************************ Report Actions *****************************************//
    fun reportEntity(id: String?, type: String?, isShownLoading: Boolean = true, complete: (() -> Unit)? = null) {
        val entity_id = id ?: return
        val entity_type = type ?: return

        if (isShownLoading)
            BusyHelper.show(requireContext())

        ReportWebService.reportEntity(entity_id, entity_type){ success, message ->
            BusyHelper.hide()
            ToastHelper.showMessage(message)

            if (complete != null) {
                complete()
            }
        }
    }

    //****************************************** User Actions ******************************//
    fun actionFriends(user: UserModel?, action: String? = null, complete: ((toPosition: Int?) -> Unit)? = null) {

        when(action?.uppercase()) {
            "REQUESTED" -> {
                cancelFriendQuest(user)
            }
            "CONFIRM REQUEST", "ACCEPT" -> {
                acceptFriendRequest(user)
            }
            "FRIENDS" -> {
                unfriend(user)
            }
            "UNBLOCK" -> {
                unblockUser(user)
            }
            "ADD FRIEND" -> {
                sendFriendRequest(user)
            }
            "INVITE" -> {
                sendInviteSMS(user)
            }
            "REJECT" -> {
                rejectFriendRequest(user)
            }
            else -> {}
        }
    }

    fun cancelFriendQuest(user: UserModel?) {
        val friendId = user?.id ?: return
        BusyHelper.show(requireContext())
        FriendWebService.cancelFriendRequest(friendId) { success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun acceptFriendRequest(user: UserModel?) {
        val friendId = user?.id ?: return
        val name = user.fullName ?: user.name ?: "${user.firstName ?: ""} ${user.lastName ?: ""}"
        val msgSuccess = "You are now friends with $name"

        BusyHelper.show(requireContext())
        FriendWebService.acceptFriendRequest(friendId) { success, message ->
            BusyHelper.hide()
            val msg = if (success) {
                refreshAll()
                AnalyticsManager.logEvent(AnalyticsManager.EventType.FRIEND_ADD)
                msgSuccess
            }else {
                message
            }
            ToastHelper.showMessage(msg)
        }
    }

    fun rejectFriendRequest(user: UserModel?) {
        val friendId = user?.id ?: return
        BusyHelper.show(requireContext())
        FriendWebService.rejectFriendRequest(friendId) { success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll()
                ToastHelper.showMessage("Friend request has been rejected")
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }


    fun unfriend(user: UserModel?) {
        val friendId = user?.id ?: return
        val fullName = user.fullName ?: user.name ?: ((user.firstName ?: "") + " " + (user.lastName ?: ""))
        val msg = "Are you sure you want to unfriend $fullName?"

        val dialog = PlansDialogFragment(msg, titleYes = "CANCEL", titleNo = "UNFRIEND", urlImage = user.profileImage, actionNo = {
            BusyHelper.show(requireContext())
            FriendWebService.unfriend(friendId){
                success, message ->
                BusyHelper.hide()
                if (success) {
                    refreshAll()
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        })
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun blockUser(user: UserModel?) {
        val friendId = user?.id ?: return
        val fullName = user.fullName ?: user.name ?: ((user.firstName ?: "") + " " + (user.lastName ?: ""))
        val msg = "Are you sure you want to block $fullName?"

        val dialog = PlansDialogFragment(msg, titleYes = "BLOCK"){
            BusyHelper.show(requireContext())
            FriendWebService.blockUser(friendId){ success, message ->
                BusyHelper.hide()
                var msgResult = message
                if (success) {
                    PLANS_APP.addBlockedUserIDs(friendId)
                    refreshAll()
                    msgResult = "You have blocked $fullName"
                }
                ToastHelper.showMessage(msgResult)
            }

        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun unblockUser(user: UserModel?, complete: ((success: Boolean?, message: String?) -> Unit)? = null) {
        val friendId = user?.id ?: return
        val fullName = user.fullName ?: user.name ?: ((user.firstName ?: "") + " " + (user.lastName ?: ""))
        val msg = "Are you sure you want to unblock $fullName?"

        val dialog = PlansDialogFragment(msg, titleYes = "UNBLOCK"){
            BusyHelper.show(requireContext())
            FriendWebService.unblockUser(friendId){ success, message ->
                BusyHelper.hide()
                var msgResult = message
                if (success) {
                    refreshAll()
                    msgResult = "You have unblocked $fullName"
                    complete?.also { it(true, null) }
                }else {
                    complete?.also { it(false, msgResult) }
                }
                ToastHelper.showMessage(msgResult)
            }

        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun sendFriendRequest(user: UserModel? = null, userId: String? = null) {
        val friendId = (user?.id ?: userId)?.takeIf { it.isNotEmpty() } ?: return
        BusyHelper.show(requireContext())
        FriendWebService.sendFriendRequest(friendId){ success, message ->
            BusyHelper.hide()
            if (success) {
                AnalyticsManager.logEvent(AnalyticsManager.EventType.FRIEND_REQUEST)
                refreshAll()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun sendInviteSMS(user: UserModel? = null, mobileNum: String? = null) {
        val mobile = user?.mobile ?: mobileNum ?: return

        BusyHelper.show(requireContext())
        FriendWebService.sendSMSForPlansInvite(mobile) { success, message ->
            BusyHelper.hide()
            if (success) {
                refreshAll()
            }else {
                ToastHelper.showMessage("Failed to send the invitation sms to the phone, please try again or check the number")
            }
        }
    }

    fun sendMessage(user: UserModel?) {
        user?.also {
            if (it.friendShipStatus == 5 ) {
                unblockUser(it) { success, _ ->
                    if (success == true) {
                        createChatGroup(arrayListOf(it), false)
                    }
                }
            }else {
                createChatGroup(arrayListOf(it), false)
            }
        }
    }

    fun reportUser(user: UserModel?) {
        val userId = user?.id ?: return
        val msg = "Are you sure you want to report this user?"

        val dialog = PlansDialogFragment(msg){
            reportEntity(userId, "user", true)
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)

    }

    //*********************************** Common Methods ***************************************//

    fun getSpanTextForEditing (friends: Int = 0, contacts: Int = 0, emails: Int = 0, isSelected: Boolean = true) : SpannableStringBuilder? {
        var spannable : SpannableStringBuilder? = null
        val arraySpanText = ArrayList<String>()
        val arrayOriginal = ArrayList<String>()

        // Friends
        if (friends > 0) {
            val text = if (friends > 1) "$friends friends" else "1 friend"
            arraySpanText.add(text)
        }

        // Contacts
        if (contacts > 0) {
            val text = if (contacts > 1) "$contacts contacts" else "1 contact"
            arraySpanText.add(text)
        }

        // Emails
        if (emails > 0) {
            val text = if (emails > 1) "$emails emails" else "1 email"
            arraySpanText.add(text)
        }


        if (arraySpanText.size > 0) {
            arrayOriginal.addAll(arrayListOf( if (isSelected) "Selected " else "Removed ", " and ", ", "))
            spannable = SpannableStringBuilder(arrayOriginal.first())

            when(arraySpanText.size) {
                1 -> {
                    spannable.append(arraySpanText.first(), ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                2 -> {
                    spannable.append(arraySpanText.first(), ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.append(arrayOriginal[1])
                    spannable.append(arraySpanText[1], ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                3 -> {
                    spannable.append(arraySpanText.first(), ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.append(arrayOriginal[2])
                    spannable.append(arraySpanText[1], ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.append(arrayOriginal[1])
                    spannable.append(arraySpanText[2], ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                else -> {}
            }
        }

        return spannable
    }

    //************************************ Log Out ********************************************//
    fun logOut(){
        val dialog = PlansDialogFragment(ConstantTexts.ASK_LOG_OUT){
            BusyHelper.show(requireContext())
            UserWebService.logout{
                success, message ->
                BusyHelper.hide()
                if (success == true) {
                    AnalyticsManager.logEvent(AnalyticsManager.EventType.LOGOUT, UserInfo.userId)
                    PLANS_APP.gotoMainActivity(R.id.landingFragment)
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }


    //************************************ Chat Methods ***************************************//
    fun createChatGroup(list: ArrayList<UserModel>?, isBack: Boolean = true) {
        if (list.isNullOrEmpty()) return

        BusyHelper.show(requireContext())
        ChatService.createChatGroup(list){ chat, message ->
            BusyHelper.hide()
            if (chat != null) {
                if (isBack) {
                    gotoBack()
                }
                gotoChatting(chatModel = chat)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun updateChatGroup(chatId: String?, list: ArrayList<UserModel>?) {
        BusyHelper.show(requireContext())
        ChatService.updateChatGroup(chatId, list){ chat, message ->
            BusyHelper.hide()
            if (chat != null) {
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun muteChatNotify(
        chat: ChatModel? = null,
        isMute: Boolean = true,
        chatId: String? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {

        val chat_id = chat?.id ?: chatId ?: return
        ChatService.muteNotification(chat_id, isMute){ success, message ->
            if (success) {
                refreshAll(false)
            }else {
                ToastHelper.showMessage(message)
            }
            complete?.also { it(success, message) }
        }
    }

    fun updateChatHidden(
        chat: ChatModel? = null,
        isHidden: Boolean = true,
        chatId: String? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        val chat_id = chat?.id ?: chatId ?: run{
            complete?.also { it(false, "Invalid Params") }
            return
        }
        val dialog = PlansDialogFragment(ConstantTexts.ASK_CHAT_DELETE){
            ChatService.updateChatHidden(chat_id, isHidden){ success, message ->
                if (success) {
                    refreshAll(false)
                }else {
                    ToastHelper.showMessage(message)
                }
                complete?.also { it(success, message) }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun leaveChatWithAssignAdmin( chatId: String? ) {
        val chat_id = chatId ?: return
        val message = "Are you sure you want to leave this chat?\nYou will be required to assign another admin"

        val dialog = PlansDialogFragment(message){
            val action = NavDashboardDirections.actionGlobalAssignAdminForChatFragment(chat_id)
            navigate(directions = action)
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun removeUserInChat(
        chat: ChatModel? = null,
        user: UserModel? = null,
        chatId: String? = null,
        userId: String? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        val chat_id = chat?.id ?: chatId ?: return
        val user_id = user?.id ?: userId ?: return

        val fullName = (user ?: chat?.members?.firstOrNull { it.id == user_id })?.let {
            it.fullName ?: it.name ?: "${it.firstName ?: ""} ${it.lastName ?: ""}"
        } ?: "this user"

        val message = if (user_id == UserInfo.userId) "Are you sure you want to leave this chat?" else "Are you sure you want to remove $fullName?"

        val dialog = PlansDialogFragment(message){
            BusyHelper.show(requireContext())
            ChatService.removeUserInChat(user_id, chat_id) { newChat, message ->
                BusyHelper.hide()
                if (newChat != null) {
                    refreshAll()
                    complete?.also { it(true, null) }
                }else {
                    ToastHelper.showMessage(message)
                    complete?.also { it(false, message) }
                }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    fun copyTextToClipBoard(
        text: String?,
        title: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {

        val message = text?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also { it(false, "Invalid Params") }
            return
        }
        val label = title?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also { it(false, "Invalid Params") }
            return
        }

        (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager).also { clipManager ->
            ClipData.newPlainText(message, label).also {
                if (clipManager != null && it != null) {
                    clipManager.setPrimaryClip(it)
                    complete?.also { it(true, null) }
                }else {
                    complete?.also { it(false, "Failed to get the clipboard service") }
                }
            }
        }
    }



}