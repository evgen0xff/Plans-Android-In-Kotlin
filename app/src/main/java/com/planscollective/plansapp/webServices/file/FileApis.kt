package com.planscollective.plansapp.webServices.file

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileApis {
    @Streaming
    @GET
    fun downloadFileWithDynamicUrl(@Url fileUrl: String?) : Call<ResponseBody>
}