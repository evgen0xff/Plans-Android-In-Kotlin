package com.planscollective.plansapp.helper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.fragment.utils.PlansBottomDialogFragment
import com.planscollective.plansapp.interfaces.OnSelectedMenuItem
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.*

object MenuOptionHelper {

    enum class MenuType {
        NONE,
        EVENT_FEED,
        EVENT_DETAILS,
        EVENT_JOIN,
        EVENT_LEAVE,
        EVENT_PENDING,
        EVENT_SAVED,
        POST_COMMENT,
        LIVE_MOMENT,
        PEOPLE_INVITED,
        USER_PROFILE,
        CHAT,
    }

    fun showBottomMenu(list: ArrayList<MenuModel>?,
                       fragmentManager: FragmentManager,
                       listener: OnSelectedMenuItem? = null,
                       data: Any? = null
    ) {
        list?.apply {
            val bottomSheet = PlansBottomDialogFragment(this, listener, data)
            bottomSheet.show(fragmentManager, PlansBottomDialogFragment.TAG)
        }
    }

    fun showBottomMenu(list: ArrayList<MenuModel>?,
                       activity: FragmentActivity,
                       listener: OnSelectedMenuItem? = null,
                       data: Any? = null
    ) {
        showBottomMenu(list, activity.supportFragmentManager, listener, data)
    }

    fun showBottomMenu(list: ArrayList<MenuModel>?,
                       fragment: Fragment,
                       listener: OnSelectedMenuItem? = null,
                       data: Any? = null
    ) {
        showBottomMenu(list, fragment.requireActivity(), listener, data)
    }

    fun showPlansMenu(data: Any? = null,
                      menuType: MenuType = MenuType.NONE,
                      fragment: Fragment,
                      listener: OnSelectedMenuItem? = null
    ) {
        val list = getMenuItems(data, menuType)?.takeIf { it.size > 0 }  ?: return
        showBottomMenu(list, fragment, listener, data)
    }

    private fun getMenuItems(data: Any? = null, menuType: MenuType = MenuType.NONE ) : ArrayList<MenuModel>? {
        return when(menuType) {
            MenuType.EVENT_FEED, MenuType.EVENT_DETAILS -> {
                getMenuItemsForEvent(data as? EventModel, menuType)
            }
            MenuType.EVENT_JOIN -> {
                getMenuItemsForJoinEvent(data as? EventModel)
            }
            MenuType.EVENT_LEAVE -> {
                getMenuItemsForLeaveEvent(data as? EventModel)
            }
            MenuType.EVENT_PENDING -> {
                getMenuItemsForPendingEvent(data as? EventModel)
            }
            MenuType.EVENT_SAVED -> {
                getMenuItemsForSavedEvent(data as? EventModel)
            }
            MenuType.POST_COMMENT -> {
                getMenuItemsForPostComment(data as? HashMap<*, *>)
            }
            MenuType.LIVE_MOMENT -> {
                getMenuItemsForLiveMoment(data as? HashMap<*, *>)
            }
            MenuType.PEOPLE_INVITED -> {
                getMenuItemsForPeopleInvited(data as? UserModel)
            }
            MenuType.USER_PROFILE -> {
                getMenuItemsForUserProfile(data as? UserModel)
            }
            MenuType.CHAT -> {
                (data as? ChatModel)?.getMenuOptions()
            }
            else -> {
                null
            }
        }
    }

    private fun getMenuItemsForUserProfile(user: UserModel?) : ArrayList<MenuModel>? {
        if (user?.id == UserInfo.userId) return null
        val list = ArrayList<MenuModel>()

        user?.friendShipStatus?.takeIf{ it != 5}?.also {
            list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Block User"))
        }
        list.add(MenuModel(R.drawable.ic_message_circle, "Send Message"))
        list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))

        return list
    }

    private fun getMenuItemsForPeopleInvited(user: UserModel?) : ArrayList<MenuModel>? {
        if (user?.id == UserInfo.userId) return null
        val list = ArrayList<MenuModel>()
        list.add(MenuModel(R.drawable.ic_trash_black_menu, "Remove Guest"))

        return list
    }

    private fun getMenuItemsForLiveMoment(data: HashMap<*, *>?) : ArrayList<MenuModel>? {
        val event = data?.get("event") as? EventModel ?: return null
        val userLiveMoment = data?.get("userLiveMoment") as? UserLiveMomentsModel ?: return null

        val list = ArrayList<MenuModel>()
        val isMine = userLiveMoment?.user?.id == UserInfo.userId
        val isEventHost = event?.userId == UserInfo.userId

        if (!isMine) {
            list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
        }

        if (isEventHost || isMine ) {
            list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete"))
        }
        return list
    }

    private fun getMenuItemsForPostComment(data: HashMap<*, *>?) : ArrayList<MenuModel>?{
        val content = data?.get("content") as? PostModel ?: return null
        val event = data?.get("event") as? EventModel ?: return null

        val list = ArrayList<MenuModel>()

        list.add(MenuModel(R.drawable.ic_share_black_menu,"Share"))

        if (content.isMediaType) {
            list.add(MenuModel(R.drawable.ic_download_black, "Download"))
        }

        if (UserInfo.userId != content.user?.id){
            list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
        }

        if (UserInfo.userId == event.userId || UserInfo.userId == content.user?.id) {
            list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete"))
        }
        return list
    }

    private fun getMenuItemsForPendingEvent(eventModel: EventModel?) : ArrayList<MenuModel>?{
        val event = eventModel ?: return null
        val list = ArrayList<MenuModel>()
        when (event.statusJoin) {
            1 -> { // Pending Invite
                list.add(MenuModel(R.drawable.ic_check_circle_black_menu, "Going"))
                list.add(MenuModel(R.drawable.ic_clock_black_menu, "Maybe"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Next Time"))
            }
            2 -> { // Going
                list.add(MenuModel(R.drawable.ic_clock_black_menu, "Maybe"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Next Time"))
            }
            3 -> { // Maybe
                list.add(MenuModel(R.drawable.ic_check_circle_black_menu, "Going"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Next Time"))
            }
            4 -> { // Next Time
                list.add(MenuModel(R.drawable.ic_check_circle_black_menu, "Going"))
                list.add(MenuModel(R.drawable.ic_clock_black_menu, "Maybe"))
            }
            else -> {}
        }
        return list
    }

    private fun getMenuItemsForLeaveEvent(eventModel: EventModel?) : ArrayList<MenuModel>?{
        val event = eventModel ?: return null
        val list = ArrayList<MenuModel>()
        list.add(MenuModel(R.drawable.ic_log_out_black_menu, "Leave Event"))
        return list
    }


    private fun getMenuItemsForJoinEvent(eventModel: EventModel?) : ArrayList<MenuModel>?{
        val event = eventModel ?: return null
        val list = ArrayList<MenuModel>()
        if (event.isJoin == true) {
            list.add(MenuModel(R.drawable.ic_log_out_black_menu, "Unjoin"))
        }else {
            list.add(MenuModel(R.drawable.ic_plus_black, "Join"))
        }
        return list
    }

    fun getMenuItemsForEvent(eventModel: EventModel?, menuType: MenuType) : ArrayList<MenuModel>? {
        val event = eventModel ?: return null
        val userId = UserInfo.userId ?: return null
        val ownerId = event.eventCreatedBy?.id ?: return null

        val itemNotify = if (!event.isTurnOffNoti(userId)){
            MenuModel(R.drawable.ic_bell_off_black_menu, "Mute Notifications")
        }else {
            MenuModel(R.drawable.ic_bell_off_black_menu, "Unmute Notifications")
        }

        val list = ArrayList<MenuModel>()

        // My own Event
        if (userId == ownerId) {

            val itemPost = if (event.isPosting == false){
                MenuModel(R.drawable.ic_x_black_menu, "Turn On Posting")
            } else {
                MenuModel(R.drawable.ic_x_black_menu,  "Turn Off Posting")
            }

            if (event.isLive == 1) {  // Lived Event
                list.add(MenuModel(R.drawable.ic_edit_black_menu, "Edit Event"))
                list.add(itemPost)
                list.add(itemNotify)
                list.add(MenuModel(R.drawable.ic_calendar_black, "Add to Calendar"))
                list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                list.add(MenuModel(R.drawable.ic_duplicate_black_menu, "Duplicate Event"))
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Event"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "End Event"))
            }else if (event.isEnded == 1) {  // Ended Event
                list.add(itemPost)
                list.add(itemNotify)
                list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                list.add(MenuModel(R.drawable.ic_duplicate_black_menu, "Duplicate Event"))
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Event"))
            }else if (event.isCancel == true) { // Canceled Event
                list.add(MenuModel(R.drawable.ic_edit_black_menu, "Update Event"))
                list.add(MenuModel(R.drawable.ic_duplicate_black_menu, "Duplicate Event"))
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Event"))
            }else if (event.isExpired == true) { // Expired Event
                list.add(MenuModel(R.drawable.ic_edit_black_menu, "Update Event"))
                list.add(MenuModel(R.drawable.ic_duplicate_black_menu, "Duplicate Event"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Cancel Event"))
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Event"))
            }else {
                list.add(MenuModel(R.drawable.ic_edit_black_menu, "Edit Event"))
                list.add(itemPost)
                list.add(itemNotify)
                list.add(MenuModel(R.drawable.ic_calendar_black, "Add to Calendar"))
                list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                list.add(MenuModel(R.drawable.ic_duplicate_black_menu, "Duplicate Event"))
                list.add(MenuModel(R.drawable.ic_x_circle_black_menu, "Cancel Event"))
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Event"))
            }

        // Other Event
        }else {
            val itemSave = if(event.isSaved == true){
                MenuModel(R.drawable.ic_save_black_menu, "Unsave Event")
            } else {
                MenuModel(R.drawable.ic_save_black_menu, "Save Event")
            }
            val itemHide = if (event.isHide == true){
                MenuModel(R.drawable.ic_eye_off_black_menu, "Unhide Event")
            }else {
                MenuModel(R.drawable.ic_eye_off_black_menu, "Hide Event")
            }

            // Joined Event
            if (event.isJoin == true) {
                if (event.isLive == 1) {
                    list.add(itemSave)
                    list.add(itemHide)
                    if (menuType == MenuType.EVENT_FEED) {
                        list.add(MenuModel(R.drawable.ic_log_out_black_menu, "Leave Event"))
                    }
                    list.add(itemNotify)
                    list.add(MenuModel(R.drawable.ic_calendar_black, "Add to Calendar"))
                    list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                    list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
                }else if (event.isCancel == true || event.isExpired == true) {
                    list.add(itemSave)
                    list.add(itemHide)
                    list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
                }else {
                    list.add(itemSave)
                    list.add(itemHide)
                    list.add(itemNotify)
                    if (event.isEnded != 1) {
                        list.add(MenuModel(R.drawable.ic_calendar_black, "Add to Calendar"))
                    }
                    list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                    list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
                }
                // Unjoined Event
            }else if (event.isCancel == true || event.isExpired == true) {
                list.add(itemSave)
                list.add(itemHide)
                list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
            }else {
                list.add(itemSave)
                list.add(itemHide)
                if (event.isEnded != 1) {
                    list.add(MenuModel(R.drawable.ic_calendar_black, "Add to Calendar"))
                }
                list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))
                list.add(MenuModel(R.drawable.ic_alert_octagon_black_menu, "Report"))
            }
        }

        return list
    }

    fun getMenuItemsForSavedEvent(eventModel: EventModel?) : ArrayList<MenuModel>? {
        val event = eventModel ?: return null

        val list = ArrayList<MenuModel>()
        val itemSave = if(event.isSaved == true){
            MenuModel(R.drawable.ic_save_black_menu, "Unsave Event")
        } else {
            MenuModel(R.drawable.ic_save_black_menu, "Save Event")
        }
        list.add(itemSave)
        list.add(MenuModel(R.drawable.ic_share_black_menu, "Share Event"))

        return list
    }



}