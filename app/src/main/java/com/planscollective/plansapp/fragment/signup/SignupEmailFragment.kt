package com.planscollective.plansapp.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentSignupEmailBinding
import com.planscollective.plansapp.extension.isEmailValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper

class SignupEmailFragment : AuthBaseFragment<FragmentSignupEmailBinding>(), PlansEditTextViewListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.apply {
            // Back Button
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // Email EditView
            etEmail.text = authVM.user.value?.email
            etEmail.listener = this@SignupEmailFragment

            // Continue Button
            tvContinue.setOnSingleClickListener{
                actionContinue()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        binding.apply {
            tvContinue.apply {
                isClickable = if (etEmail.text?.trim()?.isEmailValid() == true) {
                    etEmail.enableValid = true
                    setBackgroundResource(R.drawable.button_bkgnd_white)
                    setTextColor(resources.getColor(R.color.pink))
                    true
                }else {
                    etEmail.enableValid = false
                    setBackgroundResource(R.drawable.button_bkgnd_gray)
                    setTextColor(resources.getColor(R.color.white))
                    false
                }
            }
        }

    }

    private fun actionContinue() {
        hideKeyboard()
        BusyHelper.show(requireContext())
        authVM.verifyEmail { isVerified, message ->
            BusyHelper.hide()
            if (isVerified == true) {
                PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //********************************* PlansEditTextView Listener *************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        authVM.user.value?.email = text?.trim()
        updateUI()
    }


}