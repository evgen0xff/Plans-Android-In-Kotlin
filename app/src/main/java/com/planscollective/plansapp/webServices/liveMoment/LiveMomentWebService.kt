package com.planscollective.plansapp.webServices.liveMoment

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.getRequestBody
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserLiveMomentsModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ErrorResponseModel
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object LiveMomentWebService : BaseWebService(){
    private var webService = WebServiceBuilder.buildService(LiveMomentAPIs::class.java)

    fun getLiveMoments(
        eventId: String?,
        userId: String? = null,
        liveMomentId: String? = null,
        pageNo: Int? = 1,
        count: Int? = 100,
        complete: ((list: ArrayList<UserLiveMomentsModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        eventId?.let{ jsonObject.put("eventId", it) }
        userId?.let{ jsonObject.put("userId", it) }
        liveMomentId?.let{ jsonObject.put("liveMomentId", it) }
        pageNo?.let{ jsonObject.put("pageNo", "$it") }
        count?.let{ jsonObject.put("count", "$it") }

        val requestBody = jsonObject.getRequestBody()
        webService.getLiveMoments(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<LiveMomentResponse>>{
            override fun onResponse(
                call: Call<ResponseModel<LiveMomentResponse>>,
                response: Response<ResponseModel<LiveMomentResponse>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.getLiveMomentList(), result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to get live moments")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<LiveMomentResponse>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get live moments")
                }
            }

        })
    }

    fun deleteLiveMoment(
        liveMomentId: String?,
        eventId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        eventId?.let{ jsonObject.put("eventId", it) }
        liveMomentId?.let{ jsonObject.put("liveMommentsId", it) }

        val requestBody = jsonObject.getRequestBody()
        webService.deleteLiveMoment(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to delete live moments")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to delete live moments")
                }
            }

        })
    }

    fun viewedLiveMoment(
        liveMomentIds: ArrayList<String>?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.viewedLiveMoment(UserInfo.accessToken, liveMomentIds)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to view live moments")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to view live moments")
                }
            }

        })
    }


    fun createLiveMoment(
        eventId: String?,
        liveText: String? = null,
        mediaType: String? = null,
        urlMedia: String? = null,
        callBack: ((success: Boolean, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            callBack?.let{ it(false, "Failed to create post") }
            return
        }

        val map: MutableMap<String, RequestBody?> = HashMap()
        eventId?.let{
            map["eventId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        liveText?.let{
            map["liveText"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        mediaType?.let{
            map["mediaType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        map["createdAt"] = (System.currentTimeMillis() / 1000).toString().toRequestBody("text/plain".toMediaTypeOrNull())

        var body : MultipartBody.Part? = null
        urlMedia?.let {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                body = MultipartBody.Part.createFormData("liveMedia", file.name, requestFile)
            }
        }

        webService.createLiveMoment(accessToken, map, body)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to create live moment")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(false, handleError(result.error))
                        }else {
                            it(true, result?.response?.message)
                        }
                    }else {
                        it(false, "Failed to create live moment")
                    }
                }
            }
        })

    }

}