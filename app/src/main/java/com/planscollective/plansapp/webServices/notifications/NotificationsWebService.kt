package com.planscollective.plansapp.webServices.notifications

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationsModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NotificationsWebService : BaseWebService() {
    private val webService = WebServiceBuilder.buildService(NotificationAPIs::class.java)

    fun getNotifications(
        pageNo: Int = 1,
        count: Int = 20,
        complete: ((notifications: NotificationsModel?, message: String?) -> Unit)? = null
    ) {

        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getNotifications(UserInfo.accessToken, pageNo, count)?.enqueue(object:Callback<ResponseModel<NotificationsModel>> {
            override fun onResponse(
                call: Call<ResponseModel<NotificationsModel>>,
                response: Response<ResponseModel<NotificationsModel>>
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
                        it(null, "Failed to get the notifications")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<NotificationsModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the notifications")
                }
            }

        })

    }

    fun deleteNotification(
        notificationId: String?,
        complete: ((notifications: NotificationsModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.deleteNotification(UserInfo.accessToken, notificationId)?.enqueue(object:Callback<ResponseModel<NotificationsModel>> {
            override fun onResponse(
                call: Call<ResponseModel<NotificationsModel>>,
                response: Response<ResponseModel<NotificationsModel>>
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
                        it(null, "Failed to delete the notification")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<NotificationsModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to delete the notification")
                }
            }

        })

    }

    fun getUnviewedNotifications(
        complete: ((dataUnviewed: UnviewedNotifyModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getUnviewedNotifications(UserInfo.accessToken)?.enqueue(object:Callback<ResponseModel<UnviewedNotifyModel>> {
            override fun onResponse(
                call: Call<ResponseModel<UnviewedNotifyModel>>,
                response: Response<ResponseModel<UnviewedNotifyModel>>
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
                        it(null, "Failed to get the notifications")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<UnviewedNotifyModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the notifications")
                }
            }

        })

    }

}