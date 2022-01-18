package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM

class LiveMomentCameraVM(application: Application) : PlansBaseVM(application) {

    enum class Status {
        NORMAL,
        RECORDING_VIDEO_START,
        RECORDING_VIDEO,
        PREVIEW_PHOTO,
        PREVIEW_VIDEO,
        PLAYING_VIDEO,
    }

    var eventId: String? = null
    var eventName: String? = null
    var isLive: Boolean = false

    var urlPhotoPosting: String? = null
    var urlVideoPosting: String? = null

    var status = Status.NORMAL
    var pictureResult: PictureResult? = null
    var videoResult: VideoResult? = null
    var bmpPicture: Bitmap? = null
    var timeRecordingStart: Long = 0
    var statusFlash = Flash.OFF

    var caption = MutableLiveData<String>()

    fun sendPost(context: Context) {
        val mediaType = if (!urlVideoPosting.isNullOrEmpty()) "video" else if (!urlPhotoPosting.isNullOrEmpty()) "image" else "text"
        val urlMedia = urlVideoPosting ?: urlPhotoPosting
    }

}