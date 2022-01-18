package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService

class EventInvitationsVM(application: Application) : ListBaseVM(application) {

    var listEvents = ArrayList<EventModel>()

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int
    ) {
        if(isShownLoading)
            BusyHelper.show(context)

        EventWebService.getEventList(pageNumber, count, "invitation") {
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
        pageNumber = (listEvents.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    private fun updateData(list: ArrayList<EventModel>?, page: Int = 1, count: Int = 20){
        if (page == 1) {
            listEvents.clear()
        }
        listEvents.replace(list, page, count)
    }

}