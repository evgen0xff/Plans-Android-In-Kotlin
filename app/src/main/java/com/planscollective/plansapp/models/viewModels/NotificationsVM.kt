package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.dataModels.NotificationsModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.notifications.NotificationsWebService

class NotificationsVM(application: Application) : ListBaseVM(application) {

    override var numberOfRows: Int = 20
    var notifications: NotificationsModel? = null

    //********************************** Notifications *****************************************//

    fun deleteNotification(notify: NotificationActivityModel?) {
        BusyHelper.show(context)
        NotificationsWebService.deleteNotification(notify?.id){ notifications, message ->
            BusyHelper.hide()
            if (notifications != null) {
                getAllList()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun updateNotifications(model: NotificationsModel?, pageNumber: Int = 1, count: Int = 20) {
        notifications = model
        didLoadData.value = true
    }

    private fun getNotifications(isShownLoading: Boolean,
                                 pageNumber: Int,
                                 count: Int) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }

        NotificationsWebService.getNotifications(1, pageNumber * count){ notifications, message ->
            BusyHelper.hide()
            if (notifications != null) {
                updateNotifications(notifications, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
        }

    }


    //********************************* Common Methods *****************************************//

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int) {
        getNotifications(isShownLoading, pageNumber, count)
    }

    override fun getNextPage(context: Context,
                         isShownLoading: Boolean){
        pageNumber = ((notifications?.listActivities?.size ?: 0) / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

}