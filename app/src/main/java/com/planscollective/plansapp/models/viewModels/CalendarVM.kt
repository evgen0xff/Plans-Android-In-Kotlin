package com.planscollective.plansapp.models.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.threeten.bp.LocalDate

class CalendarVM(application: Application) : ListBaseVM(application) {

    var selectedDate = MutableLiveData<CalendarDay>()
    var listEventsOfMonth = ArrayList<EventModel>()
    var listEventsOfSelectedDay = ArrayList<EventModel>()
    var listSearchedEvents = ArrayList<EventModel>()
    var listDaysOfEvents = ArrayList<CalendarDay>()

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int

    ) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }

        val curDate = (selectedDate.value ?: CalendarDay.today()).date
        val startTime : Long = curDate.getStartTimeOfMonth()
        val endTime : Long = curDate.getEndTimeOfMonth()

        EventWebService.getEventsForCalendar(startTime, endTime){ list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateData(list)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }



    private fun updateData(list: ArrayList<EventModel>?){
        listEventsOfMonth = list?.sortedByDescending { it.startTime?.toLong() }?.toArrayList() ?: return
        listDaysOfEvents.clear()
        listEventsOfMonth.forEach { it ->
            var startDay = it.startTime?.toLocalDate() ?: return@forEach
            val endDay = it.endTime?.toLocalDate() ?: return@forEach
            while (startDay <= endDay) {
                val calendarDay = CalendarDay.from(startDay)
                if (!listDaysOfEvents.any { it.date == startDay }) {
                    listDaysOfEvents.add(calendarDay)
                }
                startDay = startDay.plusDays(1)
            }
        }

        listEventsOfSelectedDay.clear()
        listEventsOfSelectedDay = listEventsOfMonth.filter {
            val startDay = it.startTime?.toLocalDate() ?: return@filter false
            val endDay = it.endTime?.toLocalDate() ?: return@filter false
            val selectedDay = selectedDate.value?.date ?: return@filter false
            if (selectedDay < startDay || selectedDay > endDay) return@filter false
            return@filter true
        }.toArrayList()

        searchEvents()
    }

    fun searchEvents() {
        listSearchedEvents.clear()
        if (keywordSearch.trim().isNotEmpty()){
            listSearchedEvents = listEventsOfSelectedDay.filter {
                it.eventName?.lowercase()?.contains(keywordSearch.trim().lowercase()) ?: false
            }.toArrayList()
        }else {
            listSearchedEvents.addAll(listEventsOfSelectedDay)
        }

        didLoadData.value = true
    }

    fun getSelectedDayText() : String? {
        var strSelectedDay = selectedDate.value?.date?.toFormatString("EE, MMM dd") ?: ""
        val today = LocalDate.now()
        if (today == selectedDate.value?.date) {
            strSelectedDay = "Today - $strSelectedDay"
        }else if (today.plusDays(1) == selectedDate.value?.date) {
            strSelectedDay = "Tomorrow - $strSelectedDay"
        }else if (today.minusDays(1) == selectedDate.value?.date){
            strSelectedDay = "Yesterday - $strSelectedDay"
        }

        return strSelectedDay
    }

}