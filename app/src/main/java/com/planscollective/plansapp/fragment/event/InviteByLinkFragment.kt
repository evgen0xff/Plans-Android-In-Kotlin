package com.planscollective.plansapp.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.planscollective.plansapp.databinding.FragmentInviteByLinkBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.viewModels.InviteByLinkVM

class InviteByLinkFragment : PlansBaseFragment<FragmentInviteByLinkBinding>() {

    private val viewModel: InviteByLinkVM by viewModels()
    private val args: InviteByLinkFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInviteByLinkBinding.inflate(inflater, container, false)
        viewModel.eventModel = args.event
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            tvShareLink.text = viewModel?.eventModel?.eventLink?.invitation
            btnShareLink.setOnSingleClickListener {
                shareEvent(viewModel?.eventModel, true)
            }
            btnCopy.setOnSingleClickListener {
                copyTextToClipBoard("Plans Event Invitation", viewModel?.eventModel?.eventLink?.invitation) { success, _ ->
                    if (success) {
                        ToastHelper.showMessage("Link copied")
                    }
                }
            }
        }
    }
}