package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class CreateEventVM(application: Application) : PlansBaseVM(application) {

    var eventModel: EventModel = EventModel()
    val boundaries = arrayListOf("300", "400", "500", "600", "700", "800", "900", "1000", "1100", "1200", "1300", "1400", "1500")

    var selectedBoundaryItem : Int = 0
        get() {
            return boundaries.indexOfFirst { it == eventModel.checkInRange?.toString() }.takeIf{ it >= 0 } ?: 0
        }
        set(value) {
            field = value
            eventModel.checkInRange = boundaries[value].toInt()
        }

    fun getEventWith() : EventModel {
        val event = eventModel

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

    fun initialize(place: PlaceModel? = null) {
        if (eventModel.userId.isNullOrEmpty()) {
            eventModel.userId = UserInfo.userId
        }

        if (eventModel.address.isNullOrEmpty()) {
            eventModel.address = place?.address
        }

        if (eventModel.locationName.isNullOrEmpty()) {
            eventModel.locationName = place?.name
        }

        if (eventModel.lat == null || eventModel.long == null) {
            eventModel.lat = place?.latitude
            eventModel.long = place?.longitude
        }

    }



}