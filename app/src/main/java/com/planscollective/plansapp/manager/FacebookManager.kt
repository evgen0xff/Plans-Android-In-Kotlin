package com.planscollective.plansapp.manager

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.planscollective.plansapp.extension.toDate
import com.planscollective.plansapp.extension.toSeconds
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.user.UserWebService
import com.facebook.GraphResponse

import com.facebook.GraphRequest




interface FacebookLoginListener {
    fun onLoggedInFB(result: LoginResult?)
    fun onLoggedInPlans(user: UserModel?)
    fun onCreatingPlansUser(user: UserModel?)
    fun onCancel()
    fun onFailed(error: FacebookException?)
}

object FacebookManager : FacebookCallback<LoginResult?> {

    private var facebookListener: FacebookLoginListener? = null
    private var fragment: Fragment? = null

    fun setupFBLogin(btnFBLogin: LoginButton?,
                     callbackManager: CallbackManager?,
                     listener: FacebookLoginListener? = null,
                     fragment: Fragment? = null
    ) {
        btnFBLogin?.setPermissions(listOf("email"))
        callbackManager?.let {
            btnFBLogin?.registerCallback(it, this)
        }
        fragment?.let {
            btnFBLogin?.fragment = it
            this.fragment = it
        }
        facebookListener = listener
    }

    override fun onSuccess(result: LoginResult?) {
        facebookListener?.onLoggedInFB(result)

        BusyHelper.show()
        val request = GraphRequest.newMeRequest(result?.accessToken) { jsonObject, response ->
            val user = UserModel()
            user.socialId = jsonObject?.takeIf { it.has("id") }?.getString("id")
            user.email = jsonObject?.takeIf { it.has("email") }?.getString("email")
            user.name = jsonObject?.takeIf { it.has("name") }?.getString("name")
            user.firstName = jsonObject?.takeIf { it.has("first_name") }?.getString("first_name")
            user.lastName = jsonObject?.takeIf { it.has("last_name") }?.getString("last_name")
            user.birthDay = jsonObject?.takeIf { it.has("birthday") }?.getString("birthday")?.toDate("MM/dd/yyyy")
            user.dob = user.birthDay?.time?.toSeconds() ?: 0
            user.socialId?.takeIf { it.isNotEmpty() }?.also {
                user.profileImage = "https://graph.facebook.com/$it/picture?height=400&width=400"
            }

            user.loginType = "false"
            user.fcmId = UserInfo.deviceToken
            user.deviceType = 1
            UserWebService.login(user){ userPlans, _ ->
                BusyHelper.hide()
                if (userPlans != null) {
                    facebookListener?.onLoggedInPlans(userPlans)
                }else {
                    facebookListener?.onCreatingPlansUser(user)
                }
            }
            LoginManager.getInstance().logOut()
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,email,name,first_name,last_name,birthday,picture.width(400).height(400)")
        request.parameters = parameters
        request.executeAsync()
    }

    override fun onCancel() {
        facebookListener?.onCancel()
    }

    override fun onError(error: FacebookException) {
        facebookListener?.onFailed(error)
    }

}