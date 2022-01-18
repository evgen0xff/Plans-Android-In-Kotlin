package com.planscollective.plansapp.fragment.settings

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentSendFeedbackBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.models.viewModels.SendFeedbackVM
import com.planscollective.plansapp.webServices.settings.SettingsWebService
import permissions.dispatcher.*

@RuntimePermissions
class SendFeedbackFragment : PlansBaseFragment<FragmentSendFeedbackBinding>(),
    MediaPickerListener, PlansEditTextViewListener {

    enum class Type {
        SEND_FEEDBACK {
            override val type = "feedback"
            override val title = "Send Feedback"
            override val hintText = "Feedback"
        },
        REPORT_PROBLEM {
            override val type = "report"
            override val title = "Report a Problem"
            override val hintText = "What went wrong?"
        };

        abstract val type : String
        abstract val title : String
        abstract val hintText : String
    }

    private val viewModel: SendFeedbackVM by viewModels()
    private val args: SendFeedbackFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendFeedbackBinding.inflate(inflater, container, false)
        args.type?.takeIf { it.isNotEmpty() }?.also {
            viewModel.type = Type.valueOf(it)
        }
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }
            btnSend.setOnSingleClickListener {
                hideKeyboard()
                sendFeedback()
            }

            tvDescription.visibility = if (viewModel?.type == Type.SEND_FEEDBACK) View.VISIBLE else View.GONE

            layoutScreenshotEmpty.setOnSingleClickListener {
                hideKeyboard()
                showGalleryWithPermissionCheck()
            }

            layoutScreenshot.setOnSingleClickListener {
                hideKeyboard()
                gotoOpenPhoto(viewModel?.fileScreenshot)
            }

            btnCancelScreenshot.setOnSingleClickListener {
                hideKeyboard()
                viewModel?.fileScreenshot = null
                updateUI()
            }

            btnAddScreenshot.setOnSingleClickListener {
                hideKeyboard()
                showGalleryWithPermissionCheck()
            }

            etFeedback.hintText = viewModel?.type?.hintText
            etFeedback.listener = this@SendFeedbackFragment
        }

        updateUI()
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        binding.apply {
            etFeedback.text = viewModel?.feedback
            layoutScreenshot.visibility = if (viewModel?.fileScreenshot.isNullOrEmpty()) {
                btnAddScreenshot.visibility = View.VISIBLE
                View.GONE
            } else {
                btnAddScreenshot.visibility = View.GONE
                imvScreenshot.setEventImage(viewModel?.fileScreenshot)
                View.VISIBLE
            }
        }
        updateSendBtnUI()
    }


    private fun updateSendBtnUI() {
        binding.btnSend.visibility = if (viewModel.validate()) View.VISIBLE else View.INVISIBLE
    }

    private fun sendFeedback() {
        BusyHelper.show(requireContext())
        SettingsWebService.sendFeedback(viewModel.feedback, viewModel.type.type, viewModel.fileScreenshot) {
            success, message ->
            BusyHelper.hide()
            if (success == true) {
                var msg = "Thank you for your help!\n"
                msg += if (viewModel.type == Type.SEND_FEEDBACK) {
                    "Your feedback has been successfully submitted."
                }else {
                    "Your reported problem is submitted."
                }
                ToastHelper.showMessage(msg)
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }

    }

    //******************************* Permissions for Camera and Gallery **************************************//
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showGallery() {
        MediaPickerHelper.openMediaGallery(this, MediaPickerHelper.PickerType.DEFAULT_IMAGE)
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForPermissions(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        ToastHelper.showMessage("Permission denied for Gallery, you can turn them on on Settings.")
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onPermissionNeverAskAgain() {
        ToastHelper.showMessage("Permission denied for Gallery, you can turn them on on Settings.")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    //*********************************** MediaPickerListener ************************************//

    override fun onSelectedPhoto(filePath: String?) {
        viewModel.fileScreenshot = filePath
        updateUI()
    }

    override fun onSelectedVideo(filePath: String?) {
    }

    //*********************************** PlansEditTextViewListener ************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        binding.apply {
            when(editText) {
                etFeedback -> {
                    viewModel?.feedback = text
                    updateSendBtnUI()
                }
            }
        }
    }

}