package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.fragment.users.FriendsSelectionFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.friend.FriendWebService

class FriendsSelectionVM(application: Application) : ListBaseVM(application) {

    var chatId: String? = null
    var userId = UserInfo.userId
    var type = FriendsSelectionFragment.SelectType.CHAT_START

    var listFriends = ArrayList<UserModel>()
    var listSelectedAlready = ArrayList<UserModel>()

    var listAvailables = ArrayList<UserModel>()
    var listSelected =  ArrayList<UserModel>()

    override var numberOfRows : Int = 20

    fun initializeData(
        type: String = FriendsSelectionFragment.SelectType.CHAT_START.name,
        usersSelected: Array<UserModel>? = null,
        chatId: String? = null,
    ) {
        this.type = FriendsSelectionFragment.SelectType.valueOf(type)
        usersSelected?.takeIf { it.isNotEmpty() }?.also {
            listSelectedAlready.clear()
            listSelectedAlready.addAll(it)
        }
        this.chatId = chatId
    }

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int
    ) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        FriendWebService.getFriendList(pageNumber, count, keywordSearch, userId) {
            list, message ->
            BusyHelper.hide()
            if (list != null) {
                val userList = ArrayList<UserModel>().apply { addAll(list)}
                updateData(userList, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listFriends.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<UserModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listFriends.clear()
        }
        listFriends.replace(list, page, count)

        listAvailables = listFriends.filter { friend -> !listSelectedAlready.any{ it.id == friend.id} }.toArrayList()
        listSelected = listAvailables.filter { item -> listSelected.any { it.id == item.id } }.toArrayList()

        didLoadData.value = true
    }

    fun selectUser(user: UserModel?) {
        val selected = user ?: return
        if (listSelected.any { it.id == selected.id })
            listSelected.removeAll { it.id == selected.id }
        else
            listSelected.add(selected)

        didLoadData.value = true
    }

}