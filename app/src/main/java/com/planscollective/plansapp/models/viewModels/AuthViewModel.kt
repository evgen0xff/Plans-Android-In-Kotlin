package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.facebook.CallbackManager
import com.facebook.login.widget.LoginButton
import com.planscollective.plansapp.helper.ImageHelper
import com.planscollective.plansapp.manager.FacebookLoginListener
import com.planscollective.plansapp.manager.FacebookManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.user.UserWebService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File



class AuthViewModel(application: Application) : PlansBaseVM(application) {

    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel>
        get() = _user

    var codeOTP: String? = null
    var isSkipMode: Boolean = false
    var notify: NotificationActivityModel? = null

    init {
        _user.value = UserModel().apply {
            deviceType = 1
            fcmId = UserInfo.deviceToken
        }
    }

    fun setUser(newUser: UserModel?) {
        newUser?.let {
            _user.value = it
            _user.value?.deviceType = 1
            _user.value?.fcmId = UserInfo.deviceToken
        }
    }

    fun sendOtp(isCreateAccount: Boolean = true, callBack: ((otpCode: String?, message: String?)-> Unit)? = null) {

        UserWebService.sendOtp(user.value?.mobile,
                             user.value?.email,
            user.value?.email.isNullOrEmpty() ?: true,
                            isCreateAccount,
                            callBack)
    }

    fun verifyOtp(otp: String?,
                  callBack: ((isVerified: Boolean?, message: String?)-> Unit)? = null) {

        UserWebService.verifyOtp(otp,
                                user.value?.mobile,
                                user.value?.email,
              user.value?.email.isNullOrEmpty() ?: true,
                                callBack)
    }

    fun verifyEmail(callBack: ((isVerified: Boolean?, message: String?)-> Unit)? = null) {
        UserWebService.verifyEmail(user.value?.email, callBack)
    }

    fun createAccount(callBack: ((newUser: UserModel?, message: String?)-> Unit)? = null) {
        UserWebService.createUser(user.value, callBack)
    }

    fun login(callBack: ((user: UserModel?, message: String?)-> Unit)? = null) {
        UserWebService.login(user.value, callBack)
    }

    fun setupFBLogin(btnFBLogin: LoginButton?,
                     callManager: CallbackManager?,
                     listener: FacebookLoginListener?,
                     fragment: Fragment? = null
    ) {
        FacebookManager.setupFBLogin(btnFBLogin, callManager, listener, fragment)
    }

    fun changePassword(password: String?,
                       confirmPassword: String?,
                       callBack: ((user: UserModel?, message: String?)-> Unit)? = null) {
        UserWebService.changePassword(user.value?.mobile, user.value?.email, password, confirmPassword, callBack)
    }

    fun updateUserImage(
        context: Context?,
        userImagePath: String?,
        callBack: ((user: UserModel?, message: String?)-> Unit)? = null
    ) {
        val imgPath = userImagePath ?: run {
            callBack?.let{it(null, "Failed to upload image")}
            return
        }
        CoroutineScope(IO).launch {
            val compressedImage = ImageHelper.getCompressedImage(File(imgPath), context)
            compressedImage?.let {
                UserWebService.updateUserImage(user.value?.id, compressedImage, callBack)
            }
        }
    }

}