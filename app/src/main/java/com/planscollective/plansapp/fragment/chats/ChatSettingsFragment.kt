package com.planscollective.plansapp.fragment.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.ChatSettingsAdapter
import com.planscollective.plansapp.customUI.PlansEditTextView
import com.planscollective.plansapp.customUI.PlansEditTextViewListener
import com.planscollective.plansapp.databinding.FragmentChatSettingsBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.MenuOptionHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.MenuModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.ChatSettingsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatSettingsFragment : PlansBaseFragment<FragmentChatSettingsBinding>(), PlansEditTextViewListener {

    private val viewModel: ChatSettingsVM by viewModels()
    private val args: ChatSettingsFragmentArgs by navArgs()
    private val adapterList = ChatSettingsAdapter()
    private val listOptions = ArrayList<ViewGroup>()
    private val listOptionSeparators = ArrayList<View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatSettingsBinding.inflate(inflater, container, false)
        viewModel.chat = args.chatModel
        viewModel.event = args.chatModel?.event
        viewModel.chatId = args.chatId
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.apply {
            btnBack.setOnSingleClickListener(this@ChatSettingsFragment)

            etGroupName.text = viewModel?.chat?.titleChat
            etGroupName.listener = this@ChatSettingsFragment

            switchOption.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked != viewModel?.chat?.isMuteNotification){
                    muteChatNotify(viewModel?.chat, isChecked)
                    viewModel?.chat?.isMuteNotification = isChecked
                }
            }

            btnAdd.setOnSingleClickListener(this@ChatSettingsFragment)
            btnDelete.setOnSingleClickListener(this@ChatSettingsFragment)
            btnLeave.setOnSingleClickListener(this@ChatSettingsFragment)

            listOptions.clear()
            listOptions.add(btnAdd)
            listOptions.add(btnDelete)
            listOptions.add(btnLeave)

            listOptionSeparators.clear()
            listOptionSeparators.add(separatorAdd)
            listOptionSeparators.add(separatorDelete)
            listOptionSeparators.add(separatorLeave)

            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapterList
        }

        refreshAll(false)
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            if (!isUpdateLocation) {
                viewModel.getChatDetails()
            }
        }
        return isBack
    }


    private fun updateUI() {
        if (viewModel.chat?.isEventChat == true ) {
            saveInfo(viewModel.chat?.eventId)
        }else if (viewModel.chat?.isGroup == false) {
            saveInfo(userId = viewModel.chat?.profileUser?.id)
        }

        binding.viewModel = viewModel
        binding.apply {
            // Group Name
            updateGroupNameUI(etGroupName.text)

            // Mute Notification
            switchOption.isChecked = viewModel?.chat?.isMuteNotification ?: false

            // Add People
            val options = viewModel?.chat?.getMenuOptions()
            btnAdd.visibility = options?.firstOrNull { it.titleText == "Add People" || it.titleText == "Invite People" }?.let {
                tvAdd.text = it.titleText
                View.VISIBLE
            } ?: View.GONE

            // Leave Chat
            btnLeave.visibility = options?.firstOrNull { it.titleText == "Leave Chat" ||
                    it.titleText == "Leave Event" || it.titleText == "End Event" || it.titleText == "Cancel Event"
            }?.let {
                tvLeave.text = it.titleText
                View.VISIBLE
            } ?: View.GONE

            // Last Separator
            listOptionSeparators.forEach{it.visibility = View.VISIBLE}
            listOptions.indexOfLast { it.visibility == View.VISIBLE }.takeIf { it >= 0 }?.also {
                listOptionSeparators[it].visibility = View.GONE
            }
        }

        adapterList.updateAdapter(viewModel.chat,this)
    }

    private fun updateGroupNameUI(text: String? = null) {
        binding.apply {
            val isEventChat = viewModel?.chat?.isEventChat ?: false
            etGroupName.visibility = if (isEventChat || viewModel?.chat?.isGroup == false) View.GONE else View.VISIBLE

            var groupName = viewModel?.chat?.titleChat
            if (etGroupName.visibility == View.VISIBLE) {
                text?.takeIf { it != viewModel?.chat?.titleChat }?.also {
                    groupName = it
                    etGroupName.tvAction?.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
                } ?: run {
                    etGroupName.tvAction?.visibility = View.GONE
                }
            }

            if (etGroupName.text != groupName)
                etGroupName.text = groupName
        }

    }

    //****************************************** OnSingleClickListener ***************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                btnAdd -> {
                    hideKeyboard()
                    actionChat(tvAdd.text.toString(), viewModel?.chat)
                }
                btnDelete -> {
                    hideKeyboard()
                    actionChat(tvDelete.text.toString(), viewModel?.chat){ success, _ ->
                        if (success) {
                            val destId = preBackStackEntry?.destination?.id ?: curBackStackEntry?.destination?.id ?: return@actionChat
                            gotoBack(destId, true)
                        }
                    }
                }
                btnLeave -> {
                    hideKeyboard()
                    actionChat(tvLeave.text.toString(), viewModel?.chat){ success, _ ->
                        if (success) {
                            gotoDashboard()
                        }
                    }
                }
            }
        }
    }



    //********************************** PlansEditTextViewListener ******************************//
    override fun didChangedText(text: String?, editText: PlansEditTextView?){
        updateGroupNameUI(text)
    }

    override fun didClickedAction(editText: PlansEditTextView?) {
        hideKeyboard()
        viewModel.updateGroupName(editText?.text?.trim())

        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                editText?.editText?.isFocusableInTouchMode = false
                editText?.editText?.isFocusable = false
                editText?.editText?.isFocusableInTouchMode = true
                editText?.editText?.isFocusable = true
            }
        }
    }

    override fun onClickedMoreMenuUser(user: UserModel?, data: Any?) {
        val list = ArrayList<MenuModel>()
        if (viewModel.chat?.isEventChat == true)
            list.add(MenuModel(R.drawable.ic_trash_black_menu, "Remove Guest"))
        else
            list.add(MenuModel(R.drawable.ic_trash_black_menu, "Remove User"))

        MenuOptionHelper.showBottomMenu(list, this, this, user)
    }

    override fun onSelectedMenuItem(position: Int, menuItem: MenuModel?, data: Any?) {
        when (menuItem?.titleText) {
            "Remove Guest" -> {
                removeGuestFromEvent(data as? UserModel, viewModel.chat?.event){ success, message ->
                    if (success) {
                        refreshAll(false)
                    }else {
                        ToastHelper.showMessage(message)
                    }
                }
            }
            "Remove User" -> {
                removeUserInChat(viewModel.chat, data as? UserModel){ success, _ ->
                    if (success) {
                        refreshAll(false)
                    }
                }
            }
        }
    }





}