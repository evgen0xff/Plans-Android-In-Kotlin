package com.planscollective.plansapp.fragment.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.PostDetailsAdapter
import com.planscollective.plansapp.databinding.FragmentPostDetailsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.PostDetailsVM

class PostDetailsFragment : PlansBaseFragment<FragmentPostDetailsBinding>(){

    private val viewModel : PostDetailsVM by viewModels()
    private val args: PostDetailsFragmentArgs by navArgs()
    private val adapterPostDetails = PostDetailsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        viewModel.eventId = args.eventId
        viewModel.postId = args.postId

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        viewModel.messagePosting.observe(viewLifecycleOwner, Observer {
            updatePostingUI()
        })
        binding.viewModel = viewModel

        return binding.root
    }


    override fun setupUI() {
        super.setupUI()

        // Event List Recycler View
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapterPostDetails
        adapterPostDetails.listener = this

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Menu Button
        binding.btnMenu.setOnSingleClickListener(this)

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        // Posting UI
        binding.btnSend.setOnSingleClickListener(this)
        binding.imgvUserImage.setUserImage(UserInfo.profileUrl)
        updatePostingUI()

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

        binding.let {
            it.viewModel = viewModel
            it.refreshLayout.finishRefresh()
            it.refreshLayout.finishLoadMore()

            adapterPostDetails.updateAdapter(viewModel.postModel, viewModel.eventModel, viewModel.listComments, this)
        }
        updatePostingUI()
    }

    private fun updatePostingUI() {
        binding.apply {
            if (!viewModel?.messagePosting?.value.isNullOrEmpty() ) {
                btnSend.setImageResource(R.drawable.ic_send_purple)
                btnSend.isClickable = true
            }else {
                btnSend.setImageResource(R.drawable.ic_send_grey)
                btnSend.isClickable = false
            }
        }
    }

    private fun sendPost() {
        hideKeyboard()
        viewModel.sendPost(requireContext())
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnMenu -> {
                val data = hashMapOf("content" to viewModel.postModel, "event" to viewModel.eventModel)
                MenuOptionHelper.showPlansMenu(data, MenuOptionHelper.MenuType.POST_COMMENT, this, this)
            }
            binding.btnSend -> {
                sendPost()
            }
        }
    }

}