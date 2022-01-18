package com.planscollective.plansapp.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.SettingsPushNotificationsAdapter
import com.planscollective.plansapp.databinding.FragmentSettingsPushNotificationsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.dataModels.SettingOptionModel
import com.planscollective.plansapp.models.viewModels.SettingsPushNotificationsVM

class SettingsPushNotificationsFragment : PlansBaseFragment<FragmentSettingsPushNotificationsBinding>() {

    private val viewModel: SettingsPushNotificationsVM by viewModels()
    private val adapterList = SettingsPushNotificationsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsPushNotificationsBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapterList
        }
        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }


    private fun updateUI() {
        binding.viewModel = viewModel
        adapterList.updateAdapter(viewModel.settings?.pushNotifications, this, viewModel.isOnline)
    }

    //******************************* Plans Action Listener ***********************************//
    override fun onChangedSettingOption(option: SettingOptionModel?, isCheck: Boolean){
        viewModel.updateSettings(option, isCheck)
    }
}