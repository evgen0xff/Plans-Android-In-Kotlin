package com.planscollective.plansapp.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.databinding.FragmentResetPasswordBinding
import com.planscollective.plansapp.extension.getMaskForMobile
import com.planscollective.plansapp.extension.isEmailValid
import com.planscollective.plansapp.extension.isMobileValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.redmadrobot.inputmask.MaskedTextChangedListener

class ResetPasswordFragment : AuthBaseFragment<FragmentResetPasswordBinding>(), MaskedTextChangedListener.ValueListener {

    enum class VerificationMode {
        PHONE,
        EMAIL
    }

    var mode = VerificationMode.PHONE
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
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        curBackStackEntry?.savedStateHandle?.getLiveData<String>(Keys.SELECTED_COUNTRY)?.observe(this){
            changeCountry(it)
        }

        // Nav Bar
        binding.btnBack.setOnSingleClickListener {
            gotoBack()
        }

        // Phone / Email Button
        binding.tvPhone.setOnSingleClickListener {
            mode = VerificationMode.PHONE
            updateUI()
        }
        binding.tvEmail.setOnSingleClickListener {
            mode = VerificationMode.EMAIL
            updateUI()
        }

        // Phone edit text
        setupMobileEditText()


        // Email text
        binding.editTextEmail.addTextChangedListener{
            updateUI()
        }
        binding.editTextEmail.setOnFocusChangeListener { v, hasFocus ->
            updateUI()
        }

        // Country code button
        binding.tvCountryCode.setOnSingleClickListener {
            actionCountryCode()
        }

        // Send code button
        binding.tvSendCode.setOnSingleClickListener {
            actionSendVerifyCode()
        }

        updateUI()
    }

    private fun setupMobileEditText() {
        phoneExtracted = ""
        phoneFormatted = ""
        binding.editTextMobile.setText("")

        binding.editTextMobile.removeTextChangedListener(listener)
        binding.editTextMobile.onFocusChangeListener = null

        val primaryFormat = phoneCode.getMaskForMobile("0", countryCode, requireContext(), true)
        listener = MaskedTextChangedListener.installOn(binding.editTextMobile, primaryFormat, this)

        binding.editTextMobile.hint = listener?.placeholder()
    }

    private fun updateUI() {

        // Verification Mode Buttons
        binding.apply {
            if (mode == VerificationMode.PHONE) {
                layoutMobile.visibility = View.VISIBLE
                layoutEmail.visibility = View.INVISIBLE
                tvPhone.setTextColor(resources.getColor(R.color.white))
                tvEmail.setTextColor(resources.getColor(R.color.white_opacity50))
            }else {
                layoutMobile.visibility = View.INVISIBLE
                layoutEmail.visibility = View.VISIBLE
                tvPhone.setTextColor(resources.getColor(R.color.white_opacity50))
                tvEmail.setTextColor(resources.getColor(R.color.white))

                tvEmailHintLabel.visibility = if (editTextEmail.hasFocus() || editTextEmail.text.isNotEmpty()) {
                    editTextEmail.hint = ""
                    View.VISIBLE
                } else {
                    editTextEmail.hint = tvEmailHintLabel.text
                    View.INVISIBLE
                }

            }
            // Country Code Text
            tvCountryCode.text = "$countryCode $phoneCode"
        }

        // Send button Active
        activeSendCodeBtn(checkValidation())
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

    private fun checkValidation() : Boolean {
        var result = false
        binding.apply {
            if (mode == VerificationMode.PHONE) {
                val lengthMin = phoneCode.getMaskForMobile("0", countryCode, requireContext()).replace("-", "").length
                result = phoneExtracted.isMobileValid(lengthMin)
                if (result) {
                    destination = "$phoneCode $phoneFormatted"
                }
            }else {
                val email = editTextEmail.text.toString().trim()
                result = email.isEmailValid()
                imageViewCheck.visibility =  if (result) View.VISIBLE else View.INVISIBLE
                if (result) {
                    destination = email
                }
            }
        }
        return result
    }

    private fun activeSendCodeBtn(isActive: Boolean) {
        binding.tvSendCode.apply {
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
        navigate(R.id.action_resetPasswordFragment_to_countryCodeListFragment)
    }

    private fun actionSendVerifyCode() {
        hideKeyboard()
        when(mode) {
            VerificationMode.EMAIL -> {
                authVM.user.value?.email = binding.editTextEmail.text.toString().trim()
                authVM.user.value?.mobile = null
            }
            VerificationMode.PHONE -> {
                authVM.user.value?.mobile = phoneCode + phoneExtracted
                authVM.user.value?.phoneNumCode = phoneCode
                authVM.user.value?.countryNameCode = countryCode
                authVM.user.value?.email = null
            }
        }
        
        BusyHelper.show(requireContext())
        authVM.sendOtp(false){ otpCode, message ->
            BusyHelper.hide()
            if (otpCode.isNullOrEmpty()) {
                ToastHelper.showMessage(message)
            }else {
                authVM.codeOTP = otpCode
                (requireActivity() as BaseActivity<*>).showOverlay("Verification Code Sent\nSuccessfully", delayForHide = 1000){
                    val action = ResetPasswordFragmentDirections.actionResetPasswordFragmentToConfirmCodeFragment(destination)
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
        phoneExtracted = extractedValue
        phoneFormatted = formattedValue
        updateUI()
    }

}