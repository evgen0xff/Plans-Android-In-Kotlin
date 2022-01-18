package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.user.UserWebService

class EditProfileVM(application: Application) : PlansBaseVM(application) {

    var userId: String? = UserInfo.userId
    var user: UserModel? = null
    var userOld: UserModel? = null

    fun getUserProfile(isShownLoading: Boolean) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        UserWebService.getUserProfile(userId) { userModel, message ->
            BusyHelper.hide()
            if (userModel != null) {
                updateData(userModel)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun updateData(userModel: UserModel?) {
        userOld = userModel
        user = UserModel().apply {
            id = userOld?.id
            firstName = userOld?.firstName
            lastName = userOld?.lastName
            bio = userOld?.bio
            email = userOld?.email
            mobile = userOld?.mobile
            userLocation = userOld?.userLocation
            lat = userOld?.lat
            long = userOld?.long
            profileImage = userOld?.profileImage
        }
        didLoadData.value = true
    }

    fun validateUserInfo() : Boolean {
        var result = true
        if (user?.firstName.isNullOrEmpty() || user?.lastName.isNullOrEmpty()) {
            result = false
        }else if (user?.firstName == userOld?.firstName &&
                    user?.lastName == userOld?.lastName &&
                    user?.bio == userOld?.bio &&
                    user?.userLocation == userOld?.userLocation &&
                    user?.profileImage == userOld?.profileImage
                ){
            result = false
        }

        return result
    }

}