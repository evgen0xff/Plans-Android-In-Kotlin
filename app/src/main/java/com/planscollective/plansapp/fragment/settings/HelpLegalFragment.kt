package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentHelpLegalBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment

class HelpLegalFragment : PlansBaseFragment<FragmentHelpLegalBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelpLegalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }
            layoutReportProblem.setOnSingleClickListener {
                val action = NavDashboardDirections.actionGlobalSendFeedbackFragment()
                action.type = SendFeedbackFragment.Type.REPORT_PROBLEM.name
                navigate(directions = action)
            }
            layoutTermsOfService.setOnSingleClickListener {
                navigate(R.id.action_global_termsOfServicesFragment2)
            }
            layoutPrivacyPolicy.setOnSingleClickListener {
                navigate(R.id.action_global_privacyPolicyFragment2)
            }
            layoutDeleteAccount.setOnSingleClickListener {
                navigate(R.id.action_global_deleteAccountFragment)
            }
        }
    }

}