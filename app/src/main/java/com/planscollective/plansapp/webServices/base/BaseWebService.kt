package com.planscollective.plansapp.webServices.base

import android.content.Context
import android.net.ConnectivityManager
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.manager.UserInfo
import kotlinx.coroutines.*


abstract class BaseWebService {

    open fun isNetworkConnected(): Boolean {
        val cm = PLANS_APP.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    open fun handleError(error: ErrorResponseModel?) : String? {
        var message = error?.message ?: return null

        var isLogout = false

        when(message) {
            ConstantTexts.LOGIN_AGAIN, ConstantTexts.UNAUTHORISED -> {
                message = ConstantTexts.SESSION_EXPIRED
                isLogout = true
            }
        }

        if (message.lowercase().contains(ConstantTexts.LOST_INTERNET.lowercase())) {
            message = ConstantTexts.LOST_INTERNET
        }

        if (isLogout) {
            GlobalScope.launch(Dispatchers.IO) {
                delay(3500)
                withContext(Dispatchers.Main) {
                    if (UserInfo.isLoggedIn) {
                        PLANS_APP.gotoMainActivity(R.id.landingFragment)
                    }
                }
            }
        }

        return message
    }
}