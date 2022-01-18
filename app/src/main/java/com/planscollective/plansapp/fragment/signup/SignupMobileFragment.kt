package com.planscollective.plansapp.fragment.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.databinding.FragmentSignupMobileBinding
import com.planscollective.plansapp.extension.getMaskForMobile
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener.Companion.installOn
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import java.util.ArrayList

class SignupMobileFragment : AuthBaseFragment<FragmentSignupMobileBinding>(), MaskedTextChangedListener.ValueListener{

    var countryCode = "US"
    var phoneCode = "+1"
    var destination: String = ""
    var phoneFormatted = ""
    var phoneExtracted = ""
    var listener: MaskedTextChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupMobileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        curBackStackEntry?.savedStateHandle?.getLiveData<String>(Keys.SELECTED_COUNTRY)?.observe(this){
            changeCountry(it)
        }

        binding.apply {
            // Nav Bar
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // Phone edit text
            setupMobileEditText()

            // Country code button
            tvCountryCode.setOnSingleClickListener {
                actionCountryCode()
            }

            // Continue button
            tvContinue.setOnSingleClickListener {
                actionContinue()
            }

            etMobile.setText(authVM.user.value?.localMobile())
        }

        updateUI()
    }

    private fun setupMobileEditText() {
        binding.etMobile.setText("")

        binding.etMobile.removeTextChangedListener(listener)
        binding.etMobile.onFocusChangeListener = null

        val primaryFormat = phoneCode.getMaskForMobile("0", countryCode, requireContext(), true)
        listener = installOn(binding.etMobile, primaryFormat, this)

        binding.etMobile.hint = listener?.placeholder()
    }

    private fun updateUI() {
        // Country Code Text
        binding.tvCountryCode.text = "$countryCode $phoneCode"
    }

    private fun changeCountry(country: String){
        val codes = country.split(" ")
        codes.firstOrNull()?.apply {
            countryCode = this
        }
        codes.lastOrNull()?.apply {
            phoneCode = this
        }

        setupMobileEditText()

        updateUI()
    }

    private fun activeContinueBtn(isActive: Boolean) {
        binding.tvContinue.apply {
            isClickable = if (isActive) {
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

    private fun actionCountryCode() {
        hideKeyboard()
        navigate(R.id.action_signupMobileFragment_to_countryCodeListFragment2)
    }

    private fun actionContinue() {
        hideKeyboard()
        authVM.user.value?.mobile = phoneCode + phoneExtracted
        authVM.user.value?.countryNameCode = countryCode
        authVM.user.value?.phoneNumCode = phoneCode

        BusyHelper.show(requireContext())
        authVM.sendOtp(true){ otpCode, message ->
            BusyHelper.hide()
            if (otpCode.isNullOrEmpty()) {
                ToastHelper.showMessage(message)
            }else if (PLANS_APP.currentFragment is SignupMobileFragment){
                authVM.codeOTP = otpCode
                (requireActivity() as BaseActivity<*>).showOverlay("Verification Code Sent\nSuccessfully", delayForHide = 1000){
                    val destination = "$phoneCode $phoneFormatted"
                    val action = SignupMobileFragmentDirections.actionSignupMobileFragmentToConfirmCodeFragment2(destination)
                    navigate(directions = action)
                }
            }
        }
    }

    override fun onTextChanged(
        maskFilled: Boolean,
        extractedValue: String,
        formattedValue: String
    ) {
        phoneFormatted = formattedValue
        phoneExtracted = extractedValue
        activeContinueBtn(maskFilled)
    }


}