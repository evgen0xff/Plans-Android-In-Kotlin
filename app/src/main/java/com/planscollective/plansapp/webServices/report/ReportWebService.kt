package com.planscollective.plansapp.webServices.report

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ErrorResponseModel
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ReportWebService : BaseWebService() {
    private var webService = WebServiceBuilder.buildService(ReportApis::class.java)

    fun reportEntity(
        entity_id: String?,
        entity_type: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.reportEntity(UserInfo.accessToken, entity_id, entity_type)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to report")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to report")
                }
            }

        })
    }



}