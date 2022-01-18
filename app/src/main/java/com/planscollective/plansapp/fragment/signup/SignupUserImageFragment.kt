package com.planscollective.plansapp.fragment.signup

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.databinding.FragmentSignupUserimageBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ImageHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener

//@RuntimePermissions
class SignupUserImageFragment : AuthBaseFragment<FragmentSignupUserimageBinding>(),
    OnSingleClickListener, MediaPickerListener {

    private var profileImageUri: String? = null
    private var bmpProfile: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupUserimageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeData() {
        super.initializeData()
        profileImageUri?.let {
            bmpProfile = ImageHelper.getResizedBitmap(profileImageUri)
        }
    }

    override fun setupUI() {
        super.setupUI()

        // Back Button
        binding.btnSkip.setOnSingleClickListener (this)
        binding.imvUserImage.setOnSingleClickListener(this)
        binding.tvAddPhoto.setOnSingleClickListener(this)

        updateUI()
    }

    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnSkip ->  {
                actionSkip()
            }
            binding.imvUserImage -> {
                MediaPickerHelper.showPickerOptions(this, this, MediaPickerHelper.PickerType.DEFAULT_IMAGE)
            }
            binding.tvAddPhoto -> {
                if (profileImageUri != null) {
                    uploadUserImage()
                }else if (!authVM.user.value?.profileImage.isNullOrEmpty()) {
                    actionSkip()
                }else {
                    MediaPickerHelper.showPickerOptions(this, this, MediaPickerHelper.PickerType.DEFAULT_IMAGE)
                }
            }
        }
    }

    override fun onSelectedPhoto(filePath: String?) {
        profileImageUri = filePath
        bmpProfile = ImageHelper.getResizedBitmap(profileImageUri)
        updateUI()
    }

    override fun onSelectedVideo(filePath: String?) {

    }

    private fun updateUI() {
        bmpProfile?.let {
            binding.imvUserImage.setImageBitmap(bmpProfile)
        } ?: run {
            binding.imvUserImage.setUserImage(authVM.user.value?.profileImage, defaultImage = R.drawable.ic_user_outline_white)
        }

        if (profileImageUri != null || authVM.user.value?.profileImage != null) {
            binding.tvAddPhoto.text = "CONTINUE"
        }else {
            binding.tvAddPhoto.text = "ADD A PHOTO"
        }
    }

    private fun uploadUserImage() {
        BusyHelper.show(requireContext())
        authVM.updateUserImage(requireContext(), profileImageUri){ user, msg ->
            BusyHelper.hide()
            if (user != null) {
                PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
            }else {
                ToastHelper.showMessage(msg)
            }
        }
    }

    private fun actionSkip() {
        PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
    }

}