package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService

class HiddenEventsVM(application: Application) : ListBaseVM(application) {
    var listEvents = ArrayList<EventModel>()

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int ) {

        if (isShownLoading) {
            BusyHelper.show(context)
        }
        EventWebService.getEventList(count= 1000, type= "hidden", keyword= keywordSearch){ list, message ->
            BusyHelper.hide()
            if (list != null) {
                listEvents = list
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }
}