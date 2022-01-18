package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.isPasswordValid
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class ChangePasswordVM(application: Application) : PlansBaseVM(application) {
    var currentPassword: String? = null
    var newPassword: String? = null
    var confirmPassword: String? = null

    fun validate() : Boolean {
        var message: String? = null
        val result = if (currentPassword?.isPasswordValid() == false) {
            message = "Password " + ConstantTexts.ALERT_VALID_PASSWORD
            false
        }else if (newPassword?.isPasswordValid() == false){
            message = "New password " + ConstantTexts.ALERT_VALID_PASSWORD
            false
        }else if (newPassword != confirmPassword){
            message = ConstantTexts.ALERT_PASSWORD_NOT_MATCH
            false
        } else true

        if (!result) {
            ToastHelper.showMessage(message)
        }

        return result
    }

    fun isNotEmpty() : Boolean {
        return !currentPassword.isNullOrEmpty() && currentPassword!!.length > 7 && !newPassword.isNullOrEmpty() && !confirmPassword.isNullOrEmpty()
    }

}