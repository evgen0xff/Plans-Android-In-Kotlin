package com.planscollective.plansapp.fragment.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.ChatsAdapter
import com.planscollective.plansapp.databinding.FragmentChatsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.viewModels.ChatsVM

class ChatsFragment : PlansBaseFragment<FragmentChatsBinding>() {

    private val viewModel: ChatsVM by viewModels()
    private val adapterList = ChatsAdapter()
    override var screenName: String? = "Chats_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatsBinding.inflate(inflater, container, false)

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
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

            btnBack.setOnSingleClickListener {
                gotoBack()
            }

            btnNewChat.setOnSingleClickListener {
                gotoFriendsSelections()
            }

            // Search
            etSearch.addTextChangedListener {
                viewModel?.searchChatList()
            }

            // RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapterList

            // Pull-to-Refresh
            refreshLayout.setOnRefreshListener {
                viewModel?.pageNumber = 1
                refreshAll(false)
            }
            refreshLayout.setOnLoadMoreListener {
                getNextPage()
            }

            // Empty UI
            layoutEmpty.visibility = View.GONE
        }

        // Refresh All UI
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

        adapterList.updateAdapter(viewModel.listSearched, this)

        updateEmptyUI()
    }

    private fun updateEmptyUI() {
        binding.apply {
            layoutEmpty.visibility = if (viewModel?.listChats.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    //********************************* PlansActionListener **************************************//
    override fun onClickChatListItem(chatModel: ChatModel?){
        gotoChatting(chatModel = chatModel)
    }

    override fun onLongClickChatListItem(chatModel: ChatModel?){
        MenuOptionHelper.showPlansMenu(chatModel, MenuOptionHelper.MenuType.CHAT, this, this)
    }

}