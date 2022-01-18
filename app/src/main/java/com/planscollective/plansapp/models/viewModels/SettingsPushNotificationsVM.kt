package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.SettingOptionModel
import com.planscollective.plansapp.models.dataModels.SettingsModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.settings.SettingsWebService

class SettingsPushNotificationsVM(application: Application) : ListBaseVM(application) {

    var settings: SettingsModel? = null
    var isOnline = true

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int
    ) {
        if(isShownLoading)
            BusyHelper.show(context)

        SettingsWebService.getSettings { settings, message ->
            BusyHelper.hide()
            if (settings != null) {
                this.settings = settings
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }

    fun updateSettings(option: SettingOptionModel?, isCheck: Boolean = true) {
        SettingsWebService.updateSettings(option, isCheck) { settings, message ->
            if (settings != null) {
                this.settings = settings
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }

}