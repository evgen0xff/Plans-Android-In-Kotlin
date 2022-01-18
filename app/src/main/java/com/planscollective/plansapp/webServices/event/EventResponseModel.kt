package com.planscollective.plansapp.webServices.event

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.base.ErrorResponseModel

class EventListResponseModel {
    @Expose
    @SerializedName("message")
    var message: String? = null

    @Expose
    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @Expose
    @SerializedName("isEvent")
    var hasEvent: Boolean? = null

    @Expose
    @SerializedName("result")
    var result: ArrayList<EventModel>? = null

    fun getEventList () : ArrayList<EventModel>? = result?.onEach { it.prepare() }
}

class EventDetailsResponseModel {
    @Expose
    @SerializedName("currentTime")
    var currentTime: Long? = null

    @Expose
    @SerializedName("message")
    var message: String? = null

    @Expose
    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @Expose
    @SerializedName("eventsList")
    var details: EventModel? = null

    @Expose
    @SerializedName("data")
    var data: EventModel? = null

    fun getEventDetails() : EventModel? = details?.prepare()
}

class PeopleResponseModel {
    @Expose
    @SerializedName("message")
    var message: String? = null

    @Expose
    @SerializedName("error")
    var error: ErrorResponseModel? = null

    @Expose
    @SerializedName("count")
    var count: CountResponseModel? = null

    @Expose
    @SerializedName("eventData")
    var event: EventModel? = null

    @Expose
    @SerializedName("people")
    var people: ArrayList<UserModel>? = null

    fun prepare() : PeopleResponseModel {
        event?.prepare()
        return this
    }
}

class CountResponseModel {
    @Expose
    @SerializedName("goingCnt")
    var countGoing: Int? = null

    @Expose
    @SerializedName("invitedCnt")
    var countInvited: Int? = null

    @Expose
    @SerializedName("liveCnt")
    var countLive: Int? = null

    @Expose
    @SerializedName("maybeCnt")
    var countMaybe: Int? = null
    @Expose
    @SerializedName("nextTimeCnt")
    var countNextTime: Int? = null
}

class InvitationsBody {
    @Expose
    @SerializedName("eventId")
    var eventId: String? = null

    @Expose
    @SerializedName("people")
    var people: ArrayList<InvitationModel>? = null
}

