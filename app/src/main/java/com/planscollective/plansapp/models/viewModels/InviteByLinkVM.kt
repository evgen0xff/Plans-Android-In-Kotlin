package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class InviteByLinkVM(application: Application) : PlansBaseVM(application) {
    var eventModel: EventModel? = null

}