package com.planscollective.plansapp.fragment.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentNewPasswordBinding
import com.planscollective.plansapp.extension.isPasswordValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper

class NewPasswordFragment : AuthBaseFragment<FragmentNewPasswordBinding>(), PlansEditTextViewListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            // Back Button
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // New Password
            etNewPassword.listener = this@NewPasswordFragment
            etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etNewPassword.textAction = "Show"

            // Confirm Password
            etConfirmPassword.listener = this@NewPasswordFragment
            etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etConfirmPassword.textAction = "Show"

            // Change Password
            tvChangePass.setOnSingleClickListener {
                changePassword()
            }

        }
        updateUI()
    }

    private fun updateUI() {
        binding.apply {
            // New Password
            val newPassword = etNewPassword.text.toString()
            val isValidNewPass = newPassword.isPasswordValid()

            // Confirm Password
            val confirmPassword = etConfirmPassword.text.toString()
            val isValidConfirmPass = confirmPassword.isPasswordValid()

            // Change Password
            tvChangePass.apply {
                isClickable = if (isValidNewPass && isValidConfirmPass && newPassword == confirmPassword) {
                    setBackgroundResource(R.drawable.button_bkgnd_white)
                    setTextColor(resources.getColor(R.color.pink))
                    true
                }else {
                    setBackgroundResource(R.drawable.button_bkgnd_gray)
                    setTextColor(resources.getColor(R.color.white))
                    false
                }
            }
        }
    }

    private fun changeInputType(editText: PlansEditTextView?) {
        editText?.inputType = if (editText?.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
            editText.textAction = "Show"
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            editText?.textAction = "Hide"
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
    }

    private fun changePassword() {
        hideKeyboard()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        BusyHelper.show(requireContext())
        authVM.changePassword(newPassword, confirmPassword){ user, message ->
            BusyHelper.hide()
            if (user != null) {
                (requireActivity() as BaseActivity<*>).showOverlay("Your password has been updated", delayForHide = 1000){
                    gotoBack(R.id.landingFragment)
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }


    // ********************************* PlansEditTextView Listener **************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        updateUI()
    }

    override fun didClickedAction(editText: PlansEditTextView?){
        changeInputType(editText)
    }

}