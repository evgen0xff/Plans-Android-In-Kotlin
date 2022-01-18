package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.friend.FriendWebService

class FriendsListVM(application: Application) : ListBaseVM(application) {

    var userId: String? = null
    var user: UserModel? = null
    var listUsers = ArrayList<UserModel>()
    override var numberOfRows : Int = 20

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
        pageNumber = (listUsers.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<UserModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listUsers.clear()
        }
        listUsers.replace(list, page, count)
        didLoadData.value = true
    }

}