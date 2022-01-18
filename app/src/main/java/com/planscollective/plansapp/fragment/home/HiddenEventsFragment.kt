package com.planscollective.plansapp.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.EventListAdapter
import com.planscollective.plansapp.databinding.FragmentHiddenEventsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.viewModels.HiddenEventsVM

class HiddenEventsFragment : PlansBaseFragment<FragmentHiddenEventsBinding>() {

    private var adapterEventList: EventListAdapter? = null
    private val viewModel : HiddenEventsVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHiddenEventsBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateData(it)
        })
        return binding.root
    }


    override fun setupUI() {
        super.setupUI()

        // Event List Recycler View
        adapterEventList = EventListAdapter(requireContext(), this)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapterEventList

        // Calendar button
        binding.btnBack.setOnSingleClickListener(this)

        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    private fun updateData(isRefresh: Boolean = true) {
        adapterEventList?.updateAdapter(viewModel.listEvents)
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
        }
    }

}