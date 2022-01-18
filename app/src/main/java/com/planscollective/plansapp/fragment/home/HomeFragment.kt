package com.planscollective.plansapp.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.activity.DashboardActivity
import com.planscollective.plansapp.adapters.EventListAdapter
import com.planscollective.plansapp.databinding.FragmentHomeBinding
import com.planscollective.plansapp.extension.setLayoutMargin
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.interfaces.OnSwipeTouchListener
import com.planscollective.plansapp.manager.PlansLocationManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.HomeVM

class HomeFragment : PlansBaseFragment<FragmentHomeBinding>() {

    private val viewModel : HomeVM by viewModels()
    private var adapterEventList: EventListAdapter? = null

    override var screenName: String? = "HomeFeed_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
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
        binding.btnCalendar.setOnSingleClickListener(this)

        // Search Button
        binding.btnSearch.setOnSingleClickListener(this)

        // Chats Button
        binding.btnChat.setOnSingleClickListener(this)

        // Guide Layouts
        val onSwipeListener = object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeDown() {
                refreshAll()
            }
        }
        binding.layoutGuideWelcome.setOnTouchListener(onSwipeListener)
        binding.layoutGuideEmpty.setOnTouchListener(onSwipeListener)
        binding.btnHiddenEvents.setOnSingleClickListener(this)
        binding.btnGuideWelcomeFindFriends.setOnSingleClickListener(this)

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


    private fun updateData(isRefresh: Boolean = true) {
        updateGuide()
        adapterEventList?.updateAdapter(viewModel.listEvents, viewModel.listEventsHide)
        updateChatsBadge()
        showPopUpForEventLived()
    }

    fun updateChatsBadge() {
        _binding?.apply {
            viewChatBadge.visibility = if (UserInfo.countUnviewedChatMsgs > 0) {
                txtChatNumber.text = UserInfo.countUnviewedChatMsgs.toString()
                btnChat.setLayoutMargin(right = 11.toPx())
                View.VISIBLE
            }else {
                btnChat.setLayoutMargin(right = 3.toPx())
                View.GONE
            }
        }
    }

    private fun updateGuide() {
        if (viewModel.listEvents.size > 0) {
            binding.layoutGuideEmpty.visibility = View.GONE
            binding.layoutGuideWelcome.visibility = View.GONE
        }else {
            if (UserInfo.isSeenGuideWelcome) {
                binding.layoutGuideEmpty.visibility = View.VISIBLE
                binding.layoutGuideWelcome.visibility = View.GONE
            }else {
                binding.layoutGuideEmpty.visibility = View.GONE
                binding.layoutGuideWelcome.visibility = View.VISIBLE
            }
        }
    }

    private fun showPopUpForEventLived() {
        viewModel.listEvents.firstOrNull { it.isLive == 1 && it.isLiveUser(UserInfo.userId) }?.also {
            if (!UserInfo.isSeenYouLiveAt && PLANS_APP.didUpdatedUserLocation) {
                UserInfo.isSeenYouLiveAt = true
                (PLANS_APP.currentActivity as? DashboardActivity)?.showPopUpForEventLived(it)
            }
        }
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnCalendar -> {
                gotoCalendarEvent()
            }
            binding.btnSearch -> {
                gotoSearchEvents()
            }
            binding.btnChat -> {
                gotoChats()
            }
            binding.btnHiddenEvents -> {
                gotoHiddenEvents()
            }
            binding.btnGuideWelcomeFindFriends -> {
                gotoAddFriends(userId = UserInfo.userId)
            }
        }
    }

}