package com.planscollective.plansapp.fragment.users

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.adapters.AddFriendsAdapter
import com.planscollective.plansapp.databinding.FragmentAddFriendsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.AddFriendsVM
import permissions.dispatcher.*

@RuntimePermissions
class AddFriendsFragment : PlansBaseFragment<FragmentAddFriendsBinding>() {

    private val viewModel: AddFriendsVM by viewModels()
    private val args: AddFriendsFragmentArgs by navArgs()
    private val adapterList = AddFriendsAdapter()
    override var screenName: String? = "FindFriends_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddFriendsBinding.inflate(inflater, container, false)
        viewModel.userId = args.userId?.takeIf { it.isNotBlank() } ?: UserInfo.userId
        viewModel.user = args.userModel

        viewModel.typeSelected.observe(viewLifecycleOwner, {
            refreshAll()
        })

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
            viewModel.searchUsers()
        }

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

        // Open Settings UI
        binding.btnOpenSettings.setOnSingleClickListener(this)
        binding.layoutOpenSettings.visibility = View.GONE

        if (viewModel.typeSelected.value == null) {
            viewModel.typeSelected.value = AddFriendsVM.UserType.PLANS_USERS
        }else {
            refreshAll()
        }

        // Top Tabs
        binding.apply {
            layoutPlansUsers.setOnSingleClickListener(this@AddFriendsFragment)
            layoutContacts.setOnSingleClickListener(this@AddFriendsFragment)
        }
        updateTabsUI(viewModel.typeSelected.value)
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            if (viewModel.typeSelected.value == AddFriendsVM.UserType.CONTACTS) {
                refreshContactsWithPermissionCheck(isShownLoading)
            }else {
                viewModel.getAllList(isShownLoading)
            }
        }
        return isBack
    }

    override fun getNextPage(isShownLoading: Boolean) {
        super.getNextPage(isShownLoading)
        viewModel.getNextPage(requireContext(), isShownLoading)
    }

    private fun updateUI() {
        binding.viewModel = viewModel

        updateTabsUI(viewModel.typeSelected.value)

        adapterList.updateAdapter(viewModel.listUsers, viewModel.listContacts, viewModel.typeSelected.value, this)

        // Empty UI
        updateEmptyUI(viewModel.enableAccessForContacts)
    }

    private fun updateTabsUI(type: AddFriendsVM.UserType?) {
        binding.apply {
            bottomPlansUsers.visibility = if (type == AddFriendsVM.UserType.PLANS_USERS) View.VISIBLE else View.INVISIBLE
            bottomContacts.visibility = if (type == AddFriendsVM.UserType.CONTACTS) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateEmptyUI(enableAccessForContacts: Boolean = true) {
        binding.apply {
            if (viewModel?.listUsers?.isEmpty() == true) {
                when(viewModel?.typeSelected?.value) {
                    AddFriendsVM.UserType.PLANS_USERS -> {
                        layoutOpenSettings.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                        imvMark.visibility = View.VISIBLE
                        if (viewModel?.keywordSearch?.isNotBlank() == true) {
                            tvMessage.text = "Sorry! No Users Found."
                        }else {
                            tvMessage.text = "Find Friends"
                        }
                    }
                    AddFriendsVM.UserType.CONTACTS -> {
                        if (viewModel?.listContacts?.isEmpty() == true) {
                            if (enableAccessForContacts) {
                                layoutOpenSettings.visibility = View.GONE
                                layoutEmpty.visibility = View.VISIBLE
                                if (viewModel?.keywordSearch?.isNotBlank() == true) {
                                    tvMessage.text = "Sorry! No contacts found."
                                }else {
                                    tvMessage.text = "No contacts yet."
                                }
                            }else {
                                layoutOpenSettings.visibility = View.VISIBLE
                                layoutEmpty.visibility = View.GONE
                            }
                        }else {
                            layoutOpenSettings.visibility = View.GONE
                            layoutEmpty.visibility = View.GONE
                        }
                    }
                }
            }else {
                layoutEmpty.visibility = View.GONE
                layoutOpenSettings.visibility = View.GONE
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
                layoutPlansUsers -> {
                    if (viewModel?.typeSelected?.value != AddFriendsVM.UserType.PLANS_USERS) {
                        hideKeyboard()
                        viewModel?.pageNumber = 1
                        viewModel?.typeSelected?.value = AddFriendsVM.UserType.PLANS_USERS
                    }
                }
                layoutContacts -> {
                    if (viewModel?.typeSelected?.value != AddFriendsVM.UserType.CONTACTS) {
                        hideKeyboard()
                        viewModel?.pageNumber = 1
                        viewModel?.typeSelected?.value = AddFriendsVM.UserType.CONTACTS
                    }
                }
                btnOpenSettings -> {
                    gotoSettings()
                }
            }
        }
    }

    //******************************* Contacts Permission **************************************//
    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun refreshContacts(isShownLoading: Boolean = true) {
        viewModel.enableAccessForContacts = true
        viewModel.getAllList(isShownLoading)
    }

    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    fun showRationaleForContacts(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    fun onContactsDenied() {
        viewModel.enableAccessForContacts = false
        viewModel.listUsers.clear()
        viewModel.listContacts.clear()
        viewModel.didLoadData.value = true
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    fun onContactsNeverAskAgain() {
        viewModel.enableAccessForContacts = false
        viewModel.listUsers.clear()
        viewModel.listContacts.clear()
        viewModel.didLoadData.value = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

}