package com.planscollective.plansapp.webServices.post

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.getRequestBody
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.PostLikedModel
import com.planscollective.plansapp.models.dataModels.PostModel
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

object PostWebService : BaseWebService() {
    private var webService = WebServiceBuilder.buildService(PostApis::class.java)

    fun deletePost(
        postId: String?,
        eventId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("postId", postId)
        jsonObject.put("eventId", eventId)

        webService.deletePost(UserInfo.accessToken, jsonObject.getRequestBody())?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to delete post")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to delete post")
                }
            }

        })
    }

    fun deleteComment(
        commentId: String?,
        postId: String?,
        eventId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        val jsonObject = JSONObject()
        jsonObject.put("commentId", commentId)
        jsonObject.put("postId", postId)
        jsonObject.put("eventId", eventId)

        webService.deleteComment(UserInfo.accessToken, jsonObject.getRequestBody())?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to delete comment")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to delete comment")
                }
            }

        })
    }

    fun likeUnlikeComment(
        commentId: String?,
        postId: String?,
        eventId: String?,
        isLike: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.likeUnlikeComment(UserInfo.accessToken, commentId, postId, eventId, isLike)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to set up like")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set up like")
                }
            }

        })
    }

    fun likeUnlikePost(
        postId: String?,
        eventId: String?,
        isLike: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.likeUnlikePost(UserInfo.accessToken, postId, eventId, isLike)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
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
                        it(false, "Failed to set up like")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to set up like")
                }
            }

        })
    }

    fun createPost(
        eventId: String?,
        postText: String? = null,
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
        postText?.let{
            map["postText"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        mediaType?.let{
            map["mediaType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var body : MultipartBody.Part? = null
        urlMedia?.let {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                body = MultipartBody.Part.createFormData("postMedia", file.name, requestFile)
            }
        }

        webService.createPost(accessToken, map, body)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to create post")
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
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to create post")
                    }
                }
            }
        })

    }

    fun getPostDetails(
        postId: String?,
        eventId: String?,
        pageNo: Int = 1,
        count: Int = 20,
        complete: ((event: PostModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getPostDetails(UserInfo.accessToken, eventId, postId, pageNo, count)?.enqueue(object:Callback<ResponseModel<PostModel>>{
            override fun onResponse(
                call: Call<ResponseModel<PostModel>>,
                response: Response<ResponseModel<PostModel>>
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
                        it(null, "Failed to get the post details")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<PostModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the post details")
                }
            }

        })
    }

    fun createComment(
        postId: String?,
        eventId: String?,
        commentText: String? = null,
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
        postId?.let{
            map["postId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        eventId?.let{
            map["eventId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        commentText?.let{
            map["commentText"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        mediaType?.let{
            map["mediaType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var body : MultipartBody.Part? = null
        urlMedia?.let {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                body = MultipartBody.Part.createFormData("commentMedia", file.name, requestFile)
            }
        }

        webService.createComment(accessToken, map, body)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to create comment")
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
                            it(true, null)
                        }
                    }else {
                        it(false, "Failed to create comment")
                    }
                }
            }
        })

    }

    fun getPostsLiked(
        pageNo: Int? = 1,
        count: Int? = 20,
        complete: ((list: ArrayList<PostLikedModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getPostsLiked(UserInfo.accessToken, pageNo, count)?.enqueue(object:Callback<ResponseModel<ArrayList<PostLikedModel>>>{
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<PostLikedModel>>>,
                response: Response<ResponseModel<ArrayList<PostLikedModel>>>
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
                        it(null, "Failed to get posts liked")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ArrayList<PostLikedModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get posts liked")
                }
            }

        })
    }






}