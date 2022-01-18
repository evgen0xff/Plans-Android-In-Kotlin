package com.planscollective.plansapp.webServices.event

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.getRequestBody
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ErrorResponseModel
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object EventWebService : BaseWebService() {

    private var webService = WebServiceBuilder.buildService(EventApis::class.java)

    fun createEvent(
        event: EventModel?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            complete?.also{ it(false, "Failed to create event, invalid access token") }
            return
        }

        val map = event?.toMap()
        var fileBody : MultipartBody.Part? = null
        event?.imageOrVideo?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("imageOrVideo", file.name, requestFile)
            }
        }

        webService.createEvent(accessToken, map, fileBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                complete?.also{ it(false, "Failed to create event") }
            }
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.also{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to create event")
                    }
                }
            }
        })
    }

    fun updateEvent(
        event: EventModel?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            complete?.also{ it(false, "Failed to update event, invalid access token") }
            return
        }

        val map = event?.toMap()
        var fileBody : MultipartBody.Part? = null
        event?.imageOrVideo?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("imageOrVideo", file.name, requestFile)
            }
        }

        webService.updateEvent(accessToken, map, fileBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                complete?.also{ it(false, "Failed to update event") }
            }
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.also{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to update event")
                    }
                }
            }
        })
    }

    fun getEventList(
        pageNo: Int = 1,
        count: Int = 20,
        type: String? = null,
        keyword: String = "",
        userId: String? = null,
        mobile: String? = null,
        complete: ((list: ArrayList<EventModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("pageNo", pageNo)
        jsonObject.put("count", count)
        jsonObject.put("keyword", keyword)

        type.takeIf { !it.isNullOrEmpty() }?.also {
            jsonObject.put("type", it)
        }
        userId.takeIf { !it.isNullOrEmpty() }?.also {
            jsonObject.put("userId", it)
        }
        mobile.takeIf { !it.isNullOrEmpty() }?.also {
            jsonObject.put("mobile", it)
        }

        val requestBody = jsonObject.getRequestBody()
        webService.getEventList(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<EventListResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<EventListResponseModel>>,
                response: Response<ResponseModel<EventListResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            result?.response?.hasEvent?.let {
                                UserInfo.isSeenGuideWelcome = it
                            }
                            it(result?.response?.getEventList(), result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to get event list")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<EventListResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get event list")
                }
            }

        })
    }

    fun getAllEventsAttended(complete: ((list: ArrayList<EventModel>?, message: String?) -> Unit)? = null) {
        getEventList(1, 1000, "all_attended", complete = complete)
    }

    fun getEventDetails(
        eventId: String?,
        pageNo: Int = 1,
        count: Int = 20,
        complete: ((event: EventModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val event_id = eventId?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also { it(null, "Invalid Params") }
            return
        }

        webService.getEventDetails(UserInfo.accessToken, event_id, pageNo, count)?.enqueue(object:Callback<ResponseModel<EventDetailsResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<EventDetailsResponseModel>>,
                response: Response<ResponseModel<EventDetailsResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.getEventDetails(), result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to get the event details")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<EventDetailsResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the event details")
                }
            }

        })
    }

    fun getPeople(
        eventId: String?,
        status: Int?,
        pageNo: Int = 1,
        count: Int = 20,
        keyword: String = "",
        complete: ((people: PeopleResponseModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("eventId", eventId)
        jsonObject.put("status", status)
        jsonObject.put("pageNo", pageNo)
        jsonObject.put("count", count)
        jsonObject.put("keyword", keyword)

        val requestBody = jsonObject.getRequestBody()
        webService.getPeople(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<PeopleResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<PeopleResponseModel>>,
                response: Response<ResponseModel<PeopleResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.prepare(), result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to get people")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<PeopleResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get people")
                }
            }

        })
    }



    fun turnOnOffPosting(
        eventId: String?,
        isPosting: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.turnOnOffPosting(UserInfo.accessToken, eventId, if (isPosting) "true" else "false")?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to set posting up")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set posting up")
                }
            }

        })
    }

    fun muteOnOffNotification(
        eventId: String?,
        isOn: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.muteOnOffNotification(UserInfo.accessToken, eventId, isOn, UserInfo.mobile)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to set notification up")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set notification up")
                }
            }

        })
    }

    fun deleteEvent(
        eventId: String?,
        isActive: Boolean = false,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.deleteEvent(UserInfo.accessToken, eventId, isActive)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to delete event")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to delete event")
                }
            }

        })
    }

    fun endEvent(
        eventId: String?,
        isEnded: Int = 1,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.endEvent(UserInfo.accessToken, eventId, isEnded)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to delete event")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to delete event")
                }
            }

        })
    }

    fun cancelEvent(
        eventId: String?,
        isCancel: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.cancelEvent(UserInfo.accessToken, eventId, isCancel)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to cancel event")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to cancel event")
                }
            }

        })
    }

    fun saveOnOffEvent(
        eventId: String?,
        isSave: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.saveOnOffEvent(UserInfo.accessToken, eventId, isSave)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to set saving up")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set saving up")
                }
            }

        })
    }

    fun hideOnOffEvent(
        eventId: String?,
        isOn: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val callback = object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to set hiding up")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set hiding up")
                }
            }

        }

        if (isOn) {
            webService.hideEvent(UserInfo.accessToken, eventId, UserInfo.mobile)?.enqueue(callback)
        }else {
            webService.unhideEvent(UserInfo.accessToken, eventId, UserInfo.mobile)?.enqueue(callback)
        }
    }

    fun leaveEvent(
        eventId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.leaveEvent(UserInfo.accessToken, eventId, UserInfo.mobile)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to leave event")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to leave event")
                }
            }

        })
    }

    fun joinOnOffEvent(
        eventId: String?,
        isOn: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val callback = object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to set joining up")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set joining up")
                }
            }

        }

        if (isOn) {
            webService.joinEvent(UserInfo.accessToken, eventId, UserInfo.mobile)?.enqueue(callback)
        }else {
            webService.unjoinEvent(UserInfo.accessToken, eventId, UserInfo.mobile, 0)?.enqueue(callback)
        }
    }

    fun getEventsForCalendar(
        startTime: Long? = null,
        endTime: Long? = null,
        complete: ((list: ArrayList<EventModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("start", startTime)
        jsonObject.put("end", endTime)
        val requestBody = jsonObject.getRequestBody()

        webService.getEventsForCalendar(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<ArrayList<EventModel>>>{
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<EventModel>>>,
                response: Response<ResponseModel<ArrayList<EventModel>>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get event list")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ArrayList<EventModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get event list")
                }
            }

        })
    }

    fun goingMaybeNextTime(
        eventId: String? = null,
        status: Int? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("eventId", eventId)
        jsonObject.put("mobile", UserInfo.mobile)
        jsonObject.put("status", status)
        val requestBody = jsonObject.getRequestBody()

        webService.goingMaybeNextTime(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<EventDetailsResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<EventDetailsResponseModel>>,
                response: Response<ResponseModel<EventDetailsResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to response the invitation")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<EventDetailsResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to response the invitation")
                }
            }

        })
    }

    fun removeGuestFromEvent(
        eventId: String?,
        userId: String? = null,
        email: String? = null,
        mobile: String? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("eventId", eventId)
        userId.takeIf {!it.isNullOrEmpty()}?.let { jsonObject.put("userId", it)}
        email.takeIf {!it.isNullOrEmpty()}?.let { jsonObject.put("email", it)}
        mobile.takeIf {!it.isNullOrEmpty()}?.let {jsonObject.put("mobile", it)}

        val requestBody = jsonObject.getRequestBody()

        webService.removeGuestFromEvent(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to remove the guest")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to remove the guest")
                }
            }

        })
    }

    fun updateInvitations(
        eventId: String?,
        list: ArrayList<InvitationModel>? = null,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val requestBody = InvitationsBody()
        requestBody.eventId = eventId
        requestBody.people = list

        webService.updateInvitations(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to update invitations")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to update invitations")
                }
            }

        })
    }

    fun getLivedEventsForEnding(
        complete: ((list: ArrayList<EventModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getLivedEventsForEnding(UserInfo.accessToken)?.enqueue(object:Callback<ResponseModel<ArrayList<EventModel>>>{
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<EventModel>>>,
                response: Response<ResponseModel<ArrayList<EventModel>>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get event list")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ArrayList<EventModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get event list")
                }
            }

        })
    }

    fun keepLivedEvent(
        eventId: String?,
        complete: ((event: EventModel?, message: String?) -> Unit)? = null
    ) {

        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (eventId.isNullOrEmpty()) {
            complete?.also { it(null, "Invalid event") }
            return
        }

        webService.keepLivedEvent(UserInfo.accessToken, eventId)?.enqueue(object:Callback<ResponseModel<EventModel>>{
            override fun onResponse(
                call: Call<ResponseModel<EventModel>>,
                response: Response<ResponseModel<EventModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to keep lived")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<EventModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to keep lived")
                }
            }

        })
    }

    fun getShareContent(
        params: HashMap<String, String?>?,
        complete: ((content: HashMap<String, Any?>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (params == null) {
            complete?.also { it(null, "Invalid Params") }
            return
        }

        val jsonObject = JSONObject()
        params.forEach { entry ->
            jsonObject.put(entry.key, entry.value)
        }

        val requestBody = jsonObject.getRequestBody()

        webService.getShareContent(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<HashMap<String, Any?>>>{
            override fun onResponse(
                call: Call<ResponseModel<HashMap<String, Any?>>>,
                response: Response<ResponseModel<HashMap<String, Any?>>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to the shared content")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<HashMap<String, Any?>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the shared content")
                }
            }

        })
    }




}