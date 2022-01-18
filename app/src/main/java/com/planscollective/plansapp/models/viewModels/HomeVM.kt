package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService

class HomeVM(application: Application) : ListBaseVM(application) {
    var listEvents = ArrayList<EventModel>()
    var listEventsHide = ArrayList<EventModel>()

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }

        EventWebService.getEventList(pageNumber, count, type = "all"){ list, message ->
            if (list != null) {
                EventWebService.getEventList(count = 1000, type = "hidden", keyword= keywordSearch) {listHidden, msg ->
                    BusyHelper.hide()
                    if (listHidden != null) {
                        listEventsHide = listHidden
                        updateData(list, pageNumber, count)
                    }else {
                        ToastHelper.showMessage(msg)
                    }
                    didLoadData.value = true
                }
            }else {
                BusyHelper.hide()
                ToastHelper.showMessage(message)
                didLoadData.value = true
            }
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
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