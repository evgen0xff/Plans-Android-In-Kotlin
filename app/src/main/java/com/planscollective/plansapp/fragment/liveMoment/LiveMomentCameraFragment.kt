package com.planscollective.plansapp.fragment.liveMoment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.MediaController
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.PictureFormat
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.FragmentLiveMomentCameraBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.*
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.DashboardActivityVM
import com.planscollective.plansapp.models.viewModels.LiveMomentCameraVM
import com.planscollective.plansapp.models.viewModels.LiveMomentCameraVM.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs

class LiveMomentCameraFragment : PlansBaseFragment<FragmentLiveMomentCameraBinding>(),
    View.OnLongClickListener,
    View.OnTouchListener, OnKeyboardListener
{

    private val viewModel: LiveMomentCameraVM by viewModels()
    private val args: LiveMomentCameraFragmentArgs by navArgs()
    private val dashboardVM: DashboardActivityVM by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this){
            actionCancel()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLiveMomentCameraBinding.inflate(inflater, container, false)
        viewModel.caption.observe(viewLifecycleOwner, {
            updateCaptionView(it)
        })
        viewModel.eventId = args.eventId
        viewModel.eventName = args.eventName
        viewModel.isLive = args.isLive

        binding.viewModel = viewModel

        KeyboardHelper.listenKeyboardEvent(this, this)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupUI() {
        super.setupUI()
        binding.let {
            // Camera View
            setupCameraView(OSHelper.widthScreen, OSHelper.heightScreen)

            // Close Button
            it.layoutRightNavItem.setOnSingleClickListener(this)

            // Live Mark
            it.imgvLive.visibility = if (viewModel.isLive) View.VISIBLE else View.INVISIBLE
            if (it.imgvLive.visibility == View.VISIBLE) {
                val anim = AnimationUtils.loadAnimation(it.imgvLive.context, R.anim.blinking_live)
                it.imgvLive.startAnimation(anim)
            }else {
                it.imgvLive.clearAnimation()
            }

            // Others
            it.videoView.setOnSingleClickListener(this)
            it.layoutPlay.setOnSingleClickListener(this)
            it.layoutFlash.setOnSingleClickListener(this)
            it.imvRotation.setOnSingleClickListener(this)
            it.btnSend.setOnSingleClickListener(this)

            // Record Button
            it.provRecord.setTotalDuration(Constants.LIMIT_DURATION_VIDEO.toLong())
            it.provRecord.setTintColor(PlansColor.TEAL_MAIN)
            it.provRecord.setOnSingleClickListener(this)
            it.provRecord.setOnTouchListener(this)
            it.provRecord.setOnLongClickListener(this)
        }

        setupVideoView()
        initializeCamera()
        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            saveInfo(viewModel.eventId, UserInfo.userId)
        }
        return isBack
    }

    override fun onResume() {
        super.onResume()
        ToastHelper.isLoadingToastOn.value = false
    }

    override fun onPause() {
        super.onPause()
        ToastHelper.isLoadingToastOn.value = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.pictureResult = null
            viewModel.videoResult = null
        }
    }

    //*********************************** OnKeyboardListener ************************************//
    override fun onHiddenKeyboard() {
        binding.layoutCaption.setLayoutMargin(bottom = 24.toPx())
    }

    override fun onShownKeyboard() {
        binding.layoutCaption.setLayoutMargin(bottom = 0.toPx())
    }

    //*********************************** OnSingleClickListener ************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.layoutRightNavItem -> {
                actionCancel()
            }
            binding.layoutPlay -> {
                actionPlay()
            }
            binding.videoView -> {
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
            binding.btnSend -> {
                actionSend()
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when(v) {
            binding.provRecord -> {
                println("LiveMomentCameraFragment : onLongClick")
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
                        result = when(viewModel.status) {
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

    private fun initializeCamera() {
        binding.cameraView.flash = Flash.OFF
        binding.cameraView.playSounds = false
        stopRecordVideo()
        endAnim()
        updateMedia(Status.NORMAL)
    }

    private fun setupCameraView(widthPixel: Int, heightPixel: Int) {
        val width = SizeSelectors.minWidth(widthPixel)
        val height = SizeSelectors.minHeight(heightPixel)
        val dimensions = SizeSelectors.and(width, height)                                           // Matches bigger than Screen size.
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
            cameraView.setLifecycleOwner(viewLifecycleOwner)
            cameraView.addCameraListener(Listener())
        }
    }

    private fun setCameraMode(mode: Mode? = null) {
        binding.apply {
            if (cameraView.isOpened && mode != null && !cameraView.isTakingVideo && !cameraView.isTakingPicture) {
                cameraView.mode = mode
            }
        }
    }

    private fun setupVideoView() {
        val controller = MediaController(requireContext())
        controller.setAnchorView(binding.videoView)
        controller.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(controller)
        controller.visibility = View.INVISIBLE
        binding.videoView.setOnCompletionListener {
            viewModel.status = Status.PREVIEW_VIDEO
            updateUI()
        }
    }

    private fun updateMedia(status: Status = Status.NORMAL,
                            picture: PictureResult? = null,
                            video: VideoResult? = null) {
        viewModel.status = status
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
                        viewModel.bmpPicture = bitmap
                        withContext(Dispatchers.Main) {
                            binding.imvPreviewPhoto.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: UnsupportedOperationException) {
                viewModel.bmpPicture = null
                binding.imvPreviewPhoto.setImageBitmap(null)
            }
        } ?: run {
            viewModel.bmpPicture = null
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

        viewModel.pictureResult = picture
        viewModel.videoResult = video

        updateUI()
    }

    private fun updateUI(status: Status? = null) {
        viewModel.status = status ?: viewModel.status
        if (viewModel.videoResult?.file?.exists() == false ) {
            binding.videoView.stopPlayback()
            binding.videoView.setVideoURI(null)
        }

        if (viewModel.statusFlash == Flash.ON) {
            binding.imvFlash.setImageResource(R.drawable.ic_flash_on)
        }else {
            binding.imvFlash.setImageResource(R.drawable.ic_flash_off)
        }
        val valueStatus = viewModel.status
        binding.apply {
            when(valueStatus) {
                Status.NORMAL -> {
                    tvTitle.visibility = View.VISIBLE
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.VISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutCaption.visibility = View.INVISIBLE
                    layoutCameraControl.visibility = View.VISIBLE
                    provRecord.visibility = View.VISIBLE
                    layoutFlash.visibility = View.VISIBLE
                    imvRotation.visibility = View.VISIBLE
                }
                Status.RECORDING_VIDEO -> {
                    tvTitle.visibility = View.INVISIBLE
                    layoutRightNavItem.visibility = View.INVISIBLE
                    cameraView.visibility = View.VISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutCaption.visibility = View.INVISIBLE
                    layoutCameraControl.visibility = View.VISIBLE
                    provRecord.visibility = View.VISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PREVIEW_PHOTO -> {
                    tvTitle.visibility = View.VISIBLE
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.VISIBLE
                    videoView.visibility = View.INVISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutCaption.visibility = View.VISIBLE
                    layoutCameraControl.visibility = View.INVISIBLE
                    provRecord.visibility = View.INVISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PREVIEW_VIDEO -> {
                    tvTitle.visibility = View.VISIBLE
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.VISIBLE
                    layoutPlay.visibility = View.VISIBLE
                    layoutCaption.visibility = View.VISIBLE
                    layoutCameraControl.visibility = View.INVISIBLE
                    provRecord.visibility = View.INVISIBLE
                    layoutFlash.visibility = View.INVISIBLE
                    imvRotation.visibility = View.INVISIBLE
                }
                Status.PLAYING_VIDEO -> {
                    tvTitle.visibility = View.VISIBLE
                    layoutRightNavItem.visibility = View.VISIBLE
                    cameraView.visibility = View.INVISIBLE
                    imvPreviewPhoto.visibility = View.INVISIBLE
                    videoView.visibility = View.VISIBLE
                    layoutPlay.visibility = View.INVISIBLE
                    layoutCaption.visibility = View.INVISIBLE
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

    private fun updateCaptionView(caption: String?){
        var height = (caption?.getHeight(requireContext(),
            16,
            OSHelper.widthScreen - (16 + 50).toPx(), R.font.product_sans_regular) ?: 0) + 16.toPx()
        height = if (height > 150.toPx()) {
            150.toPx()
        }else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.layoutCaption.setLayoutHeight(height)
    }

    private fun startAnim() {
        binding.provRecord.startAnim()
    }

    private fun endAnim() {
        binding.provRecord.stopAnim()
    }

    private fun actionCancel() {
        hideKeyboard()
        binding.videoView.stopPlayback()
        when(viewModel.status) {
            Status.NORMAL, Status.RECORDING_VIDEO -> {
                gotoBack()
            }
            Status.PREVIEW_VIDEO, Status.PREVIEW_PHOTO, Status.PLAYING_VIDEO -> {
                initializeCamera()
            }
        }
    }

    private fun actionPlay() {
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
            updateUI(Status.PLAYING_VIDEO)
        }
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

        viewModel.statusFlash = if (viewModel.statusFlash == Flash.ON) Flash.OFF else Flash.ON
        updateUI()
    }

    private fun actionTakePhoto() {
        if (!binding.cameraView.isOpened) return

        val action = {
            if (!binding.cameraView.isTakingPicture && !binding.cameraView.isTakingVideo) {
                binding.cameraView.flash = viewModel.statusFlash
                binding.cameraView.takePicture()
                println("LiveMomentCameraFragment - actionTakePhoto")
            }
        }

        if (binding.cameraView.mode == Mode.VIDEO){
            setCameraMode(Mode.PICTURE)
            lifecycleScope.launch (Dispatchers.IO) {
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
                viewModel.status = Status.RECORDING_VIDEO_START
                if (viewModel.statusFlash == Flash.ON) {
                    binding.cameraView.flash = Flash.TORCH
                }
                binding.cameraView.takeVideoSnapshot(FileHelper.fileForPlansVideo(), Constants.LIMIT_DURATION_VIDEO)
                println("LiveMomentCameraFragment - actionRecordVideo")
            }
        }
        if (binding.cameraView.mode == Mode.PICTURE){
            setCameraMode(Mode.VIDEO)
            viewModel.status = Status.RECORDING_VIDEO_START
            lifecycleScope.launch (Dispatchers.IO) {
                delay(300)
                withContext(Dispatchers.Main) {
                    if (viewModel.status == Status.RECORDING_VIDEO_START) {
                        action()
                    }
                }
            }
        }else {
            action()
        }
    }

    private fun stopRecordVideo() {
        binding.apply {
            if (cameraView.isOpened && cameraView.isTakingVideo) {
                val now = System.currentTimeMillis()
                val delayMillis = abs(now - (viewModel?.timeRecordingStart ?: 0))
                if ( delayMillis > 1000) {
                    println("LiveMomentCameraFragment - stopRecordVideo - delayMillis-1 : $delayMillis")
                    cameraView.stopVideo()
                }else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        delay((1000 - delayMillis))
                        withContext(Dispatchers.Main) {
                            if (cameraView.isOpened && cameraView.isTakingVideo) {
                                println("LiveMomentCameraFragment - stopRecordVideo - delayMillis-2 : $delayMillis")
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
        when(viewModel.status) {
            Status.PREVIEW_PHOTO -> {
                sendPictureFile(viewModel.pictureResult)
            }
            Status.PREVIEW_VIDEO -> {
                sendVideoFile(viewModel.videoResult)
            }
        }
    }

    private fun sendPictureFile(picture: PictureResult?) {
        picture?.let {
            val extension = when (it.format) {
                PictureFormat.JPEG -> "jpg"
                PictureFormat.DNG -> "dng"
                else -> throw RuntimeException("Unknown format.")
            }
            viewModel.bmpPicture?.toByteArray()?.also { byteArray ->
                CameraUtils.writeToFile(byteArray, FileHelper.fileForPlansImage(extension)) { file ->
                    if (file != null) {
                        gotoBack()
                        dashboardVM.createLiveMoment(viewModel.eventId, file.path, caption = viewModel.caption.value)
                    } else {
                        ToastHelper.showMessage("Error while writing file.")
                    }
                }
            }
        }
    }

    private fun sendVideoFile(video: VideoResult?) {
        video?.let {
            gotoBack()
            dashboardVM.createLiveMoment(viewModel.eventId, videoFilePath = it.file.path, caption = viewModel.caption.value)
        }
    }

    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            println("LiveMomentCameraFragment - onCameraOpened")
        }

        override fun onCameraClosed() {
            println("LiveMomentCameraFragment - onCameraClosed")
        }

        override fun onCameraError(exception: CameraException) {
            println("LiveMomentCameraFragment - onCameraError : $exception")
            initializeCamera()
        }

        override fun onPictureTaken(result: PictureResult) {
            println("LiveMomentCameraFragment - onPictureTaken")
            binding.cameraView.flash = Flash.OFF
            updateMedia(Status.PREVIEW_PHOTO, result)
        }

        override fun onVideoTaken(result: VideoResult) {
            println("LiveMomentCameraFragment - onVideoTaken")
            updateMedia(Status.PREVIEW_VIDEO, video = result)
        }

        override fun onVideoRecordingStart() {
            println("LiveMomentCameraFragment - onVideoRecordingStart")
            viewModel.timeRecordingStart = System.currentTimeMillis()
            if (viewModel.status == Status.RECORDING_VIDEO_START) {
                println("onVideoRecordingStart - Status : RECORDING_VIDEO_START")
                startAnim()
                updateUI(Status.RECORDING_VIDEO)
            }else {
                println("onVideoRecordingStart - Status : NORMAL")
                initializeCamera()
            }
        }

        override fun onVideoRecordingEnd() {
            println("LiveMomentCameraFragment - onVideoRecordingEnd")
            binding.cameraView.flash = Flash.OFF
        }

    }
}