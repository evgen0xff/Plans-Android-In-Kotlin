package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class DeleteAccountVM(application: Application) : PlansBaseVM(application) {

    var password: String? = null
}