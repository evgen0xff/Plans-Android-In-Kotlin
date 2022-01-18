package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentDeleteAccountBinding
import com.planscollective.plansapp.extension.isPasswordValid
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.fragment.utils.PlansDialogFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.DeleteAccountVM
import com.planscollective.plansapp.webServices.user.UserWebService

class DeleteAccountFragment : PlansBaseFragment<FragmentDeleteAccountBinding>(), PlansEditTextViewListener {

    private val viewModel: DeleteAccountVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            tvTitle.text = "${UserInfo.firstName ?: ""} ${UserInfo.lastName ?: ""}, we are sorry to see you go"

            etPassword.editText?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.listener = this@DeleteAccountFragment

            btnDeleteAccount.setOnSingleClickListener {
                deleteAccount()
            }
        }
        updateUI()
    }

    private fun deleteAccount(){
        hideKeyboard()
        val dialog = PlansDialogFragment(ConstantTexts.ASK_DELETE_ACCOUNT){
            BusyHelper.show(requireContext())
            UserWebService.deleteAccount(viewModel.password) {
                success, message ->
                BusyHelper.hide()
                if (success == true) {
                    PLANS_APP.gotoMainActivity(R.id.landingFragment)
                }else {
                    ToastHelper.showMessage(message)
                }
            }
        }
        dialog.show(requireActivity().supportFragmentManager, PlansDialogFragment.TAG)
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        binding.apply {
            etPassword.text = viewModel?.password
        }
        updateDeleteAccountBtn()
    }

    private fun updateDeleteAccountBtn() {
        binding.apply {
            val resId = if (viewModel?.password?.isPasswordValid() == true) {
                btnDeleteAccount.isClickable = true
                R.drawable.button_bkgnd_purple
            }else {
                btnDeleteAccount.isClickable = false
                R.drawable.button_bkgnd_gray
            }
            btnDeleteAccount.background = ContextCompat.getDrawable(PLANS_APP, resId)
        }
    }


    override fun didChangedText(text: String?, editText: PlansEditTextView?) {
        binding.apply {
            when(editText) {
                etPassword -> {
                    viewModel?.password = text
                    updateDeleteAccountBtn()
                }
            }
        }
    }

}