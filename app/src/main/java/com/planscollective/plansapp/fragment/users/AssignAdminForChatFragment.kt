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
import com.planscollective.plansapp.databinding.FragmentAssignAdminBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.AssignAdminForChatVM
import com.planscollective.plansapp.webServices.chat.ChatService

class AssignAdminForChatFragment : PlansBaseFragment<FragmentAssignAdminBinding>() {

    private val viewModel: AssignAdminForChatVM by viewModels()
    private val args: AssignAdminForChatFragmentArgs by navArgs()
    private val adapterList = FriendsSelectionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAssignAdminBinding.inflate(inflater, container, false)

        viewModel.chatId = args.chatId

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
            btnBack.setOnSingleClickListener (this@AssignAdminForChatFragment)

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
            btnDone.setOnSingleClickListener(this@AssignAdminForChatFragment)
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

        adapterList.updateAdapter(viewModel.listMembers, viewModel.listSelected, this)

        // Done UI
        binding.layoutDone.visibility = if (viewModel.listSelected.isNotEmpty()) View.VISIBLE else View.GONE

        // Empty UI
        updateEmptyUI()
    }

    private fun updateEmptyUI() {
        binding.apply {
            layoutEmpty.visibility = if (viewModel?.listMembers?.isNotEmpty() == true) View.GONE  else {
                if (viewModel?.keywordSearch?.isNotBlank() == true) {
                    tvMessage.text = "Sorry! No Members Found."
                    imvMark.setImageResource(R.drawable.ic_search_grey)
                }else {
                    tvMessage.text = "There isn't any members in the chat."
                    imvMark.setImageResource(R.drawable.ic_friends_grey)
                }
                View.VISIBLE
            }
        }
    }

    private fun actionDone() {
        val adminId = viewModel.listSelected.firstOrNull()?.id ?: return

        BusyHelper.show(timeDelay = 0)
        ChatService.assignAdminForGroupChat(viewModel.chatId, adminId){ chat, message ->
            if (chat != null) {
                ChatService.removeUserInChat(UserInfo.userId, viewModel.chatId){ chat, message ->
                    BusyHelper.hide()
                    if (chat != null) {
                        gotoBack(R.id.chatsFragment)
                    }else {
                        ToastHelper.showMessage(message)
                    }
                }
            }else {
                BusyHelper.hide()
                ToastHelper.showMessage(message)
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