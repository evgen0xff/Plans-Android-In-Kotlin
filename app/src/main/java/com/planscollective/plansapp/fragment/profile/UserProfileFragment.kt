package com.planscollective.plansapp.fragment.profile

import android.graphics.Color
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.adapters.UserProfileAdapter
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.FragmentUserProfileBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toDp
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.EventType
import com.planscollective.plansapp.models.viewModels.UserProfileVM

class UserProfileFragment : PlansBaseFragment<FragmentUserProfileBinding>() {

    private val viewModel: UserProfileVM by viewModels()
    private val args: UserProfileFragmentArgs by navArgs()
    private var adapterProfile = UserProfileAdapter(UserProfileAdapter.ProfileType.USER_PROFILE)
    private val arrayTabTitles = ArrayList<TextView>()
    private val arrayTabBottoms = ArrayList<View>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        viewModel.userId = args.userId
        viewModel.user = args.userModel
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            updateUI()
        })
        viewModel.selectedType.observe(viewLifecycleOwner, Observer {
            refreshAll(false)
        })

        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Top Bar
        binding.apply {
            layoutNavTopBar.visibility = View.GONE
            layoutTabsTop.visibility = View.GONE

            arrayTabTitles.clear()
            arrayTabBottoms.clear()
            arrayTabTitles.addAll(arrayOf(tvOrganized, tvAttending))
            arrayTabBottoms.addAll(arrayOf(bottomOrganized, bottomAttending))

            btnBack.setOnSingleClickListener(this@UserProfileFragment)
            btnMore.setOnSingleClickListener(this@UserProfileFragment)
            layoutOrganized.setOnSingleClickListener(this@UserProfileFragment)
            layoutAttending.setOnSingleClickListener(this@UserProfileFragment)
        }
        updateTabItems(viewModel.selectedType.value)

        // Event List Recycler View
        binding.recyclerView.layoutManager = object: LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                for (i in 0 until childCount) {
                    getChildAt(i)?.also {
                        viewModel.childSizesMap[getPosition(it)] = it.height
                    }
                }
            }

            override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
                if (childCount == 0) {
                    return 0
                }
                var scrolledY: Int = 0
                getChildAt(0)?.also {
                    val firstChildPosition = getPosition(it)
                    scrolledY = -it.y.toInt()
                    for (i in 0 until firstChildPosition) {
                        scrolledY += viewModel.childSizesMap[i] ?: 0
                    }
                }
                return scrolledY
            }
        }
        binding.recyclerView.adapter = adapterProfile
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset = recyclerView.computeVerticalScrollOffset().toDp()
                val heightOverlay = viewModel.heightProfile - (55 + 45)
                binding.apply {
                    if (offset < 50) {
                        layoutNavTopBar.visibility = View.GONE
                        layoutTabsTop.visibility = View.GONE
                    }else if (offset <= heightOverlay) {
                        layoutNavTopBar.visibility = View.VISIBLE
                        layoutTabsTop.visibility = View.GONE
                    }else {
                        layoutNavTopBar.visibility = View.VISIBLE
                        layoutTabsTop.visibility = View.VISIBLE
                    }
                }
            }
        })

        // Refresh Layout
        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        if (viewModel.selectedType.value != null ) {
            refreshAll(false)
        }else {
            viewModel.selectedType.value = EventType.ORGANIZED
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

    private fun updateUI() {
        // ViewModel
        binding.viewModel = viewModel

        // Top Nav Bar
        updateTopBar()

        // Update Profile Adapter
        adapterProfile.updateAdapter(viewModel.user, viewModel.listEvents, viewModel.selectedType.value, this)
    }

    private fun updateTopBar() {
        binding.apply {
            tvUserName.text = viewModel?.user?.fullName ?: viewModel?.user?.name ?: "${viewModel?.user?.firstName ?: ""} ${viewModel?.user?.lastName ?: ""}"
            tvUserAddress.visibility = viewModel?.user?.userLocation?.takeIf{ it.isNotEmpty() }?.let {
                tvUserAddress.text = it
                View.VISIBLE
            } ?: View.GONE
        }
        updateTabItems(viewModel.selectedType.value)
    }

    private fun updateTabItems(type: EventType?) {
        val tab = type ?: EventType.ORGANIZED
        for (i in 0 until arrayTabTitles.size) {
            val tvTitle = arrayTabTitles[i]
            if (i == tab.value) {
                tvTitle.setTextColor(PlansColor.PURPLE_JOIN)
            }else {
                tvTitle.setTextColor(PlansColor.BLACK)
            }
        }

        for (i in 0 until arrayTabBottoms.size) {
            val viewBottom = arrayTabBottoms[i]
            if (i == tab.value) {
                viewBottom.setBackgroundColor(PlansColor.PURPLE_JOIN)
            }else {
                viewBottom.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun gotoAppSettings() {

    }

    //************************************* OnSingleClickListener *********************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                btnMore -> {
                    MenuOptionHelper.showPlansMenu(
                        viewModel?.user,
                        MenuOptionHelper.MenuType.USER_PROFILE,
                        this@UserProfileFragment,
                        this@UserProfileFragment)
                }
                layoutOrganized -> {
                    viewModel?.selectedType?.value = EventType.ORGANIZED
                }
                layoutAttending -> {
                    viewModel?.selectedType?.value = EventType.ATTENDING
                }
            }
        }
    }


    //********************************** Plans Action Listener ***********************************//

    override fun onClickedEventsTab(type: EventType, user: UserModel?) {
        viewModel.selectedType.value = type
    }

    override fun onClickedSettings(user: UserModel?) {
        gotoAppSettings()
    }

    override fun onViewSize(size: Size?) {
        size?.height?.also {
            viewModel.heightProfile = it.toDp()
        }
    }

    override fun onClickedMoreMenuUser(user: UserModel?, data: Any?) {
        MenuOptionHelper.showPlansMenu(user, MenuOptionHelper.MenuType.USER_PROFILE, this, this)
    }

}