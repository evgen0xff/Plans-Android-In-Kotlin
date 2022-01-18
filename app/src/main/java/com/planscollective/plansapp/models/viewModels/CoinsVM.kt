package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.user.UserWebService

class CoinsVM(application: Application) : PlansBaseVM(application) {
    var userId: String? = null
    var user: UserModel? = null
    var userName: String? = null
    var coinNumber: Int = 0
    var arrayStars = arrayListOf<Int>()

    fun getUserInfo(isShownLoading: Boolean = false) {
        if (user != null) {
            updateUserInfo(user)
        }else {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            UserWebService.getUserProfile(userId) { userInfo, message ->
                BusyHelper.hide()
                if (userInfo != null) {
                    updateUserInfo(userInfo)
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }
    }

    private fun updateUserInfo(userInfo: UserModel?) {
        user = userInfo
        userName = user?.fullName ?: user?.name ?: "${user?.firstName ?: ""} ${user?.lastName ?: ""}"
        coinNumber = user?.coinNumber ?: 0

        arrayStars.clear()
        for (i in 0 until Constants.arrayStarsLarge.size ) {
            if (i < coinNumber) {
                arrayStars.add(Constants.arrayStarsLarge[i])
            }else {
                arrayStars.add(R.drawable.ic_lock_circle_green_lagre)
            }
        }

        didLoadData.value = true
    }

}