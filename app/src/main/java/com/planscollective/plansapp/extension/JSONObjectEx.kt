package com.planscollective.plansapp.extension

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


fun JSONObject.getRequestBody(): RequestBody {
    return JSONObject(this.toString()).toString()
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
}
