package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.friend.FriendWebService

class UsersBlockedVM(application: Application) : ListBaseVM(application) {

    var listUsers = ArrayList<UserModel>()

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int
    ) {
        if(isShownLoading)
            BusyHelper.show(context)

        FriendWebService.getBlockedUsers(true, pageNumber, count) {
            list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateData(list, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value =true
        }
    }


    override fun getNextPage(context: Context,
                         isShownLoading: Boolean){
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUsers.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<UserModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listUsers.clear()
        }
        listUsers.replace(list, page, count)
    }

}