package com.planscollective.plansapp.fragment.signup

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.activity.BaseActivity
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentSignupPasswordBinding
import com.planscollective.plansapp.extension.isPasswordValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.manager.UserInfo

class SignupPasswordFragment : AuthBaseFragment<FragmentSignupPasswordBinding>(), PlansEditTextViewListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.apply {
            // Back Button
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            // Password
            etPassword.text = authVM.user.value?.password
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.textAction = "Show"
            etPassword.listener = this@SignupPasswordFragment

            // Terms and Privacy
            tvTerms.setOnSingleClickListener{
                actionTerms()
            }
            tvPrivacy.setOnSingleClickListener {
                actionPrivacy()
            }

            // Create Account
            tvCreateAccount.setOnSingleClickListener {
                createAccount()
            }
        }


        updateUI()
    }

    private fun updateUI() {
        binding.apply {
            // Create Account view
            tvCreateAccount.apply {
                isClickable = if (etPassword.text?.isPasswordValid() == true) {
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

    private fun actionTerms() {
        navigate(R.id.action_signupPasswordFragment_to_termsOfServicesFragment)
    }

    private fun actionPrivacy() {
        navigate(R.id.action_signupPasswordFragment_to_privacyPolicyFragment)
    }

    private fun createAccount() {
        hideKeyboard()

        BusyHelper.show(requireContext())
        authVM.createAccount { newUser, message ->
            BusyHelper.hide()
            if (newUser != null) {
                authVM.user.value?.id = newUser.id
                authVM.user.value?.accessToken = newUser.accessToken
                UserInfo.initForLogin(authVM.user.value)
                AnalyticsManager.logEvent(AnalyticsManager.EventType.SIGN_UP, UserInfo.userId)
                if (UserInfo.isClickedByAppLink) {
                    AnalyticsManager.logEvent(AnalyticsManager.EventType.INVITE_LINK, UserInfo.userId)
                }
                (requireActivity() as? BaseActivity<*>)?.let {
                    it.showOverlay("Account Created Successfully"){
                        PLANS_APP.pushNextStepForSignup(authVM.user.value, this, authVM.isSkipMode)
                    }
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }

    }

    //************************************ PlansEditTextView Listener ************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        authVM.user.value?.password = text
        updateUI()
    }

    override fun didClickedAction(editText: PlansEditTextView?) {
        changeInputType(editText)
    }

}