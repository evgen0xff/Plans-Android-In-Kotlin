package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService

class DetailsOfEventVM(application: Application) : ListBaseVM(application) {

    var eventId: String? = null
    var eventModel: EventModel? = null

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        EventWebService.getEventDetails(eventId, pageNumber, count){ event, message ->
            BusyHelper.hide()
            if (event != null) {
                updateData(event, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
    }

    private fun updateData(event: EventModel?, page: Int = 1, count: Int = 20){
        eventModel = event
    }

}