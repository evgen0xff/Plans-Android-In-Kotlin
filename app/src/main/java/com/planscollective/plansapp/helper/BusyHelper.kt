package com.planscollective.plansapp.helper

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.kaopiz.kprogresshud.KProgressHUD
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.customUI.LoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object BusyHelper {
    @SuppressLint("StaticFieldLeak")
    var progressHUD: KProgressHUD? = null

    fun show(context: Context? = null, message: String? = null, timeDelay: Long = 2000) {
        hide()

        val contextApp = context ?: PLANS_APP.currentActivity
        progressHUD = KProgressHUD.create(contextApp)
            .setCornerRadius(50f)
            .setCustomView(LoadingView(contextApp))

        if (!message.isNullOrEmpty()){
            progressHUD?.setLabel(message)
        }

        PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.IO){
            delay(timeDelay)
            withContext(Dispatchers.Main) {
                progressHUD?.show()
            }
        }
    }

    val isShown: Boolean
        get() = progressHUD != null && progressHUD!!.isShowing

    fun hide() {
        if (progressHUD != null){
            progressHUD!!.dismiss()
        }
        progressHUD = null
    }
}
