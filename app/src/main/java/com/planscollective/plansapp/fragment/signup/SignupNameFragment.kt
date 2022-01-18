package com.planscollective.plansapp.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentSignupNameBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment

class SignupNameFragment : AuthBaseFragment<FragmentSignupNameBinding>(), PlansEditTextViewListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            // Back Button
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // First Name
            etFirstName.text = authVM.user.value?.firstName
            etFirstName.listener = this@SignupNameFragment

            // Last Name
            etLastName.text = authVM.user.value?.lastName
            etLastName.listener = this@SignupNameFragment

            // Continue View
            tvContinue.setOnSingleClickListener{
                actionContinue()
            }
        }

        updateUI()
    }


    private fun updateUI() {
        binding.apply {
            // First Name
            val firstName = etFirstName.text?.trim()

            // Last Name
            val lastName = etLastName.text?.trim()

            // Continue View
            tvContinue.apply {
                isClickable = if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
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

    private fun actionContinue() {
        hideKeyboard()
        PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
    }

    //****************************** PlansEditTextView Listener ******************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        binding.apply {
            when(editText) {
                etFirstName -> {
                    authVM.user.value?.firstName = text?.trim()
                }
                etLastName -> {
                    authVM.user.value?.lastName = text?.trim()
                }
            }
        }
        updateUI()
    }



}