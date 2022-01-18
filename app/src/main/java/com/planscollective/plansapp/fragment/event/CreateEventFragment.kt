package com.planscollective.plansapp.fragment.event

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentCreateEventBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.fragment.utils.PlansDialogFragment
import com.planscollective.plansapp.helper.KeyboardHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.OnKeyboardListener
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.models.viewModels.CreateEventVM
import permissions.dispatcher.*

@RuntimePermissions
class CreateEventFragment : PlansBaseFragment<FragmentCreateEventBinding>(), MediaPickerListener,
    PlansEditTextViewListener, OnKeyboardListener {

    private val viewModel : CreateEventVM by navGraphViewModels(R.id.createEventFragment)
    private val args: CreateEventFragmentArgs by navArgs()
    override var screenName: String? = "CreateEvent_Screen_1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        viewModel.initialize(args.placeModel)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            // Back button
            btnBack.setOnSingleClickListener(this@CreateEventFragment)

            // Event Cover Image/Video UI
            layoutEventCover.setOnSingleClickListener(this@CreateEventFragment)

            // Event Name/Details
            etEventName.listener = this@CreateEventFragment
            etDetails.listener = this@CreateEventFragment

            // Continue Button
            btnContinue.setOnSingleClickListener(this@CreateEventFragment)
        }

        // Keyboard Listener
        KeyboardHelper.listenKeyboardEvent(this, this)

        updateUI()
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        // Event Cover - Image/Video
        updateEventCoverUI()

        // Edit Texts UI - Event Name, Event Details, Event Caption
        updateEditTextsUI()

        // Continue Button UI
        updateContinueUI()
    }

    private fun updateContinueUI () {
        binding.apply {
            btnContinue.isClickable = if (validateData()) {
                btnContinue.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_purple)
                true
            }else {
                btnContinue.background = ContextCompat.getDrawable(PLANS_APP, R.drawable.button_bkgnd_gray)
                false
            }
        }
    }

    private fun validateData(isShownAlert: Boolean = false) : Boolean {
        var result = true
        var errMsg : String? = null
        if (result && viewModel.eventModel.imageOrVideo.isNullOrEmpty()) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_IMAGE_VIDEO
        }

        if (result && binding.etEventName.text.isNullOrEmpty()) {
            result = false
            errMsg = ConstantTexts.YOUR_EVENT_NEED_EVENT_NAME
        }

        if (!result && isShownAlert) {
            ToastHelper.showMessage(errMsg)
        }

        return result
    }


    private fun updateEventCoverUI() {
        val event = viewModel.eventModel ?: return
        binding.apply {
            // Event Cover Image/Video
            videoView.visibility = if (event.mediaType == "video"){
                videoView.playVideoUrl(event.imageOrVideo, event.thumbnail)
                View.VISIBLE
            }else {
                imgvEventCover.visibility = if(event.imageOrVideo.isNullOrEmpty()) View.GONE else {
                    imgvEventCover.setEventImage(event.imageOrVideo)
                    View.VISIBLE
                }
                View.GONE
            }
        }
    }

    private fun updateEditTextsUI() {
        binding.etEventName.text = viewModel.eventModel.eventName
        binding.etDetails.text = viewModel.eventModel.details
    }

    //******************************* Permissions for Camera and Gallery **************************************//
    @NeedsPermission(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showCameraGallery() {
        MediaPickerHelper.showPickerOptions(this, this, MediaPickerHelper.PickerType.EVENT_COVER)
    }

    @OnShowRationale(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForPermissions(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionNeverAskAgain() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    //********************************* PlansEditTextViewListener **************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        when(editText) {
            binding.etEventName -> {
                updateContinueUI ()
            }
            else -> {}
        }
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    if (viewModel?.eventModel?.imageOrVideo.isNullOrEmpty() &&
                        etEventName.text.isNullOrEmpty() &&
                        etDetails.text.isNullOrEmpty()) {
                        gotoBack()
                    }else {
                        PlansDialogFragment(ConstantTexts.DISCARD_EVENT){
                            gotoBack()
                        }.show(requireActivity().supportFragmentManager, "CreateEventFragment")
                    }
                }
                layoutEventCover -> {
                    showCameraGallery()
                }
                btnContinue -> {
                    viewModel?.eventModel?.eventName = etEventName.text
                    viewModel?.eventModel?.details = etDetails.text
                    val action = NavDashboardDirections.actionGlobalCreateEventProgress1Fragment()
                    navigate(directions = action)
                }
            }
        }
    }

    //********************************* MediaPickerListener **************************************//
    override fun onSelectedPhoto(filePath: String?) {
        viewModel.eventModel.imageOrVideo = filePath
        viewModel.eventModel.mediaType = "image"
        updateEventCoverUI()
        updateContinueUI ()
    }

    override fun onSelectedVideo(filePath: String?) {
        viewModel.eventModel.imageOrVideo = filePath
        viewModel.eventModel.mediaType = "video"
        updateEventCoverUI()
        updateContinueUI ()
    }

    //************************************* Keyboard Listener ***********************************//
    override fun onShownKeyboard() {
        super.onShownKeyboard()
        binding.apply {
            scrollView.scrollTo(0, scrollView.bottom)
        }
    }

}