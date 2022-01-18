package com.planscollective.plansapp.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.adapters.SearchEventAdapter
import com.planscollective.plansapp.adapters.TabItemAdapter
import com.planscollective.plansapp.databinding.FragmentSearchEventBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.models.viewModels.SearchEventVM

class SearchEventFragment : PlansBaseFragment<FragmentSearchEventBinding>(), OnItemTouchListener {

    private val viewModel: SearchEventVM by viewModels()
    private var tabItemAdapter = TabItemAdapter(arrayListOf("LIVE", "UPCOMING", "PUBLIC", "ENDED"))
    private var searchEventAdapter = SearchEventAdapter()
    override var screenName: String? = "SearchEvent_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchEventBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        viewModel.selectedItem.observe(viewLifecycleOwner, {
            refreshAll()
        })
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            updateEvents()
        })
        viewModel.actionEnterKey = actionEnterKey

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener (this)

        // Search
        binding.etSearch.addTextChangedListener {
            refreshAll(false)
        }

        // Top TabBar
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recvTabBar.layoutManager = layoutManager
        binding.recvTabBar.adapter = tabItemAdapter
        tabItemAdapter.updateAdapter(listener = this, selectedItem = viewModel.selectedItem.value)

        // Event List
        val layoutMangerList = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = searchEventAdapter
        searchEventAdapter.updateAdapter(listener = this)

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        if (viewModel.selectedItem.value == null) {
            viewModel.selectedItem.value = 0
        }
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


    private fun updateEvents() {
        tabItemAdapter.updateAdapter(selectedItem = viewModel.selectedItem.value)
        searchEventAdapter.updateAdapter(viewModel.listEvents)
        binding.layoutEmpty.visibility = if (viewModel.listEvents.size > 0) View.INVISIBLE else View.VISIBLE
    }

    //******************************* Top TabBar click listener *********************************//
    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        if (viewModel.selectedItem.value != position) {
            viewModel.pageNumber = 1
            viewModel.selectedItem.value = position
        }
    }


    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
        }
    }

}