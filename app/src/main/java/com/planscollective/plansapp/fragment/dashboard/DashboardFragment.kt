package com.planscollective.plansapp.fragment.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentDashboardBinding
import com.planscollective.plansapp.extension.setBadge
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.BaseFragment
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.fragment.home.HomeFragment
import com.planscollective.plansapp.fragment.location.LocationDiscoveryFragment
import com.planscollective.plansapp.fragment.notifications.NotificationsFragment
import com.planscollective.plansapp.fragment.profile.MyProfileFragment
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import com.planscollective.plansapp.models.viewModels.DashboardVM
import com.planscollective.plansapp.webServices.notifications.NotificationsWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : PlansBaseFragment<FragmentDashboardBinding>(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val viewModel: DashboardVM by viewModels()

    var homeFragment: HomeFragment? = null
    var locationDiscoveryFragment: LocationDiscoveryFragment? = null
    var notificationsFragment: NotificationsFragment? = null
    var myProfileFragment: MyProfileFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel.selectedItemId.observe(viewLifecycleOwner, Observer {
            attachItem(it)
        })
        return binding.root
    }

    override fun initializeData() {
        super.initializeData()
        PLANS_APP.dashboardFragment = this
        PLANS_APP.getLivedEventsForEnding()
    }

    override fun setupUI() {
        super.setupUI()

        binding.also {
            it.imvPlus.setOnSingleClickListener {
                gotoCreateEvent()
            }
            it.bottomNavView.setOnNavigationItemSelectedListener (this)
            it.bottomNavView.itemIconTintList = null
        }

        viewModel.selectedItemId.value?.also { attachItem(it) }
        updateTabBar()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            getItemFragment(viewModel.selectedItemId.value)?.refreshAll(isShownLoading, isUpdateLocation)
        }
        return isBack
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        viewModel.selectedItemId.value = item.itemId
        return true
    }

    override fun onResume() {
        super.onResume()
        PLANS_APP.updateBadges()
    }
    override fun onDestroy() {
        super.onDestroy()
        PLANS_APP.dashboardFragment = null
    }

    private fun getItemFragment(resItemId: Int?) : BaseFragment<*>? {
       return when (resItemId) {
            R.id.menu_home -> {
                if (homeFragment == null) {
                    homeFragment = HomeFragment()
                }
                homeFragment
            }
            R.id.menu_location -> {
                if (locationDiscoveryFragment == null) {
                    locationDiscoveryFragment = LocationDiscoveryFragment()
                }
                locationDiscoveryFragment
            }
            R.id.menu_notification -> {
                if (notificationsFragment == null) {
                    notificationsFragment = NotificationsFragment()
                }
                notificationsFragment
            }
            R.id.menu_profile -> {
                if (myProfileFragment == null) {
                    myProfileFragment = MyProfileFragment()
                }
                myProfileFragment
            }
           else -> null
       }
    }

    private fun attachItem(resItemId: Int) {
        val itemFragment = getItemFragment(resItemId).takeIf { it != null } ?: return
        childFragmentManager.beginTransaction()
        childFragmentManager.commit {
            setReorderingAllowed(true)
            if (!childFragmentManager.fragments.contains(itemFragment)) {
                add(R.id.frame_container, itemFragment)
            }
            attach(itemFragment)
            childFragmentManager.fragments.forEach {
                if (it != itemFragment){
                    setMaxLifecycle(it, Lifecycle.State.STARTED)
                    detach(it)
                }
            }
        }
    }

    private fun showItem(resItemId: Int) {
        val itemFragment = getItemFragment(resItemId).takeIf { it != null } ?: return
        childFragmentManager.beginTransaction()
        childFragmentManager.commit {
            setReorderingAllowed(true)
            if (!childFragmentManager.fragments.contains(itemFragment)) {
                add(R.id.frame_container, itemFragment)
            }
            childFragmentManager.fragments.forEach {
                if (it == itemFragment){
                    show(it)
                }else {
                    hide(it)
                }
            }
        }
        itemFragment.refreshAll()
    }

    fun updateTabBar() {
        updateNotifyBadge()
    }

    fun updateBadges() {
        NotificationsWebService.getUnviewedNotifications { dataUnviewed, message ->
            UserInfo.countUnviewedChatMsgs = dataUnviewed?.listChatsUnviewed?.size ?: 0
            UserInfo.countUnviewedNotify = dataUnviewed?.listNotifyUnviewed?.size ?: 0

            homeFragment?.updateChatsBadge()
            updateNotifyBadge()
        }
    }

    fun updateNotifyBadge() {
        _binding?.apply {
            bottomNavView.setBadge(R.id.menu_notification, UserInfo.countUnviewedNotify)
        }
    }


    fun gotoNotification(notification: NotificationActivityModel?) {
        when(notification?.notificationType) {
            "Private Message", "Event Chat" -> {
                _binding?.bottomNavView?.selectedItemId = R.id.menu_home
            }
            else -> {
                _binding?.bottomNavView?.selectedItemId = R.id.menu_notification
            }
        }

        lifecycleScope.launch(Dispatchers.IO){
            delay(300)
            withContext(Dispatchers.Main){
                when(notification?.notificationType) {
                    "Private Message", "Event Chat" -> {
                        homeFragment?.gotoChats(notification)
                    }
                    else -> {
                        notificationsFragment?.gotoNotification(notification)
                    }
                }
            }
        }
    }

}