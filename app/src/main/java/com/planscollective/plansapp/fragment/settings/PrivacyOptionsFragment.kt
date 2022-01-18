package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentPrivacyOptionsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.webServices.user.UserWebService

class PrivacyOptionsFragment : PlansBaseFragment<FragmentPrivacyOptionsBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivacyOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }
            switchPrivateAccount.isChecked = UserInfo.isPrivateAccount
            switchPrivateAccount.setOnCheckedChangeListener { buttonView, isChecked ->
                updatePrivateAccount(isChecked)
            }
            btnBlockedUsers.setOnSingleClickListener {
                navigate(R.id.action_global_usersBlockedFragment)
            }
        }
    }

    private fun updatePrivateAccount(isPrivate: Boolean = true) {
        val user = UserModel()
        user.isPrivateAccount = isPrivate
        BusyHelper.show(requireContext())
        UserWebService.updateUser(user){ newUser, message ->
            BusyHelper.hide()
            if (newUser != null) {
                UserInfo.isPrivateAccount = newUser.isPrivateAccount ?: true
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }
}