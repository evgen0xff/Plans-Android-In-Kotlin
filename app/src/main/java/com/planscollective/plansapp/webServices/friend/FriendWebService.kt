package com.planscollective.plansapp.webServices.friend

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.extension.getRequestBody
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ErrorResponseModel
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FriendWebService : BaseWebService() {
    private var webService = WebServiceBuilder.buildService(FriendAPIs::class.java)

    fun cancelFriendRequest(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.cancelFriendRequest(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to cancel friend request")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to cancel friend request")
                }
            }

        })
    }

    fun acceptFriendRequest(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.acceptFriendRequest(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to accept friend request")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to accept friend request")
                }
            }

        })
    }

    fun rejectFriendRequest(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.rejectFriendRequest(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to reject friend request")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to reject friend request")
                }
            }

        })
    }


    fun unfriend(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.unfriend(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to unfriend")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to unfriend")
                }
            }

        })
    }

    fun unblockUser(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.unblockUser(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to unblock user")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to unblock user")
                }
            }

        })
    }

    fun blockUser(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.blockUser(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to block user")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to block user")
                }
            }

        })
    }


    fun sendFriendRequest(
        friendId: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.sendFriendRequest(UserInfo.accessToken, friendId)?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to send friend request")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to send friend request")
                }
            }
        })
    }

    fun sendSMSForPlansInvite(
        mobile: String?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
       }

        val json = JSONObject()
        json.put("mobile", mobile)
        json.put("type", "inviteToPlans")

        webService.sendSMS(UserInfo.accessToken, json.getRequestBody())?.enqueue(object:
            Callback<ResponseModel<ErrorResponseModel>> {
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
                        it(false, "Failed to send SMS")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ErrorResponseModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(false, "Failed to send SMS")
                }
            }
        })
    }

    fun getFriendList(
        pageNo: Int = 1,
        count: Int = 20,
        keyword: String = "",
        userId: String? = null,
        complete: ((list: ArrayList<InvitationModel>?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val json = JSONObject()
        json.put("pageNo", pageNo)
        json.put("count", count)
        json.put("keyword", keyword)
        userId.takeIf { !it.isNullOrEmpty() }?.let{ json.put("userId", it)}

        webService.getFriendList(UserInfo.accessToken, json.getRequestBody())?.enqueue(object:
            Callback<ResponseModel<ArrayList<InvitationModel>>> {
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<InvitationModel>>>,
                response: Response<ResponseModel<ArrayList<InvitationModel>>>
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
                        it(null, "Failed to get friend list")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ArrayList<InvitationModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get friend list")
                }
            }
        })
    }

    fun getBlockedUsers(
        isByMe: Boolean = true,
        pageNo: Int = 1,
        count: Int = 20,
        complete: ((list: ArrayList<UserModel>?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getBlockedUsers(UserInfo.accessToken, isByMe, pageNo, count)?.enqueue(object:
            Callback<ResponseModel<ArrayList<UserModel>>> {
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                response: Response<ResponseModel<ArrayList<UserModel>>>
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
                        it(null, "Failed to get blocked users")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get blocked users")
                }
            }
        })
    }

    fun getFriendRequests(
        pageNo: Int = 1,
        count: Int = 20,
        complete: ((list: ArrayList<UserModel>?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getFriendRequests(UserInfo.accessToken, pageNo, count)?.enqueue(object:
            Callback<ResponseModel<ArrayList<UserModel>>> {
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                response: Response<ResponseModel<ArrayList<UserModel>>>
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
                        it(null, "Failed to get the friend requests")
                    }
                }
            }
            override fun onFailure(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the friend requests")
                }
            }
        })
    }

}