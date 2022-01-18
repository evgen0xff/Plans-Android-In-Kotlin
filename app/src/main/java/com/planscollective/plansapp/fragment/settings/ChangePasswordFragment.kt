package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentChangePasswordBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.viewModels.ChangePasswordVM
import com.planscollective.plansapp.webServices.user.UserWebService

class ChangePasswordFragment : PlansBaseFragment<FragmentChangePasswordBinding>(), PlansEditTextViewListener {

    private val viewModel: ChangePasswordVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
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
                savePassword()
            }

            etCurrentPassword.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etNewPassword.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etConfirmPassword.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            etCurrentPassword.listener = this@ChangePasswordFragment
            etNewPassword.listener = this@ChangePasswordFragment
            etConfirmPassword.listener = this@ChangePasswordFragment
        }

        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            updateUI()
        }
        return isBack
    }

    private fun updateUI() {
        binding.viewModel = viewModel
        binding.apply {
            etCurrentPassword.text = viewModel?.currentPassword
            etNewPassword.text = viewModel?.newPassword
            etConfirmPassword.text = viewModel?.confirmPassword
        }
        updateSaveBtnUI()
    }

    private fun updateSaveBtnUI() {
        binding.btnSave.visibility = if (viewModel.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    private fun savePassword() {
        hideKeyboard()
        if (viewModel.validate()) {
            BusyHelper.show(requireContext())
            UserWebService.changePasswordWithOldPassword(viewModel.currentPassword, viewModel.newPassword) {
                success, message ->
                BusyHelper.hide()
                ToastHelper.showMessage(message)
                if (success == true) {
                    gotoBack()
                }
            }
        }
    }

    //*********************************** PlansEditTextViewListener ************************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        binding.apply {
            when(editText) {
                etCurrentPassword -> {
                    viewModel?.currentPassword = text
                    updateSaveBtnUI()
                }
                etNewPassword -> {
                    viewModel?.newPassword = text
                    updateSaveBtnUI()
                }
                etConfirmPassword -> {
                    viewModel?.confirmPassword = text
                    updateSaveBtnUI()
                }
            }
        }
    }


}