package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentSettingsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment

class SettingsFragment : PlansBaseFragment<FragmentSettingsBinding>() {
    override var screenName: String? = "Settings_Screen"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }
            layoutEditProfile.setOnSingleClickListener {
                navigate(R.id.action_global_editProfileFragment)
            }
            layoutChangePassword.setOnSingleClickListener {
                navigate(R.id.action_global_changePasswordFragment)
            }
            layoutPostsYouLiked.setOnSingleClickListener {
                navigate(R.id.action_global_postsLikedFragment)
            }
            layoutPushNotifications.setOnSingleClickListener {
                navigate(R.id.action_global_settingsPushNotificationsFragment)
            }
            layoutPrivacyOptions.setOnSingleClickListener {
                navigate(R.id.action_global_privacyOptionsFragment)
            }
            layoutSendFeedback.setOnSingleClickListener {
                val action = NavDashboardDirections.actionGlobalSendFeedbackFragment()
                action.type = SendFeedbackFragment.Type.SEND_FEEDBACK.name
                navigate(directions = action)
            }
            layoutHelpLegal.setOnSingleClickListener {
                navigate(R.id.action_global_helpLegalFragment)
            }
            layoutLogout.setOnSingleClickListener {
                logOut()
            }
        }
    }

}