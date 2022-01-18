package com.planscollective.plansapp.webServices.user

import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ErrorResponseModel
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object UserWebService : BaseWebService() {
    private var webService = WebServiceBuilder.buildService(UserApis::class.java)

    //************************************* Authentication *********************************

    fun sendOtp(
        mobile: String? = null,
        email: String? = null,
        communicationType: Boolean = true,
        isCreateAccount: Boolean = true,
        callBack: ((otpCode: String?, message: String?)-> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.sendOTP(mobile, email, communicationType, isCreateAccount)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to verify the code")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.otp, result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to verify the code")
                    }
                }
            }
        })
    }

    fun verifyOtp(
        otp: String?,
        mobile: String? = null,
        email: String? = null,
        communicationType: Boolean = true,
        callBack: ((isVerified: Boolean?, message: String?)-> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.verifyOTP(otp, mobile, email, communicationType)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to verify the code")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.isVerified, result?.response?.message)
                        }
                    }else {
                        it(null, "Failed to verify the code")
                    }
                }
            }
        })
    }

    fun verifyEmail(
        email: String? = null,
        callBack: ((isVerified: Boolean?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.verifyEmail(email)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to verify the code")
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
                        it(false, "Failed to verify the code")
                    }
                }
            }
        })

    }

    fun createUser(
        user: UserModel?,
        callBack: ((newUser: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.createUser(user)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to create account")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            if (!result?.response?.accessToken.isNullOrEmpty()){
                                result?.response?.userDetails?.accessToken = result?.response?.accessToken
                            }
                            it(result?.response?.userDetails, null)
                        }
                    }else {
                        it(null, "Failed to create account")
                    }
                }
            }
        })

    }

    fun login(
        user: UserModel?,
        callBack: ((user: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.login(user)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to login")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            if (!result?.response?.accessToken.isNullOrEmpty()){
                                result?.response?.userProfile?.accessToken = result?.response?.accessToken
                            }
                            it(result?.response?.userProfile, null)
                        }
                    }else {
                        it(null, "Failed to login")
                    }
                }
            }
        })

    }

    fun changePassword(
        mobile: String?,
        email: String?,
        password: String?,
        confirmPassword: String?,
        callBack: ((user: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.changePassword(mobile, email, password, confirmPassword)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to reset password")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            if (!result?.response?.accessToken.isNullOrEmpty()){
                                result?.response?.data?.accessToken = result?.response?.accessToken
                            }
                            it(result?.response?.data, null)
                        }
                    }else {
                        it(null, "Failed to reset password")
                    }
                }
            }
        })

    }

    fun changePasswordWithOldPassword(
        oldPassword: String?,
        newPassword: String?,
        callBack: ((success: Boolean?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.changePasswordWithOldPassword(UserInfo.accessToken, UserInfo.mobile, UserInfo.email, newPassword, newPassword, oldPassword)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to change password")
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
                            it(true, "Your password has been updated.")
                        }
                    }else {
                        it(null, "Failed to change password")
                    }
                }
            }
        })

    }


    //************************************* Update User Profile *********************************
    fun updateUser(
        user: UserModel?,
        complete: ((newUser: UserModel?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            complete?.also{ it(null, "Failed to update user, invalid access token") }
            return
        }

        val map = user?.toMap()
        var fileBody : MultipartBody.Part? = null
        user?.profileImage?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
            }
        }

        webService.updateUser(accessToken, map, fileBody)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                complete?.also{ it(null, "Failed to update user profile") }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                complete?.also{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response?.userDetails, null)
                        }
                    }else {
                        it(null, "Failed to update user profile")
                    }
                }
            }
        })
    }


    fun updateUserImage(
        userId: String?,
        userImagePath: String?,
        callBack: ((user: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            callBack?.let{ it(null, "Failed to update user") }
            return
        }
        val user_id = userId ?: run {
            callBack?.let{ it(null, "Failed to update user") }
            return
        }
        val imgPath = userImagePath ?: run {
            callBack?.let{ it(null, "Failed to update user") }
            return
        }

        val map: MutableMap<String, RequestBody> = HashMap()
        map["userId"] = user_id.toRequestBody("text/plain".toMediaTypeOrNull())
        map["mediaType"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())

        var fileBody : MultipartBody.Part? = null
        imgPath.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
            }
        }

        webService.updateUserImage(accessToken, map, fileBody)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to reset password")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            if (!result?.response?.accessToken.isNullOrEmpty()){
                                result?.response?.data?.accessToken = result?.response?.accessToken
                            }
                            it(result?.response?.userDetails, null)
                        }
                    }else {
                        it(null, "Failed to reset password")
                    }
                }
            }
        })

    }

    fun updateLocation(
        latitude: Double?,
        longitude: Double?,
        callBack: ((success: Boolean, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.updateLocation(UserInfo.accessToken, latitude, longitude)?.enqueue(object:Callback<ResponseModel<Any>>{
            override fun onFailure(call: Call<ResponseModel<Any>>, t: Throwable) {
                if (callBack != null) {
                    callBack(false, "Failed to update user location")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<Any>>,
                response: Response<ResponseModel<Any>>
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
                        it(false, "Failed to update user location")
                    }
                }
            }
        })

    }

    fun getUsersWithMobiles(
        mobiles: List<String?>?,
        callBack: ((users: ArrayList<UserModel>?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getUsersWithMobiles(UserInfo.accessToken, mobiles)?.enqueue(object:Callback<ResponseModel<ArrayList<UserModel>>>{
            override fun onFailure(call: Call<ResponseModel<ArrayList<UserModel>>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to get users from mobiles")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                response: Response<ResponseModel<ArrayList<UserModel>>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get users from mobiles")
                    }
                }
            }
        })

    }

    fun getUsersWithEmails(
        mobiles: List<String?>?,
        callBack: ((users: ArrayList<UserModel>?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getUsersWithEmails(UserInfo.accessToken, mobiles)?.enqueue(object:Callback<ResponseModel<ArrayList<UserModel>>>{
            override fun onFailure(call: Call<ResponseModel<ArrayList<UserModel>>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to get users from emails")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                response: Response<ResponseModel<ArrayList<UserModel>>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get users from emails")
                    }
                }
            }
        })

    }

    fun getUserProfile(
        userId: String?,
        complete: ((user: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getUserProfile(UserInfo.accessToken, userId)?.enqueue(object:Callback<ResponseModel<AuthResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<AuthResponseModel>>, t: Throwable) {
                complete?.also{ it(null, "Failed to get the user profile")}
            }
            override fun onResponse(
                call: Call<ResponseModel<AuthResponseModel>>,
                response: Response<ResponseModel<AuthResponseModel>>
            ) {
                if (response.isSuccessful){
                    val result = response.body()
                    if (result?.error != null) {
                        complete?.also{ it(null, handleError(result.error))}
                    }else {
                        complete?.also{ it(result?.response?.userProfile, null)}
                    }
                }else {
                    complete?.also{ it(null, "Failed to get the user profile")}
                }
            }
        })

    }

    fun getPlansUserList(
        keyword: String?,
        pageNo: Int?,
        count: Int?,
        callBack: ((users: ArrayList<UserModel>?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getAllUsersList(UserInfo.accessToken, keyword, pageNo, count)?.enqueue(object:Callback<ResponseModel<ArrayList<UserModel>>>{
            override fun onFailure(call: Call<ResponseModel<ArrayList<UserModel>>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to get plans user list")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<UserModel>>>,
                response: Response<ResponseModel<ArrayList<UserModel>>>
            ) {
                callBack?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get plans user list")
                    }
                }
            }
        })

    }

    fun deleteAccount(
        password: String?,
        callBack: ((success: Boolean?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.deleteAccount(UserInfo.accessToken, password)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to delete the account")
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
                            it(null, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(null, "Failed to delete the account")
                    }
                }
            }
        })

    }

    fun logout(
        callBack: ((success: Boolean?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            callBack?.also { it(false, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.logout(UserInfo.accessToken)?.enqueue(object:Callback<ResponseModel<ErrorResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ErrorResponseModel>>, t: Throwable) {
                if (callBack != null) {
                    callBack(null, "Failed to logout")
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
                            it(null, handleError(result.error))
                        }else {
                            it(true, null)
                        }
                    }else {
                        it(null, "Failed to logout")
                    }
                }
            }
        })

    }

    fun updateLastViewTimeForNotify(
        time: Long?,
        complete: ((user: UserModel?, message: String?)-> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.updateLastViewTimeForNotify(UserInfo.accessToken, time)?.enqueue(object:Callback<ResponseModel<UserModel>>{
            override fun onFailure(call: Call<ResponseModel<UserModel>>, t: Throwable) {
                if (complete != null) {
                    complete(null, "Failed to update last seen time for the notifications")
                }
            }
            override fun onResponse(
                call: Call<ResponseModel<UserModel>>,
                response: Response<ResponseModel<UserModel>>
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
                        it(null, "Failed to update last seen time for the notifications")
                    }
                }
            }
        })

    }


}