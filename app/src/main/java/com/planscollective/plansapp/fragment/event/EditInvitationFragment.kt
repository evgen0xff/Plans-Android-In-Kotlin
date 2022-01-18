package com.planscollective.plansapp.fragment.event

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.EditInvitationAdapter
import com.planscollective.plansapp.adapters.TabItemAdapter
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.FragmentEditInvitationBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.MenuModel
import com.planscollective.plansapp.models.viewModels.EditInvitationVM
import com.redmadrobot.inputmask.MaskedTextChangedListener
import permissions.dispatcher.*

@RuntimePermissions
class EditInvitationFragment : PlansBaseFragment<FragmentEditInvitationBinding>(), OnItemTouchListener, MaskedTextChangedListener.ValueListener {

    private val viewModel: EditInvitationVM by viewModels()
    private var tabItemAdapter = TabItemAdapter()
    private var adapterList = EditInvitationAdapter()
    private val args : EditInvitationFragmentArgs by navArgs()
    var listener: MaskedTextChangedListener? = null
    override var screenName: String? = "EventInvitations_Screen"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditInvitationBinding.inflate(inflater, container, false)
        viewModel.selectedType.observe(viewLifecycleOwner, {
            refreshAll()
        })
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()
            updateUI()
        })
        viewModel.emailAdding.observe(viewLifecycleOwner, {
            updateAddEmailUI()
        })

        viewModel.initializeData(args.editMode, args.eventId, args.usersSelected?.toList()?.toArrayList(), actionEnterKey)

        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        curBackStackEntry?.savedStateHandle?.getLiveData<String>(Keys.SELECTED_COUNTRY)?.observe(this){
            changeCountry(it)
        }

        // Back button
        binding.btnBack.setOnSingleClickListener (this)

        // Search
        binding.etSearch.addTextChangedListener {
            viewModel.searchUsers(it.toString())
        }

        // Top TabBar
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recvTabBar.layoutManager = layoutManager
        binding.recvTabBar.adapter = tabItemAdapter
        tabItemAdapter.updateAdapter(viewModel.listTabItems, this, viewModel.selectedTabIndex)

        // User List
        val layoutMangerList = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = adapterList

        binding.refreshLayout.setOnRefreshListener {
            viewModel.pageNumber = 1
            refreshAll(false)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            getNextPage()
        }

        // Add Phone and Email UI
        binding.layoutAdd.visibility = View.GONE

        binding.tvCountryCode.setOnSingleClickListener(this)
        binding.btnAddPhone.setOnSingleClickListener(this)
        changeCountry("US +1")

        binding.btnAddEmail.setOnSingleClickListener(this)


        // No Access UI for Contacts
        binding.btnOpenSettings.setOnSingleClickListener(this)
        binding.layoutOpenSettings.visibility = View.GONE
        binding.editTextEmail.setOnFocusChangeListener { v, hasFocus ->
            updateAddEmailUI()
        }

        setupMobileEditText()

        // Share Link UI
        binding.layoutShareLink.visibility = View.GONE
        binding.btnCopy.setOnSingleClickListener(this)
        binding.btnShareLink.setOnSingleClickListener(this)

        // Done UI
        binding.btnDone.setOnSingleClickListener(this)
        binding.layoutDone.visibility = View.GONE

        // Empty UI
        binding.layoutEmpty.visibility = View.GONE

        // Load Data
        if (viewModel.selectedType.value == null) {
            viewModel.selectedType.value = InvitationModel.InviteType.FRIEND
        }else {
            refreshAll()
        }
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            if (viewModel.selectedType.value == InvitationModel.InviteType.CONTACT) {
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

    //********************************** Update UI ***********************************************//
    private fun setupMobileEditText() {
        viewModel.phoneExtracted = ""
        viewModel.phoneFormatted = ""
        binding.editTextMobile.setText("")

        binding.editTextMobile.removeTextChangedListener(listener)
        binding.editTextMobile.onFocusChangeListener = null

        val primaryFormat = viewModel.phoneCode.getMaskForMobile("0", viewModel.nameCountryCode, requireContext(), true)
        listener = MaskedTextChangedListener.installOn(binding.editTextMobile, primaryFormat, this)

        binding.editTextMobile.hint = listener?.placeholder()
        viewModel.lengthMin = listener?.placeholder()?.replace("-", "")?.length ?: 10
    }

    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)
        binding.viewModel = viewModel
        // Top tab bar UI
        tabItemAdapter.updateAdapter(selectedItem = viewModel.selectedTabIndex)

        // Add Phone and Email UI
        updateAddUI()

        // People UI
        adapterList.updateAdapter(viewModel.listUsers, viewModel.listStatus, viewModel.eventModel, this, viewModel.selectedType.value)

        // Share Link UI
        updateShareLinkUI()

        // Description UI
        updateDoneUI()

        // Empty UI
        updateEmptyUI(viewModel.enableAccessForContacts)
    }

    private fun updateAddUI() {
        binding.apply {
            layoutAdd.visibility = when(viewModel?.selectedType?.value) {
                InvitationModel.InviteType.CONTACT -> {
                    layoutMobile.visibility = View.VISIBLE
                    layoutEmail.visibility = View.INVISIBLE
                    updateAddPhoneUI()
                    View.VISIBLE
                }
                InvitationModel.InviteType.EMAIL -> {
                    layoutMobile.visibility = View.INVISIBLE
                    layoutEmail.visibility = View.VISIBLE
                    updateAddEmailUI()
                    View.VISIBLE
                }
                else -> View.GONE
            }
        }
    }

    private fun updateDoneUI() {
        val selected = viewModel.getSelectedCounts()
        val removed = viewModel.getSelectedCounts(true)

        val spanSelected = getSpanTextForEditing(selected.first, selected.second, selected.third, true)
        val spanRemoved = getSpanTextForEditing(removed.first, removed.second, removed.third, false)

        binding.apply {
            tvDescriptionRemove.visibility = if (spanRemoved == null) View.GONE else {
                tvDescriptionRemove.text = spanRemoved
                tvDone.text = "DONE"
                View.VISIBLE
            }
            tvDescriptionSelect.visibility = if (spanSelected == null) View.GONE else {
                tvDescriptionSelect.text = spanSelected
                tvDone.text = "INVITE"
                View.VISIBLE
            }

            layoutDone.visibility = if (viewModel?.editMode == EditInvitationVM.EditMode.CREATE) {
                tvDone.text = "DONE"
                if (spanRemoved != null || spanSelected != null) View.VISIBLE else {
                    if (args.usersSelected?.size ?: 0 > 0) View.VISIBLE else View.GONE
                }
            }else {
                if (spanRemoved != null || spanSelected != null) View.VISIBLE else View.GONE
            }

        }
    }

    private fun updateEmptyUI(enableAccessForContacts: Boolean = true) {
        binding.apply {
            layoutEmpty.visibility = when(viewModel?.selectedType?.value) {
                InvitationModel.InviteType.FRIEND -> {
                    if (viewModel?.listFriends?.size ?: 0 > 0) View.GONE else {
                        tvEmpty.text = "No friends yet."
                        View.VISIBLE
                    }
                }
                InvitationModel.InviteType.CONTACT -> {
                    if (!enableAccessForContacts){
                        layoutOpenSettings.visibility = View.VISIBLE
                        View.GONE
                    }else {
                        layoutOpenSettings.visibility = View.GONE
                        if (viewModel?.listContacts?.size ?: 0 > 0) View.GONE else {
                            tvEmpty.text = "No contacts yet."
                            View.VISIBLE
                        }
                    }
                }
                InvitationModel.InviteType.EMAIL -> {
                    if (viewModel?.listEmails?.size ?: 0 > 0) View.GONE else {
                        tvEmpty.text = "No emails yet."
                        View.VISIBLE
                    }
                }
                else -> View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAddPhoneUI() {
        println("EditInvitationFragment UpdateAddPhoneUI()")
        binding.apply {
            var color = if (editTextMobile.hasFocus()) {
                tvMobileHintLabel.setTextColor(PlansColor.PURPLE_JOIN)
                viewBottomBarPhone.setBackgroundColor(PlansColor.PURPLE_JOIN)
                PlansColor.BLACK
            }else {
                tvMobileHintLabel.setTextColor(PlansColor.BLACK)
                viewBottomBarPhone.setBackgroundColor(PlansColor.GRAY_LABEL)
                PlansColor.GRAY_LABEL
            }
            editTextMobile.setTextColor(color)

            tvCountryCode.text = "${viewModel?.nameCountryCode} ${viewModel?.phoneCode}"
            val number = viewModel?.phoneExtracted
            imvCheckPhone.visibility = if (number?.isMobileValid(viewModel?.lengthMin ?: 10) == true) {
                color = PlansColor.PURPLE_JOIN
                btnAddPhone.isClickable = true
                View.VISIBLE
            }else {
                color = PlansColor.GRAY_LABEL
                btnAddPhone.isClickable = false
                View.GONE
            }
            btnAddPhone.setTextColor(color)
        }
    }

    private fun updateAddEmailUI() {
        binding.apply {
            var color = if (editTextEmail.hasFocus()) {
                tvEmailHintLabel.setTextColor(PlansColor.PURPLE_JOIN)
                editTextEmail.setTextColor(PlansColor.BLACK)
                PlansColor.PURPLE_JOIN
            }else {
                tvEmailHintLabel.setTextColor(PlansColor.BLACK)
                editTextEmail.setTextColor(PlansColor.GRAY_LABEL)
                PlansColor.GRAY_LABEL
            }
            viewBottomBarEmail.setBackgroundColor(color)

            tvEmailHintLabel.visibility = if (viewModel?.emailAdding?.value.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

            imvCheckEmail.visibility = if (viewModel?.emailAdding?.value?.isEmailValid() == true) {
                color = PlansColor.PURPLE_JOIN
                btnAddEmail.isClickable = true
                View.VISIBLE
            }else {
                color = PlansColor.GRAY_LABEL
                btnAddEmail.isClickable = false
                View.GONE
            }
            btnAddEmail.setTextColor(color)
        }
    }

    private fun updateShareLinkUI() {
        binding.apply {
            layoutShareLink.visibility = if (viewModel?.selectedType?.value != InvitationModel.InviteType.LINK) View.GONE else {
                tvShareLink.text = viewModel?.eventModel?.eventLink?.invitation
                View.VISIBLE
            }
        }
    }

    //**********************************  User Actions *******************************************//

    private fun actionDone() {
        hideKeyboard()
        val selectedUsers = viewModel.getSelectedUsers()
        preBackStackEntry?.savedStateHandle?.set(Keys.SELECTED_USERS, selectedUsers)
        when(viewModel?.editMode) {
            EditInvitationVM.EditMode.CREATE -> {
                gotoBack()
            }
            EditInvitationVM.EditMode.EDIT -> {
                viewModel.updateInvitations(selectedUsers){
                    success, message ->
                    if (success) {
                         gotoBack()
                    }else {
                        ToastHelper.showMessage(message)
                    }
                }
            }
        }

    }

    private fun actionAddEmail() {
        hideKeyboard()
        viewModel.addEmail()
    }

    private fun actionAddPhone() {
        hideKeyboard()
        viewModel.addPhoneNumber(binding.editTextMobile)
    }

    private fun actionPickCountry(){
        hideKeyboard()
        gotoCountryPicker()
    }

    private fun actionCopyShareLink() {
        copyTextToClipBoard("Plans Event Invitation", viewModel.eventModel?.eventLink?.invitation) {
            success, _ ->
            if (success) {
                ToastHelper.showMessage("Link copied")
            }
        }
    }

    private fun actionShareLink() {
        shareEvent(viewModel.eventModel, true)
    }

    //**********************************  Private Methods *******************************************//

    private fun changeCountry(country: String) {
        val codes = country.split(" ")
        codes.firstOrNull()?.also {
            viewModel.nameCountryCode = it
        }
        codes.lastOrNull()?.also {
            viewModel.phoneCode = it
        }

        setupMobileEditText()

        updateAddPhoneUI()
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

    //******************************* Top TabBar click listener *********************************//
    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        if (viewModel.selectedTabIndex != position) {
            hideKeyboard()
            viewModel.pageNumber = 1
            viewModel.selectedType.value = viewModel.invitationTypes[position]
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
                btnOpenSettings -> {
                    gotoSettings()
                }
                btnAddEmail -> {
                    actionAddEmail()
                }
                btnAddPhone -> {
                    actionAddPhone()
                }
                tvCountryCode -> {
                    actionPickCountry()
                }
                btnCopy -> {
                    actionCopyShareLink()
                }
                btnShareLink -> {
                    actionShareLink()
                }
            }
        }
    }

    //******************************* PlansActionListener *********************************//
    override fun onClickedSelect(user: InvitationModel?): Int?{
        return  viewModel.selectUser(user)
    }

    override fun onClickedSelected(user: InvitationModel?): Int?{
        return viewModel.unselectUser(user)
    }

    override fun onClickedInvited(user: InvitationModel?, complete: ((toPosition: Int?) -> Unit)?){
        removeGuestFromEvent(user, eventId = viewModel.eventId, actionName = "uninvite") {
            success, message ->
            if (success) {
                viewModel.cancelUser(user, complete)
            }else {
                complete?.also { it(null) }
                ToastHelper.showMessage(message)
            }
        }
    }

    override fun onClickedMenuInvite(user: InvitationModel?){
        val item = user ?: return
        val list = ArrayList<MenuModel>()
        when(viewModel.selectedType.value) {
            InvitationModel.InviteType.CONTACT -> {
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Contact"))
            }
            InvitationModel.InviteType.EMAIL -> {
                list.add(MenuModel(R.drawable.ic_trash_black_menu, "Delete Email"))
            }
            else -> {}
        }
        list.takeIf { it.size > 0 }?.let {
            MenuOptionHelper.showBottomMenu(list, this, this, item)
        }
    }

    //******************************* Menu Listener *****************************************//
    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        when(menuItem?.titleText) {
            "Delete Contact" -> {
                viewModel.deleteContact(data as? InvitationModel)
            }
            "Delete Email" -> {
                viewModel.deleteEmail(data as? InvitationModel)
            }
            else -> {}
        }
    }

    //******************************* Phone number change Listener *****************************************//
    override fun onTextChanged(
        maskFilled: Boolean,
        extractedValue: String,
        formattedValue: String
    ) {
        viewModel.phoneExtracted = extractedValue
        viewModel.phoneFormatted = formattedValue
        updateAddPhoneUI()
    }


}