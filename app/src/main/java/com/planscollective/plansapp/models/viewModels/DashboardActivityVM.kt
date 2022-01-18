package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.liveMoment.LiveMomentWebService

class DashboardActivityVM(application: Application) : PlansBaseVM(application) {

    var activityEvent: EventModel? = null
    var activityUser: UserModel? = null
    var userMe: UserModel? = null

    fun createLiveMoment(eventId: String?,
                         photoFilePath: String? = null,
                         videoFilePath: String? = null,
                         caption: String? = null) {

        val mediaType = (if (!photoFilePath.isNullOrEmpty()) "image" else if (!videoFilePath.isNullOrEmpty()) "video" else null) ?: return
        val msgSuccess = "Your live moment was posted."

        ToastHelper.showLoadingAlerts(ToastHelper.LoadingToastType.POSTING)
        LiveMomentWebService.createLiveMoment(eventId, caption, mediaType, photoFilePath ?: videoFilePath){
            success, message ->
            ToastHelper.hideLoadingAlerts(ToastHelper.LoadingToastType.POSTING)
            val msg = if (success) msgSuccess else message
            ToastHelper.showMessage(msg)
            PLANS_APP.currentFragment?.refreshAll(false)
            AnalyticsManager.logEvent(AnalyticsManager.EventType.STORY_ADD)
        }
    }

}