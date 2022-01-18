package com.planscollective.plansapp.models.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.R
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

open class DashboardVM(application: Application) : PlansBaseVM(application) {
    val selectedItemId = MutableLiveData<Int>()
    init {
        selectedItemId.value = R.id.menu_home
    }
}