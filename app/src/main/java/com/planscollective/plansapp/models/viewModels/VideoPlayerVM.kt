package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class VideoPlayerVM(application: Application) : PlansBaseVM(application) {

    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition: Long = 0
    var urlString: String? = null


}