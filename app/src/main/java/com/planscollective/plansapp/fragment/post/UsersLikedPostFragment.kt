package com.planscollective.plansapp.fragment.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.FriendsListAdapter
import com.planscollective.plansapp.databinding.FragmentUsersLikedPostBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.viewModels.UsersLikedPostVM

class UsersLikedPostFragment : PlansBaseFragment<FragmentUsersLikedPostBinding>() {

    private val viewModel: UsersLikedPostVM by viewModels()
    private val args: UsersLikedPostFragmentArgs by navArgs()
    private val adapterUsers = FriendsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUsersLikedPostBinding.inflate(inflater, container, false)
        viewModel.eventId = args.eventId
        viewModel.postId = args.postId
        viewModel.actionEnterKey = actionEnterKey

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

            etSearch.addTextChangedListener {
                viewModel?.searchUsers()
            }

            // Event List Recycler View
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapterUsers

            // Refresh Layout
            refreshLayout.setOnRefreshListener {
                viewModel?.pageNumber = 1
                refreshAll(false)
            }
            refreshLayout.setOnLoadMoreListener {
                getNextPage()
            }
        }

        // Loading Data from server
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

        binding.viewModel = viewModel
        binding.apply {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()

            adapterUsers.updateAdapter(viewModel?.listSearched, this@UsersLikedPostFragment)
        }
    }
}