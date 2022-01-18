package com.planscollective.plansapp.fragment.event

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.adapters.PeopleAdapter
import com.planscollective.plansapp.adapters.TabItemAdapter
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.FragmentPeopleInvitedBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.MenuModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.PeopleInvitedVM

class PeopleInvitedFragment : PlansBaseFragment<FragmentPeopleInvitedBinding>(), OnItemTouchListener {

    private val viewModel: PeopleInvitedVM by viewModels()
    private var tabItemAdapter = TabItemAdapter()
    private var adapterPeople = PeopleAdapter()
    private val args : PeopleInvitedFragmentArgs by navArgs()
    override var screenName: String? = "GuestList_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPeopleInvitedBinding.inflate(inflater, container, false)

        viewModel.selectedType.observe(viewLifecycleOwner, {
            refreshAll()
        })
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            updateUI()
        })

        viewModel.actionEnterKey = actionEnterKey
        viewModel.eventId = args.eventId
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

        // Top TabBar
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recvTabBar.layoutManager = layoutManager
        binding.recvTabBar.adapter = tabItemAdapter
        tabItemAdapter.updateAdapter(viewModel.listTabItems, this, viewModel.selectedTabIndex)

        // Event List
        val layoutMangerList = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = adapterPeople

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

    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)
        binding.viewModel = viewModel
        // Top tab bar UI
        tabItemAdapter.updateAdapter(viewModel.listTabItems, this, viewModel.selectedTabIndex)

        // People UI
        adapterPeople.updateAdapter(viewModel.listUsers, viewModel.eventModel, this)

        // Description UI
        updateDescriptionUI()

        // Empty UI
        updateEmptyUI()
    }


    private fun updateEmptyUI() {
        binding.layoutEmpty.visibility = if (viewModel.listUsers.size > 0) View.GONE else {
            binding.tvEmpty.text = when (viewModel.selectedType.value) {
                PeopleInvitedVM.JoinType.INVITED -> {
                    "No invited friends"
                }
                PeopleInvitedVM.JoinType.LIVE -> {
                    "No live friends"
                }
                PeopleInvitedVM.JoinType.GOING,
                PeopleInvitedVM.JoinType.MAYBE,
                PeopleInvitedVM.JoinType.NEXT_TIME-> {
                    "No friends responded"
                }
                else -> {
                    "No user found"
                }
            }
            View.VISIBLE
        }
    }

    private fun updateDescriptionUI() {
        binding.layoutDescription.visibility = if (viewModel.eventModel?.userId != UserInfo.userId) View.GONE else {
            viewModel.eventModel?.let { it ->
                val (pendingEmails, pendingMobiles, pendingLinks)  = it.getPendingInvitations()
                getSpanTextForPendingInvite(pendingEmails, pendingMobiles, pendingLinks)?.let { it1 ->
                    binding.tvDescription.text = it1
                    View.VISIBLE
                } ?: View.GONE
            } ?: View.GONE
        }
    }

    private fun getSpanTextForPendingInvite (emails: Int = 0, mobiles: Int = 0, links: Int = 0) : SpannableStringBuilder? {

        var spannable : SpannableStringBuilder? = null
        val arraySpanText = ArrayList<String>()
        val arrayOriginal = ArrayList<String>()

        // Emails
        if (emails > 0) {
            val text = if (emails > 1) "$emails emails" else "1 email"
            arraySpanText.add(text)
        }

        // Contacts
        if (mobiles > 0) {
            val text = if (mobiles > 1) "$mobiles contacts" else "1 contact"
            arraySpanText.add(text)
        }

        if (arraySpanText.size > 0) {
            arrayOriginal.addAll(arrayListOf("Pending invite ", " and "))
            spannable = SpannableStringBuilder(arrayOriginal.first())

            when(arraySpanText.size) {
                1 -> {
                    spannable.append(arraySpanText.first(), ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                2 -> {
                    spannable.append(arraySpanText.first(), ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.append(arrayOriginal[1])
                    spannable.append(arraySpanText[1], ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                else -> {}
            }
        }

        if (links > 0) {
            val text = if (links > 1) "$links guests" else "1 guest"
            if (spannable == null) {
                spannable = SpannableStringBuilder()
            }
            if (arraySpanText.size > 0) {
                spannable.append("\n")
            }
            spannable.append(text, ForegroundColorSpan(PlansColor.TEAL_MAIN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append(" responded by link.")
        }

        return spannable
    }


    //******************************* Top TabBar click listener *********************************//
    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        if (viewModel.selectedTabIndex != position) {
            viewModel.pageNumber = 1
            viewModel.selectedType.value = viewModel.userTypes[position]
        }
    }


    //******************************* OnSingleClickListener *********************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
        }
    }

    //********************************** Plans Action Listener ***************************//
    override fun onClickedMoreMenuUser(user: UserModel?, data: Any?) {
        MenuOptionHelper.showPlansMenu(user, MenuOptionHelper.MenuType.PEOPLE_INVITED, this, this)
    }

    //********************************* Menu options Listener **************************************//
    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        when(menuItem?.titleText) {
            "Remove Guest" -> {
                removeGuestFromEvent(data as? UserModel, viewModel.eventModel){
                    success, message ->
                    if (success) {
                        refreshAll()
                    }else {
                        ToastHelper.showMessage(message)
                    }
                }
            }
            else -> {}
        }
    }





}