package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import java.util.*

class EditEventVM(application: Application) : ListBaseVM(application) {

    var eventId: String? = null
    var isDuplicate: Boolean = false
    var eventModel: EventModel? = null
    val boundaries = arrayListOf("300", "400", "500", "600", "700", "800", "900", "1000", "1100", "1200", "1300", "1400", "1500")
    var selectedBoundaryItem : Int = 0
        get() {
            return boundaries.indexOfFirst { it == eventModel?.checkInRange?.toString() }.takeIf{ it >= 0 } ?: 0
        }
        set(value) {
            field = value
            eventModel?.checkInRange = boundaries[value].toInt()
        }
    var isNewMedia = false

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {
        if (eventModel == null) {
            if (isShownLoading) {
                BusyHelper.show(context)
            }
            EventWebService.getEventDetails(eventId, pageNumber, count){ event, message ->
                BusyHelper.hide()
                if (event != null) {
                    initializeData(event)
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }else {
            initializeData(eventModel)
        }
    }

    private fun initializeData(event: EventModel?){
        if (didLoadData.value != true) {
            eventModel = event
            eventModel?.invitations = event?.invitations?.filter{it.isFriend == true}?.toArrayList()
            if (event?.checkInRange == null || !boundaries.any { it == event.checkInRange?.toString() }) {
                eventModel?.checkInRange = boundaries.firstOrNull()?.toInt()
            }
            didLoadData.value = true
        }
    }

    fun getEventWith() : EventModel? {
        val event = eventModel ?: return null

        val eventNew = EventModel()
        eventNew.eventId                = event.id
        eventNew.eventName              = event.eventName
        eventNew.details                = event.details
        eventNew.userId                 = event.userId
        eventNew.address                = event.address
        eventNew.locationName           = event.locationName
        eventNew.long                   = event.long
        eventNew.lat                    = event.lat
        eventNew.caption                = event.caption
        eventNew.startDate              = event.startDate
        eventNew.startTime              = event.startTime
        eventNew.endDate                = event.endDate
        eventNew.endTime                = event.endTime
        eventNew.checkInRange           = event.checkInRange
        eventNew.isPublic               = event.isPublic
        eventNew.isGroupChatOn          = event.isGroupChatOn
        eventNew.invitedPeople          = event.invitedPeople
        eventNew.isCancel               = false
        eventNew.friendsContactNumbers  = event.invitations?.map { it.mobile }?.joinToString(",")
        eventNew.mediaType              = event.mediaType
        eventNew.imageOrVideo           = event.imageOrVideo
        eventNew.thumbnail              = event.thumbnail

        return eventNew
    }

    fun checkValidData(isAlert: Boolean = false): Boolean {
        var message: String? = null

        // Event Name
        if (eventModel?.eventName.isNullOrEmpty()) {
            message = ConstantTexts.YOUR_EVENT_NEED_EVENT_NAME
        }else {
            // Start Time
            val isCheckTime = isDuplicate || when(eventModel?.eventStatus) {
                EventModel.EventStatus.ENDED, EventModel.EventStatus.CANCELLED, EventModel.EventStatus.EXPIRED -> true
                else -> false
            }

            if (isCheckTime) {
                val now = Date().time / 1000
                val start = eventModel?.startTime?.toLong() ?: 0
                val end = eventModel?.endTime?.toLong() ?: 0

                if (start < now && end < now) {
                    message = ConstantTexts.EVENT_CANNOT_IN_PAST
                }else if (start < now) {
                    message = ConstantTexts.START_TIME_CANNOT_AFTER_END_TIME
                }
            }
        }

        if (isAlert && !message.isNullOrEmpty()) {
            ToastHelper.showMessage(message)
        }

        return message.isNullOrEmpty()
    }



}