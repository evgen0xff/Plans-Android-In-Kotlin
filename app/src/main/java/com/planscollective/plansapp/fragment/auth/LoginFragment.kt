package com.planscollective.plansapp.fragment.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentLoginBinding
import com.planscollective.plansapp.extension.isEmailValid
import com.planscollective.plansapp.extension.isPasswordValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.manager.UserInfo

class LoginFragment : AuthBaseFragment<FragmentLoginBinding>(), PlansEditTextViewListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
            editTextEmail.text = authVM.user.value?.email
            editTextEmail.listener = this@LoginFragment

            // Password EditView
            editTextPassword.text = authVM.user.value?.password
            editTextPassword.listener = this@LoginFragment
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editTextPassword.textAction = "Show"


            // Forgot Password button
            tvForgotPassword.setOnSingleClickListener {
                actionForgotPassword()
            }

            // Login Button
            tvLogin.setOnSingleClickListener{
                actionLogin()
            }
        }

        updateUI()
    }

    private fun updateUI() {
        binding.apply {
            // Email Edit View
            val isValidEmail =  editTextEmail.text.toString().trim().isEmailValid()
            editTextEmail.enableValid = isValidEmail

            // Password Edit View
            val isValidPassword = editTextPassword.text.toString().isPasswordValid()

            // Login Button
            tvLogin.apply {
                isClickable = if (isValidEmail && isValidPassword) {
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

    private fun actionForgotPassword() {
        navigate(R.id.action_loginFragment_to_resetPasswordFragment)
    }

    private fun actionLogin() {
        hideKeyboard()
        authVM.user.value?.fcmId = UserInfo.deviceToken
        authVM.user.value?.loginType = "true"
//        authVM.user.value?.loginType = "admin"

        BusyHelper.show(requireContext())
        authVM.login { user, message ->
            BusyHelper.hide()
            if (user != null) {
                authVM.setUser(user)
                UserInfo.initForLogin(user)
                AnalyticsManager.logEvent(AnalyticsManager.EventType.LOGIN, UserInfo.userId)
                PLANS_APP.gotoDashboardActivity(user, ConstantTexts.SIGN_IN_SUCCESS)
            }else {
                ToastHelper.showMessage(message)
            }
        }


    }

    //****************************** PlansEditTextView Listeners *******************************//

    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        binding.apply {
            when(editText) {
                editTextEmail -> {
                    authVM.user.value?.email = text?.trim()
                }
                editTextPassword -> {
                    authVM.user.value?.password = text?.trim()
                }
            }
        }
        updateUI()
    }

    override fun didClickedAction(editText: PlansEditTextView?){
        binding.apply {
            when(editText) {
                editTextPassword -> {
                    if (editTextPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)){
                        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        editTextPassword.textAction = "Show"
                    } else {
                        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        editTextPassword.textAction = "Hide"
                    }
                }
            }
        }
    }



}