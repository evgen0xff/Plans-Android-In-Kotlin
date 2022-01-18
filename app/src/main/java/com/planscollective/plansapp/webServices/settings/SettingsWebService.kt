package com.planscollective.plansapp.webServices.settings

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.getRequestBody
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.SettingOptionModel
import com.planscollective.plansapp.models.dataModels.SettingsModel
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
import java.util.*

object SettingsWebService : BaseWebService() {
    private val webService = WebServiceBuilder.buildService(SettingsAPIs::class.java)

    fun getSettings(
        complete: ((settings: SettingsModel?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getSettings(UserInfo.accessToken)?.enqueue(object:Callback<ResponseModel<SettingsModel>> {
            override fun onResponse(
                call: Call<ResponseModel<SettingsModel>>,
                response: Response<ResponseModel<SettingsModel>>
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
                        it(null, "Failed to get Settings")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<SettingsModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get Settings")
                }
            }

        })

    }

    fun updateSettings(
        option: SettingOptionModel?,
        isChecked: Boolean = true,
        complete: ((settings: SettingsModel?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        option?.key.takeIf{ !it.isNullOrEmpty() }?.let{
            jsonObject.put(it, isChecked)
        } ?: run{
            complete?.also { it(null, "Invalid Params") }
            return
        }
        val requestBody = jsonObject.getRequestBody()

        webService.updateSettings(UserInfo.accessToken, requestBody)?.enqueue(object:Callback<ResponseModel<SettingsModel>> {
            override fun onResponse(
                call: Call<ResponseModel<SettingsModel>>,
                response: Response<ResponseModel<SettingsModel>>
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
                        it(null, "Failed to update Settings")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<SettingsModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to update Settings")
                }
            }

        })

    }

    fun sendFeedback(
        msgFeedback: String?,
        type: String?,
        filePath: String? = null,
        msgSubFeedback: String = "",
        complete: ((success: Boolean?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            complete?.also{ it(null, "Failed to send the ${type ?: "data"}, invalid access token") }
            return
        }

        val map: MutableMap<String, RequestBody?> = HashMap()

        msgFeedback?.also{
            map["feedbackMessage"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        type?.also{
            map["feedbackType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        msgSubFeedback.also{
            map["feedbackSubject"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var fileBody : MultipartBody.Part? = null
        filePath?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("liveMedia", file.name, requestFile)
            }
        }

        webService.sendFeedback(accessToken, map, fileBody)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                complete?.also{ it(null, "Failed to send the ${type ?: "data"}") }
            }
            override fun onResponse(
                call: Call<ResponseModel<ErrorResponseModel>>,
                response: Response<ResponseModel<ErrorResponseModel>>
            ) {
                complete?.also{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(null, "Failed to send the ${type ?: "data"}")
                    }
                }
            }
        })
    }



}