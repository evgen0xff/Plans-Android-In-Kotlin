package com.planscollective.plansapp.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentCreateEventProgress3Binding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.models.viewModels.CreateEventVM
import com.planscollective.plansapp.webServices.event.EventWebService

class CreateEventProgress3Fragment : PlansBaseFragment<FragmentCreateEventProgress3Binding>() {

    private val viewModel : CreateEventVM by navGraphViewModels(R.id.createEventFragment)
    override var screenName: String? = "CreateEvent_Screen_4"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateEventProgress3Binding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        if (viewModel.eventModel.isPublic == null) {
            viewModel.eventModel.isPublic = false
        }

        if (viewModel.eventModel.isGroupChatOn == null) {
            viewModel.eventModel.isGroupChatOn = true
        }

        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Options
        binding.also {
            it.layoutPublic.setOnSingleClickListener(this)
            it.layoutPrivate.setOnSingleClickListener(this)
            it.layoutGroupChatOn.setOnSingleClickListener(this)
            it.layoutGroupChatOff.setOnSingleClickListener(this)
            it.btnContinue.setOnSingleClickListener(this)
        }

        updateUI()
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        // Update Options UI
        updateOptionsUI()
    }

    private fun updateOptionsUI() {
        binding.apply {
            (viewModel?.eventModel?.isPublic ?: false).also {
                btnPublic.isSelected = it
                btnPrivate.isSelected = !it
            }

            (viewModel?.eventModel?.isGroupChatOn ?: false).also {
                btnGroupChatOn.isSelected = it
                btnGroupChatOff.isSelected = !it
            }
        }
    }

    fun createEvent() {
        val event = viewModel.getEventWith()
        ToastHelper.showLoadingAlerts(ToastHelper.LoadingToastType.CREATING_EVENT)
        EventWebService.createEvent(event){
                success, message ->
            ToastHelper.hideLoadingAlerts(ToastHelper.LoadingToastType.CREATING_EVENT)
            if (success) {
                ToastHelper.showMessage("Your event was created.")
                PLANS_APP.refreshCurrentScreen()
                AnalyticsManager.logEvent(AnalyticsManager.EventType.CREATE_EVENT)
            }else {
                ToastHelper.showMessage(message)
            }
        }
        gotoDashboard()
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                layoutPublic -> {
                    viewModel?.eventModel?.isPublic = true
                    updateOptionsUI()
                }
                layoutPrivate -> {
                    viewModel?.eventModel?.isPublic = false
                    updateOptionsUI()
                }
                layoutGroupChatOn -> {
                    viewModel?.eventModel?.isGroupChatOn = true
                    updateOptionsUI()
                }
                layoutGroupChatOff -> {
                    viewModel?.eventModel?.isGroupChatOn = false
                    updateOptionsUI()
                }
                btnContinue -> {
                    createEvent()
                }
            }
        }
    }
}