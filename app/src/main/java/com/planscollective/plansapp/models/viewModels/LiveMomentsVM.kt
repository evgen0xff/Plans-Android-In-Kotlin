package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService

class LiveMomentsVM(application: Application) : ListBaseVM(application) {

    var eventId: String? = null
    var eventModel: EventModel? = null
    var listUserLiveMoments = ArrayList<UserLiveMomentsModel>()

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int

    ) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        EventWebService.getEventDetails(eventId){ event, message ->
            BusyHelper.hide()
            if (event != null) {
                eventModel = event
                updateData(eventModel?.liveMoments)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUserLiveMoments.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    fun updateData(list: ArrayList<UserLiveMomentsModel>?, page: Int = 1, count: Int = 20){
        listUserLiveMoments.clear()
        list?.let {
            val search = if (keywordSearch.isEmpty()) it else {
                it.filter { it.user?.fullName?.lowercase()?.contains(keywordSearch.lowercase()) == true }
            }
            listUserLiveMoments.addAll(search)
        }
        didLoadData.value = true
    }

}