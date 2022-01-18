package com.planscollective.plansapp.fragment.settings

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentEditProfileBinding
import com.planscollective.plansapp.extension.formatPhoneNumber
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.EditProfileVM
import com.planscollective.plansapp.webServices.user.UserWebService
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import permissions.dispatcher.*

@RuntimePermissions
class EditProfileFragment : PlansBaseFragment<FragmentEditProfileBinding>(),
    MediaPickerListener, PlansEditTextViewListener {

    private val viewModel: EditProfileVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }
            btnSave.setOnSingleClickListener {
                saveProfile()
            }
            btnUserImage.setOnSingleClickListener {
                showCameraGalleryWithPermissionCheck()
            }
            etFirstName.listener = this@EditProfileFragment
            etLastName.listener = this@EditProfileFragment
            etBio.listener = this@EditProfileFragment
            etLocation.listener = this@EditProfileFragment
        }

        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            if(viewModel.didLoadData.value != true) {
                viewModel.getUserProfile(isShownLoading)
            }
        }
        return isBack
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        binding.apply {
            imvUserImage.setUserImage(viewModel?.user?.profileImage)
            etFirstName.text = viewModel?.user?.firstName
            etLastName.text = viewModel?.user?.lastName
            etBio.text = viewModel?.user?.bio
            etEmail.text = viewModel?.user?.email
            etPhoneNumber.text = viewModel?.user?.mobile?.formatPhoneNumber(
                PLANS_APP.currentActivity,
                numberFormat = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL,
                separator = "-",
                isRemovedOwnCountryCode = true)
        }
        updateLocationUI()
        updateSaveBtnUI()
    }

    private fun updateLocationUI() {
        binding.apply {
            etLocation.text = viewModel?.user?.userLocation?.takeIf { it.isNotEmpty() }?.let {
                etLocation.btnClear?.setImageResource(R.drawable.ic_x_grey)
                it.removeOwnCountry()
            } ?: run {
                etLocation.btnClear?.setImageResource(R.drawable.ic_crosshair_circle_black)
                ""
            }
            etLocation.btnClear?.visibility = View.VISIBLE
        }
    }

    private fun updateSaveBtnUI() {
        binding.btnSave.visibility = if (viewModel.validateUserInfo()) View.VISIBLE else View.INVISIBLE
    }

    private fun saveProfile() {
        hideKeyboard()
        BusyHelper.show(requireContext())
        UserWebService.updateUser(viewModel.user) { newUser, message ->
            BusyHelper.hide()
            if (newUser != null) {
                UserInfo.updateUserInfoForEditProfile(newUser)
                ToastHelper.showMessage("Your profile has been updated.")
                gotoBack()
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //******************************* Permissions for Camera and Gallery **************************************//
    @NeedsPermission(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showCameraGallery() {
        MediaPickerHelper.showPickerOptions(this, this, MediaPickerHelper.PickerType.DEFAULT_IMAGE)
    }

    @OnShowRationale(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForPermissions(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    @OnNeverAskAgain(
        Manifest.permission.CAMERA,
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

    //*********************************** MediaPickerListener ************************************//

    override fun onSelectedPhoto(filePath: String?) {
        viewModel.user?.profileImage = filePath
        updateUI()
    }

    override fun onSelectedVideo(filePath: String?) {
    }

    //*********************************** PlansEditTextViewListener ************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        binding.apply {
            when(editText) {
                etFirstName -> {
                    viewModel?.user?.firstName = text
                    updateSaveBtnUI()

                }
                etLastName -> {
                    viewModel?.user?.lastName = text
                    updateSaveBtnUI()
                }
                etBio -> {
                    val strBio = if (text != null && text.length > Constants.LIMIT_PROFILE_BIO_MAX) {
                        editText.text = viewModel?.user?.bio
                        editText.text
                    } else text

                    viewModel?.user?.bio = strBio
                    updateSaveBtnUI()
                }
            }
        }
    }

    override fun didClickedClearBtn(editText: PlansEditTextView?): Boolean {
        var result = false
        binding.apply {
            when(editText) {
                etLocation -> {
                    if (etLocation.text.isNullOrEmpty()) {
                        viewModel?.user?.userLocation = if (!UserInfo.userAddressLocality.isNullOrEmpty() && !UserInfo.userAddressAdminArea.isNullOrEmpty())
                            "${UserInfo.userAddressLocality}, ${UserInfo.userAddressAdminArea}" else ""

                        viewModel?.user?.lat = UserInfo.latitude
                        viewModel?.user?.long = UserInfo.longitude
                    }else {
                        viewModel?.user?.userLocation = ""
                        viewModel?.user?.lat = 0
                        viewModel?.user?.long = 0
                    }
                    updateLocationUI()
                    updateSaveBtnUI()
                    result = true
                }
            }
        }
        return result
    }



}