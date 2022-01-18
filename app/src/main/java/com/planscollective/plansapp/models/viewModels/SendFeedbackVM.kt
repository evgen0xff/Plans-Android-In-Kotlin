package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.fragment.settings.SendFeedbackFragment
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class SendFeedbackVM(application: Application) : PlansBaseVM(application) {

    var fileScreenshot: String? = null
    var feedback: String? = null
    var type = SendFeedbackFragment.Type.SEND_FEEDBACK

    fun validate() : Boolean {
        return !feedback.isNullOrEmpty()
    }

}