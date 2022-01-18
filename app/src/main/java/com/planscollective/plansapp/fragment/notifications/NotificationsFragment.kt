package com.planscollective.plansapp.fragment.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.NotificationsAdapter
import com.planscollective.plansapp.databinding.FragmentNotificationsBinding
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.MenuModel
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.viewModels.NotificationsVM

class NotificationsFragment : PlansBaseFragment<FragmentNotificationsBinding>() {

    private val viewModel: NotificationsVM by viewModels()
    private val adapterList = NotificationsAdapter()
    override var screenName: String? = "Notifications_Screen"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.refreshLayout.finishRefresh()
                binding.refreshLayout.finishLoadMore()
                updateUI()
            }
        })


        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        binding.apply {
            // RecyclerView
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            layoutManager.stackFromEnd = false
            recyclerView.layoutManager = layoutManager
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.didLoadData.value = false
        UserInfo.countUnviewedNotify = 0
        PLANS_APP.updateBadges()
    }


    private fun updateUI() {
        binding.viewModel = viewModel

        adapterList.updateAdapter(viewModel.notifications,this)

        updateEmptyUI()
    }

    private fun updateEmptyUI() {
        binding.apply {
            layoutEmpty.visibility = if (((viewModel?.notifications?.listActivities?.size ?: 0) +
                        (viewModel?.notifications?.eventInvitationCount ?: 0) +
                        (viewModel?.notifications?.friendRequestCount ?: 0)) > 0) View.GONE else View.VISIBLE
        }
    }

    fun gotoNotification(notify: NotificationActivityModel?) {
        onClickNotification(notify)
    }

    //********************************* PlansActionListener **************************************//
    override fun onClickedEventInvitations(){
        gotoEventInvitations()
    }

    override fun onClickedFriendRequests(){
        gotoFriendRequests()
    }

    override fun onLongClickNotification(notify: NotificationActivityModel?){
        val list = arrayListOf(MenuModel(R.drawable.ic_trash_black_menu, "Delete"))
        MenuOptionHelper.showBottomMenu(list, this, this, notify)
    }

    override fun onClickNotification(notify: NotificationActivityModel?){
        when(notify?.notificationType) {
            "Update Event", "Live", "End Event", "Invitation Sent", "Event Reminder" -> {
                gotoEventDetails(eventId = notify.eventId)
            }
            "Watch Live Moments" -> {
                if (!notify.eventId.isNullOrEmpty() && !notify.liveMomentId.isNullOrEmpty()) {
                    gotoWatchLiveMoments(eventId = notify.eventId, liveMomentId = notify.liveMomentId)
                }else if (!notify.eventId.isNullOrEmpty()) {
                    gotoLiveMomentsAll(eventId = notify.eventId)
                }
            }
            "Comment", "Like", "Comment Like", "Post" -> {
                gotoPostComment(postId = notify.postId, eventId = notify.eventId)
            }
            "Friend Request" -> {
                gotoFriendRequests()
            }
            "Friend Request Accepted" -> {
                gotoUserProfile(userId = notify.uid)
            }
            "Invitation Accepted", "Event Joined" -> {
                gotoEventGuests(eventId = notify.eventId)
            }
            "Coin" -> {
                gotoCoinStar(userId = UserInfo.userId)
            }
            "Event Deleted", "Event Cancelled", "Event Expired" -> {
                notify.takeIf { !it.uid.isNullOrEmpty() && it.uid == UserInfo.userId }?.also {
                    gotoEventDetails(eventId = it.eventId)
                }
            }
            else -> {
                gotoEventDetails(eventId = notify?.eventId)
            }
        }
    }

    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        super.onSelectedMenuItem(position, menuItem, data)

        when (data) {
            is NotificationActivityModel -> {
                when(menuItem?.titleText) {
                    "Delete" -> {
                        viewModel.deleteNotification(data as? NotificationActivityModel)
                    }
                }
            }
        }
    }

}