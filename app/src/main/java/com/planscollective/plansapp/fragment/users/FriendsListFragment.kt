package com.planscollective.plansapp.fragment.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.FriendsListAdapter
import com.planscollective.plansapp.databinding.FragmentFriendsListBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.FriendsListVM

class FriendsListFragment : PlansBaseFragment<FragmentFriendsListBinding>() {

    private val viewModel: FriendsListVM by viewModels()
    private val args: FriendsListFragmentArgs by navArgs()
    private val adapterList = FriendsListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFriendsListBinding.inflate(inflater, container, false)
        viewModel.userId = args.userId
        viewModel.user = args.userModel

        viewModel.didLoadData.observe(viewLifecycleOwner, {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            updateUI()
        })

        viewModel.actionEnterKey = actionEnterKey
        binding.viewModel = viewModel

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

        // Add Friends
        binding.btnAddFriends.visibility = if (viewModel.userId == UserInfo.userId) View.VISIBLE else View.GONE
        binding.btnAddFriends.setOnSingleClickListener(this)

        // Friends List
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapterList

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        // Empty UI
        binding.layoutEmpty.visibility = View.GONE

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
        binding.viewModel = viewModel

        adapterList.updateAdapter(viewModel.listUsers, this)

        // Empty UI
        updateEmptyUI()
    }

    private fun updateEmptyUI() {
        binding.apply {
            layoutEmpty.visibility = if (viewModel?.listUsers?.isNotEmpty() == true) View.GONE else {
                if (viewModel?.keywordSearch?.isNotBlank() == true) {
                    tvMessage.text = "Sorry! No friends found."
                    imvMark.setImageResource(R.drawable.ic_search_grey)
                    imvArrowUp.visibility = View.GONE
                    tvSearchAddFriends.visibility = View.GONE
                }else if (viewModel?.userId == UserInfo.userId){
                    tvMessage.text = "You don't have any friends yet."
                    imvMark.setImageResource(R.drawable.ic_friends_grey)
                    imvArrowUp.visibility = View.VISIBLE
                    tvSearchAddFriends.visibility = View.VISIBLE
                }else {
                    tvMessage.text = "No friends yet."
                    imvMark.setImageResource(R.drawable.ic_search_grey)
                    imvArrowUp.visibility = View.GONE
                    tvSearchAddFriends.visibility = View.GONE
                }
                View.VISIBLE
            }
        }
    }

    //******************************* OnSingleClickListener *********************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                btnAddFriends -> {
                    gotoAddFriends()
                }
            }
        }
    }
}