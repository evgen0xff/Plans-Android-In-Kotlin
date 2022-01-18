package com.planscollective.plansapp.interfaces

import android.util.Size
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.models.dataModels.*
import com.planscollective.plansapp.models.viewModels.EventType

interface PlansActionListener {
    fun onClickedEvent(event: EventModel? = null, eventId: String? = null){}
    fun onClickedUser(user: UserModel?, event: EventModel? = null){}
    fun onClickedJoin(event: EventModel?){}
    fun onClickedUnjoin(event: EventModel?){}
    fun onClickedMoreMenu(event: EventModel?, menuType: MenuOptionHelper.MenuType = MenuOptionHelper.MenuType.EVENT_FEED){}
    fun onClickedMoreMenuSavedEvent(event: EventModel?){}
    fun onClickedMoreUsers(event: EventModel?){}
    fun onClickedDateTime(event: EventModel?){}
    fun onClickedLocation(event: EventModel?){}
    fun onClickedHiddenEvents(){}
    fun onClickedBack(){}
    fun onClickEditEvent(event: EventModel?){}
    fun onClickInviteUser(event: EventModel?){}
    fun onClickDetailsOfEvent(event: EventModel?){}
    fun onClickChatOfEvent(event: EventModel?){}
    fun onClickGuestAction(actionName: String?, event: EventModel?){}
    fun onClickAddLiveMoment(event: EventModel?){}
    fun onClickWatchUserLiveMoments(userLiveMoments: UserLiveMomentsModel? = null, event: EventModel? = null){}
    fun onClickShowAllLiveMoments(event: EventModel?){}
    fun onClickPost(post: PostModel? = null, event: EventModel? = null, postId: String? = null, eventId: String? = null){}
    fun onClickMoreMenuForContent(content: PostModel?, post: PostModel? = null, event: EventModel? = null){}
    fun onClickOpenPhoto(urlPhoto: String?, data: Any? = null){}
    fun onClickPlayVideo(urlVideo: String?, data: Any? = null){}
    fun onClickLikeContent(content: PostModel?, post:PostModel? = null, event: EventModel? = null, isLike: Boolean = true){}
    fun onClickLikePost(postId: String?, eventId:String? = null, isLike: Boolean = true){}
    fun onClickMoreUsersLiked(post: PostModel?, event: EventModel? = null){}
    fun onClickedMoreMenuUser(user: UserModel?, data: Any? = null){}
    fun onClickedFriendAction(user: UserModel?, action: String? = null){}
    fun onClickedChat(user: UserModel?){}
    fun onClickedSelect(user: InvitationModel?): Int?{ return null}
    fun onClickedSelected(user: InvitationModel?): Int?{ return null}
    fun onClickedInvited(user: InvitationModel?, complete: ((toPosition: Int?) -> Unit)? = null){}
    fun onClickedMenuInvite(user: InvitationModel?){}
    fun onClickedSettings(user: UserModel? = null){}
    fun onClickedUserImage(user: UserModel?){}
    fun onClickedCoinStar(user: UserModel?){}
    fun onClickedFriends(user: UserModel?){}
    fun onClickedEventsTab(type: EventType, user: UserModel? = null){}
    fun onClickedCreateEvent(){}
    fun onViewSize(size: Size?){}
    fun onChangedSettingOption(option: SettingOptionModel?, isCheck: Boolean = true){}
    fun onClickedEventInvitations(){}
    fun onClickedFriendRequests(){}
    fun onLongClickNotification(notify: NotificationActivityModel?){}
    fun onClickNotification(notify: NotificationActivityModel?){}
    fun onClickEventInvitationAction(actionName: String?, event: EventModel?){}
    fun onClickChatListItem(chatModel: ChatModel?){}
    fun onLongClickChatListItem(chatModel: ChatModel?){}
    fun onSelectedUser(user: UserModel?) {}

}