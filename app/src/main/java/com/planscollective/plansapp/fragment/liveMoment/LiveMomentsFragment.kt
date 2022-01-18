package com.planscollective.plansapp.fragment.liveMoment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.planscollective.plansapp.adapters.LiveMomentsAdapter
import com.planscollective.plansapp.databinding.FragmentLiveMomentsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.viewModels.LiveMomentsVM

class LiveMomentsFragment : PlansBaseFragment<FragmentLiveMomentsBinding>() {

    private val viewModel: LiveMomentsVM by viewModels()
    private var adapterLiveMoments = LiveMomentsAdapter(typeAdapter = LiveMomentsAdapter.AdapterType.FULL_LIVE_MOMENTS)
    private val args: LiveMomentsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLiveMomentsBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            updateUI()
        })
        viewModel.actionEnterKey = actionEnterKey
        viewModel.eventId = args.eventId
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener (this)

        // Search
        binding.etSearch.addTextChangedListener {
            viewModel.updateData(viewModel.eventModel?.liveMoments)
        }

        // Watch All
        binding.btnWatchAll.setOnSingleClickListener(this)

        // Event List
        val layoutMangerList = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = adapterLiveMoments

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
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

    override fun getNextPage(isShownLoading: Boolean) {
        super.getNextPage(isShownLoading)
        viewModel.getNextPage(requireContext(), isShownLoading)
    }


    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)

        binding.refreshLayout.finishRefresh()
        binding.refreshLayout.finishLoadMore()
        adapterLiveMoments.updateAdapter(viewModel.eventModel, viewModel.listUserLiveMoments, this)
    }

    //******************************* View.OnClick listener *********************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnWatchAll -> {
                gotoWatchLiveMoments(viewModel.eventModel)
            }
        }
    }

}