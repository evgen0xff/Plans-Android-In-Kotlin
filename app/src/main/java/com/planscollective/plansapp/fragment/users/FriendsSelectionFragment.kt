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
import com.planscollective.plansapp.adapters.FriendsSelectionAdapter
import com.planscollective.plansapp.databinding.FragmentFriendsSelectionBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.FriendsSelectionVM

class FriendsSelectionFragment : PlansBaseFragment<FragmentFriendsSelectionBinding>() {

    enum class SelectType {
        CHAT_START,
        CHAT_ADD_PEOPLE
    }

    private val viewModel: FriendsSelectionVM by viewModels()
    private val args: FriendsSelectionFragmentArgs by navArgs()
    private val adapterList = FriendsSelectionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFriendsSelectionBinding.inflate(inflater, container, false)
        viewModel.initializeData(args.type, args.usersSelected, args.chatId)

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
        binding.apply {
            // Back button
            btnBack.setOnSingleClickListener (this@FriendsSelectionFragment)

            // Search
            etSearch.addTextChangedListener {
                refreshAll(false)
            }

            // Friends List
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapterList

            // Refresh Layout
            refreshLayout.setOnRefreshListener {
                viewModel?.pageNumber = 1
                refreshAll(false)
            }
            refreshLayout.setOnLoadMoreListener {
                getNextPage()
            }

            // Empty UI
            layoutEmpty.visibility = View.GONE

            // Done UI
            when (viewModel?.type) {
                SelectType.CHAT_START -> {
                    tvDone.text = "START CHAT"
                }
                SelectType.CHAT_ADD_PEOPLE -> {
                    tvDone.text = "ADD"
                }
            }
            btnDone.setOnSingleClickListener(this@FriendsSelectionFragment)
            layoutDone.visibility = View.GONE
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
        binding.viewModel = viewModel

        adapterList.updateAdapter(viewModel.listAvailables, viewModel.listSelected, this)

        // Done UI
        binding.layoutDone.visibility = if (viewModel.listSelected.isNotEmpty()) View.VISIBLE else View.GONE

        // Empty UI
        updateEmptyUI()
    }

    private fun updateEmptyUI() {
        binding.apply {
            layoutEmpty.visibility = if (viewModel?.listFriends?.isNotEmpty() == true) {
                if (viewModel?.listAvailables?.isNotEmpty() == true) View.GONE else {
                    tvMessage.text = "Sorry! No Friends Found."
                    imvMark.setImageResource(R.drawable.ic_search_grey)
                    View.VISIBLE
                }
            } else {
                if (viewModel?.keywordSearch?.isNotBlank() == true) {
                    tvMessage.text = "Sorry! No Friends Found."
                    imvMark.setImageResource(R.drawable.ic_search_grey)
                }else {
                    tvMessage.text = "You don't have any friends yet."
                    imvMark.setImageResource(R.drawable.ic_friends_grey)
                }
                View.VISIBLE
            }
        }
    }

    private fun actionDone() {
        val list = ArrayList<UserModel>().apply {
            addAll(viewModel.listSelectedAlready)
            addAll(viewModel.listSelected)
        }

        when(viewModel.type) {
            SelectType.CHAT_START -> {
                createChatGroup(list)
            }
            SelectType.CHAT_ADD_PEOPLE -> {
                updateChatGroup(viewModel.chatId, list)
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
                btnDone -> {
                    actionDone()
                }
            }
        }
    }

    //******************************* PlansActions Listener *********************************//
    override fun onSelectedUser(user: UserModel?) {
        viewModel.selectUser(user)
    }
}