package com.planscollective.plansapp.helper

import android.util.Size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.activity.DashboardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ToastHelper {

    enum class LoadingToastType {
        POSTING {
            override val message = "Posting"
        },
        CREATING_EVENT{
            override val message = "Creating event"
        },
        UPLOADING{
            override val message = "Uploading"
        },
        DOWNLOADING{
            override val message = "Downloading"
        };

        abstract val message: String?
    }

    var loadingToasts = ArrayList<HashMap<String, Any>>()
    var isLoadingToastOn = MutableLiveData<Boolean>()

    init {
        isLoadingToastOn.value = true
        isLoadingToastOn.observeForever {
            if (it == true) {
                showLoadingAlerts()
            }else if (it == false){
                hideAllAlerts()
            }
        }
    }


    fun showLoadingAlerts(type: LoadingToastType? = null, message: String? = null) {
        hideAllAlerts()
        addLoadingAlert(type)
        val loadingMsg = message ?: getLoadingMsg()
        if (isLoadingToastOn.value == true && !loadingMsg.isNullOrEmpty()) {
            showWaiting(loadingMsg)
        }
    }

    fun hideLoadingAlerts(type: LoadingToastType? = null) {
        hideAllAlerts()
        removeLoadingAlert(type)
        val loadingMsg = getLoadingMsg()
        if (isLoadingToastOn.value == true && !loadingMsg.isNullOrEmpty()) {
            showWaiting(loadingMsg)
        }
    }

    fun addLoadingAlert(type: LoadingToastType? = null) {
        type?.also { typeLoading ->
            loadingToasts.indexOfFirst { (it["type"] as? LoadingToastType) == typeLoading }.takeIf { it >= 0 }?.also {
                loadingToasts[it]["count"] = ((loadingToasts[it]["count"] as? Int) ?: 0) + 1
            } ?: run {
                loadingToasts.add(hashMapOf("type" to typeLoading, "count" to 1))
            }
        }
    }

    fun removeLoadingAlert(type: LoadingToastType? = null) {
        type?.also { typeLoading ->
            loadingToasts.indexOfFirst { (it["type"] as? LoadingToastType) == typeLoading }.takeIf { it >= 0 }?.also {
                val count = ((loadingToasts[it]["count"] as? Int) ?: 0) - 1
                if (count < 1) {
                    loadingToasts.removeAt(it)
                }else {
                    loadingToasts[it]["count"] = count
                }
            }
        }
    }

    fun getLoadingMsg(): String? {
        var message: String? = null
        loadingToasts.forEach { item ->
            (item["type"] as? LoadingToastType)?.also { type ->
                (item["count"] as? Int)?.also { count ->
                    if (!message.isNullOrEmpty()) {
                        message += " ${type.message}"
                    }else {
                        message = "${type.message}"
                    }
                    if (count > 1) {
                        message += " ($count)..."
                    }else {
                        message += "..."
                    }
                }
            }
        }
        return message
    }

    fun showMessage(text: String?) {
        PLANS_APP.currentActivity?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                showAlert(text)
                delay(3500)
                hideAlertMessage()
            }
        }
    }

    fun showWaiting(text: String?) {
        PLANS_APP.currentActivity?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                showWaiting(text)
            }
        }
    }

    fun hideWaiting() {
        PLANS_APP.currentActivity?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                hideWaitingMessage()
            }
        }
    }

    fun hideAllAlerts() {
        PLANS_APP.currentActivity?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                hideAlertMessage()
                hideWaitingMessage()
            }
        }
    }

    fun showOverlay(message: String?, imageResId: Int?, sizeImage: Size? = null) {
        (PLANS_APP.currentActivity as? DashboardActivity)?.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                showPopUp(message, imageResId, sizeImage)
            }
        }
    }

}