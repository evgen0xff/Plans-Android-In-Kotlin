package com.planscollective.plansapp.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.databinding.FragmentConfirmCodeBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.helper.ToastHelper

class ConfirmCodeFragment : AuthBaseFragment<FragmentConfirmCodeBinding>() {

    private val args: ConfirmCodeFragmentArgs by navArgs()
    private var codePin: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentConfirmCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            // Nav Bar
            binding.btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // Description
            tvDescription.text = getString(R.string.confirm_code_description) + " " + args.destination.toString()

            // Pin View
            pinView.itemWidth = ((OSHelper.widthScreen - (30 + 5 * 3).toPx()) / 6.0).toInt()
            pinView.addTextChangedListener {
                updateUI()
                confirmCode(it.toString(), true)
            }

            // Resend Code view
            tvResendCode.setOnSingleClickListener {
                resendCode()
            }

            // Continue View
            tvContinue.setOnSingleClickListener {
                confirmCode(pinView.text.toString())
            }
        }
        updateUI()
    }

    private fun updateUI() {
        binding.apply{
            tvContinue.apply {
                if (pinView.text.toString().length == 6) {
                    isClickable = true
                    setBackgroundResource(R.drawable.button_bkgnd_white)
                    setTextColor(resources.getColor(R.color.pink))
                }else {
                    isClickable = false
                    setBackgroundResource(R.drawable.button_bkgnd_gray)
                    setTextColor(resources.getColor(R.color.white))
                }
            }
        }
    }

    private fun confirmCode(code: String, isCheckOld: Boolean = false) {
        if (isCheckOld && codePin == code) return
        if (code.length != 6) return

        codePin = code

        hideKeyboard()

        BusyHelper.show(requireContext())
        authVM.verifyOtp(code, callBack = { isVerified, message ->
            BusyHelper.hide()
            if (isVerified == true) {
                (requireActivity() as BaseActivity<*>).showOverlay("Validation Successful", delayForHide = 1000){
                    when(args.from) {
                        "ResetPassword" -> {
                            navigate(R.id.action_confirmCodeFragment_to_newPasswordFragment)
                        }
                        "Signup" -> {
                            PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
                        }
                    }
                }
            }else {
                ToastHelper.showMessage(message)
            }
        })
    }

    private fun resendCode() {
        hideKeyboard()
        var isCreateAccount = false
        when(args.from) {
            "ResetPassword" -> {
                isCreateAccount = false
            }
            "Signup" -> {
                isCreateAccount = true
            }
        }

        BusyHelper.show(requireContext())
        authVM.sendOtp(isCreateAccount){ otpCode, message ->
            BusyHelper.hide()
            if (otpCode.isNullOrEmpty()) {
                ToastHelper.showMessage(message)
            }else {
                authVM.codeOTP = otpCode
                (requireActivity() as BaseActivity<*>).showOverlay("Verification Code Sent\nSuccessfully")
            }
        }
    }


}