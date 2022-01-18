package com.planscollective.plansapp.activity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.MediaController
import androidx.lifecycle.lifecycleScope
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.PictureFormat
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.ActivityPlansCameraBinding
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toByteArray
import com.planscollective.plansapp.helper.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs

class PlansCameraActivity : BaseActivity<ActivityPlansCameraBinding>(),
    OnSingleClickListener, View.OnLongClickListener, View.OnTouchListener {

    enum class Status {
        NORMAL,
        RECORDING_VIDEO_START,
        RECORDING_VIDEO,
        PREVIEW_PHOTO,
        PREVIEW_VIDEO,
        PLAYING_VIDEO,
    }

    private var status = Status.NORMAL
    var pictureResult: PictureResult? = null
    var bmpPicture: Bitmap? = null
    var videoResult: VideoResult? = null
    var timeRecordingStart: Long = 0
    var mediaType = MediaPickerHelper.typePicker.mediaType
    var statusFlash = Flash.OFF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlansCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    override fun setupUI() {
        super.setupUI()

        binding.let { it ->
            setupCameraView(OSHelper.widthScreen, OSHelper.heightScreen)

            it.videoView.setOnSingleClickListener(this)
            it.layoutRightNavItem.setOnSingleClickListener(this)
            it.layoutPlay.setOnSingleClickListener(this)
            it.layoutFlash.setOnSingleClickListener(this)
            it.imvRotation.setOnSingleClickListener(this)
            it.layoutSend.setOnSingleClickListener(this)

            it.provRecord.setTotalDuration(Constants.LIMIT_DURATION_VIDEO.toLong())
            it.provRecord.setTintColor(PlansColor.TEAL_MAIN)
            it.provRecord.setOnSingleClickListener(this)
            if (mediaType != MediaPickerHelper.MediaType.IMAGE_ONLY) {
                it.provRecord.setOnTouchListener(this)
                it.provRecord.setOnLongClickListener(this)
            }
        }

        setupVideoView()
        initializeCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pictureResult = null
            videoResult = null
        }
    }

    override fun onSingleClick(v: View?) {
        when(v) {
            binding.layoutRightNavItem -> {
                actionCancel()
            }
            binding.layoutPlay, binding.videoView -> {
                actionPlayPause()
            }
            binding.layoutFlash -> {
                actionFlash()
            }
            binding.provRecord -> {
                actionTakePhoto()
            }
            binding.imvRotation -> {
                actionSwitchCamera()
            }
            binding.layoutSend -> {
                actionSend()
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when(v) {
            binding.provRecord -> {
                println("PlansCameraActivity - onLongClick")
                actionRecordVideo()
            }
        }
        return true
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        var result = false
        when(v) {
            binding.provRecord -> {
                when(event?.actionMasked) {
                    MotionEvent.ACTION_UP -> {
                        println("PlansCameraActivity - MotionEvent.ACTION_UP")
                        result = when(status) {
                            Status.RECORDING_VIDEO_START, Status.RECORDING_VIDEO -> {
                                initializeCamera()
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
        }
        return result
    }

    private fun setupCameraView(widthPixel: Int, heightPixel: Int) {

        val width = SizeSelectors.minWidth(widthPixel)
        val height = SizeSelectors.minHeight(heightPixel)
        val dimensions = SizeSelectors.and(width, height)                                           // Matches sizes bigger than Screen size.
        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(widthPixel, heightPixel), 0.03f)  // Matches Screen ratio.
        val result = SizeSelectors.or(
            SizeSelectors.and(ratio, dimensions),   // Try to match both constraints
            ratio,                                  // If none is found, at least try to match the aspect ratio
            SizeSelectors.biggest()                 // If none is found, take the biggest
        )

        binding.apply {
            cameraView.setPictureSize(result)
            cameraView.setVideoSize(result)
            cameraView.setPreviewStreamSize(result)

            cameraView.setLifecycleOwner(this@PlansCameraActivity)
            cameraView.addCameraListener(Listener())
        }
    }

    private fun setupVideoView() {
        val controller = MediaController(this)
        controller.setAnchorView(binding.videoView)
        controller.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(controller)
        controller.visibility = View.INVISIBLE
        binding.videoView.setOnCompletionListener {
            status = Status.PREVIEW_VIDEO
            updateUI()
        }
    }

    private fun updateData(status: Status = Status.NORMAL,
                           picture: PictureResult? = null,
                           video: VideoResult? = null) {
        this.status = status

        when(status) {
            Status.NORMAL, Status.PREVIEW_VIDEO -> {
                setCameraMode(Mode.PICTURE)
            }
        }

        picture?.let {
            try {
                it.toBitmap(OSHelper.widthScreen, OSHelper.heightScreen){ bitmapOrigin ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = if (it.facing == Facing.BACK) bitmapOrigin else {
                            ImageHelper.createFlippedBitmap(bitmapOrigin)
                        }
                        bmpPicture = bitmap
                        withContext(Dispatchers.Main) {
                            binding.imvPreviewPhoto.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: UnsupportedOperationException) {
                bmpPicture = null
                binding.imvPreviewPhoto.setImageBitmap(null)
            }
        } ?: run {
            bmpPicture = null
            binding.imvPreviewPhoto.setImageBitmap(null)
        }

        video?.let {
            binding.apply {
                videoView.stopPlayback()
                videoView.setVideoURI(Uri.fromFile(it.file))
                videoView.setOnPreparedListener { mp ->
                    val videoWidth = mp.videoWidth.toFloat()
                    val videoHeight = mp.videoHeight.toFloat()
                    val viewWidth = videoView.width.toFloat()
                    val height = (viewWidth * (videoHeight / videoWidth)).toInt()
                    videoView.setLayoutHeight(height)
                    if (mp.duration > 200) {
                        videoView.seekTo(200)
                    }
                }
            }
        } ?: run {
            binding.videoView.stopPlayback()
            binding.videoView.setVideoURI(null)
        }

        pictureResult = picture
        videoResult = video

        updateUI()
    }

    private fun updateUI(status: Status? = null) {
        this.status = status ?: this.status

        println("PlansCameraActivity - updateUI : ${this.status.name}")

        if (videoResult?.file?.exists() == false ) {
            binding.videoView.stopPlayback()
            binding.videoView.setVideoURI(null)
        }

        if (statusFlash == Flash.ON) {
            binding.imvFlash.setImageResource(R.drawable.ic_flash_on)
        }else {
            binding.imvFlash.setImageResource(R.drawable.ic_flash_off)
        }
        val valueStatus = this.status
        binding.apply {
            when(valueStatus) {
                Status.NORMAL -> {
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.VISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutSend.visibility = View.INVISIBLE
                    layoutCameraControl.visibility = View.VISIBLE
                    provRecord.visibility = View.VISIBLE
                    layoutFlash.visibility = View.VISIBLE
                    imvRotation.visibility = View.VISIBLE
                }
                Status.RECORDING_VIDEO -> {
                    layoutRightNavItem.visibility = View.INVISIBLE
                    cameraView.visibility = View.VISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutSend.visibility = View.INVISIBLE
                    layoutCameraControl.visibility = View.VISIBLE
                    provRecord.visibility = View.VISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PREVIEW_PHOTO -> {
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.VISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutSend.visibility = View.VISIBLE
                    layoutCameraControl.visibility = View.INVISIBLE
                    provRecord.visibility = View.INVISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PREVIEW_VIDEO -> {
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.VISIBLE
                    layoutPlay.visibility = View.VISIBLE
                    layoutSend.visibility = View.VISIBLE
                    layoutCameraControl.visibility = View.INVISIBLE
                    provRecord.visibility = View.INVISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PLAYING_VIDEO -> {
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.VISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutSend.visibility = View.INVISIBLE
                    layoutCameraControl.visibility = View.INVISIBLE
                    provRecord.visibility = View.INVISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
            }

            if (cameraView.facing == Facing.FRONT) {
                layoutFlash.visibility = View.INVISIBLE
            }
        }


    }

    private fun startAnim() {
        binding.provRecord.startAnim()
    }

    private fun endAnim() {
        binding.provRecord.stopAnim()
    }

    private fun actionCancel() {
        binding.videoView.stopPlayback()
        when(status) {
            Status.NORMAL, Status.RECORDING_VIDEO -> {
                finish()
            }
            Status.PREVIEW_VIDEO, Status.PREVIEW_PHOTO, Status.PLAYING_VIDEO -> {
                initializeCamera()
            }
        }
    }

    override fun onBackPressed() {
        actionCancel()
    }

    private fun actionPlayPause() {
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
            updateUI(Status.PLAYING_VIDEO)
        }else {
            binding.videoView.pause()
            updateUI(Status.PREVIEW_VIDEO)
        }
    }

    private fun actionFlash() {
        if (!binding.cameraView.isOpened) return

        statusFlash = if (statusFlash != Flash.ON) Flash.ON else Flash.OFF
        updateUI()
    }

    private fun actionTakePhoto() {
        if (!binding.cameraView.isOpened) return

        val action = {
            if (!binding.cameraView.isTakingPicture && !binding.cameraView.isTakingVideo) {
                binding.cameraView.flash = statusFlash
                binding.cameraView.takePicture()
                println("PlansCameraActivity - actionTakePhoto")
            }
        }
        if (binding.cameraView.mode == Mode.VIDEO){
            setCameraMode(Mode.PICTURE)
            lifecycleScope.launch(Dispatchers.IO) {
                delay(300)
                withContext(Dispatchers.Main) {
                    action()
                }
            }
        }else {
            action()
        }
    }

    private fun actionRecordVideo() {
        if (!binding.cameraView.isOpened) return

        val action = {
            if (!binding.cameraView.isTakingVideo) {
                status = Status.RECORDING_VIDEO_START
                if (statusFlash == Flash.ON) {
                    binding.cameraView.flash = Flash.TORCH
                }
                binding.cameraView.takeVideoSnapshot(FileHelper.fileForPlansVideo(), Constants.LIMIT_DURATION_VIDEO)
                println("PlansCameraActivity - actionRecordVideo")
            }
        }

        if (binding.cameraView.mode == Mode.PICTURE){
            setCameraMode(Mode.VIDEO)
            status = Status.RECORDING_VIDEO_START
            lifecycleScope.launch (Dispatchers.IO) {
                delay(300)
                withContext(Dispatchers.Main) {
                    if (status == Status.RECORDING_VIDEO_START) {
                        action()
                    }
                }
            }
        }else {
            action()
        }
    }

    private fun initializeCamera() {
        binding.cameraView.flash = Flash.OFF
        stopRecordVideo()
        endAnim()
        updateData(Status.NORMAL)
    }

    private fun setCameraMode(mode: Mode? = null) {
        binding.apply {
            if (cameraView.isOpened && mode != null && !cameraView.isTakingPicture && !cameraView.isTakingVideo) {
                cameraView.mode = mode
            }
        }
    }

    private fun stopRecordVideo() {
        binding.apply {
            if (cameraView.isOpened && cameraView.isTakingVideo) {
                val now = System.currentTimeMillis()
                val delayMillis = abs(now - timeRecordingStart)
                if ( delayMillis > 1000) {
                    println("PlansCameraActivity - stopRecordVideo - delayMillis-1 : $delayMillis")
                    cameraView.stopVideo()
                }else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        delay((1000 - delayMillis))
                        withContext(Dispatchers.Main) {
                            if (cameraView.isOpened && cameraView.isTakingVideo) {
                                println("PlansCameraActivity - stopRecordVideo - delayMillis-2 : $delayMillis")
                                cameraView.stopVideo()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun actionSwitchCamera() {
        if (!binding.cameraView.isOpened) return

        binding.cameraView.facing = if (binding.cameraView.facing == Facing.FRONT) Facing.BACK else Facing.FRONT
        updateUI()
    }

    private fun actionSend() {
        when(status) {
            Status.PREVIEW_PHOTO -> {
                sendPictureFile(pictureResult)
            }
            Status.PREVIEW_VIDEO -> {
                sendVideoFile(videoResult)
            }
        }
    }

    private fun sendPictureFile(picture: PictureResult?) {
        picture?.let { it ->
            val extension = when (it.format) {
                PictureFormat.JPEG -> "jpg"
                PictureFormat.DNG -> "dng"
                else -> throw RuntimeException("Unknown format.")
            }
            bmpPicture?.toByteArray()?.also { byteArray ->
                CameraUtils.writeToFile(byteArray, FileHelper.fileForPlansImage(extension)) { file ->
                    if (file != null) {
                        finish()
                        MediaPickerHelper.onSelectedPicture(file.path)
                    } else {
                        ToastHelper.showMessage("Error while writing file.")
                    }
                }
            }
        }
    }

    private fun sendVideoFile(video: VideoResult?) {
        video?.let {
            finish()
            MediaPickerHelper.onSelectedVideo(it.file.path, Uri.fromFile(it.file))
        }
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            println("PlansCameraActivity - onCameraOpened")
        }

        override fun onCameraClosed() {
            println("PlansCameraActivity - onCameraClosed")
        }

        override fun onCameraError(exception: CameraException) {
            println("PlansCameraActivity - onCameraError : $exception")
            initializeCamera()
        }

        override fun onPictureTaken(result: PictureResult) {
            println("PlansCameraActivity - onPictureTaken")
            binding.cameraView.flash = Flash.OFF
            updateData(Status.PREVIEW_PHOTO, result)
        }

        override fun onVideoTaken(result: VideoResult) {
            println("PlansCameraActivity - onVideoTaken")
            updateData(Status.PREVIEW_VIDEO, video = result)
        }

        override fun onVideoRecordingStart() {
            println("PlansCameraActivity - onVideoRecordingStart")
            timeRecordingStart = System.currentTimeMillis()
            if (status == Status.RECORDING_VIDEO_START) {
                println("PlansCameraActivity - onVideoRecordingStart - Status : RECORDING_VIDEO_START")
                startAnim()
                updateUI(Status.RECORDING_VIDEO)
            }else {
                println("PlansCameraActivity - onVideoRecordingStart - Status : NORMAL")
                initializeCamera()
            }
        }

        override fun onVideoRecordingEnd() {
            println("PlansCameraActivity - onVideoRecordingEnd")
            binding.cameraView.flash = Flash.OFF
        }

    }
}
