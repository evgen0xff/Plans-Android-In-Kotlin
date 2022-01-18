package com.planscollective.plansapp.fragment.chats

import android.Manifest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.ChattingAdapter
import com.planscollective.plansapp.databinding.FragmentChattingBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.stringDate
import com.planscollective.plansapp.extension.toLocalDateTime
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.KeyboardHelper
import com.planscollective.plansapp.helper.MediaPickerHelper
import com.planscollective.plansapp.helper.OnKeyboardListener
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.MediaPickerListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.ChattingVM
import com.planscollective.plansapp.webServices.chat.ChatService
import permissions.dispatcher.*

@RuntimePermissions
class ChattingFragment : PlansBaseFragment<FragmentChattingBinding>(), MediaPickerListener, TextWatcher,
    OnKeyboardListener {

    private val viewModel : ChattingVM by viewModels()
    private val args: ChattingFragmentArgs by navArgs()
    private val adapterList = ChattingAdapter()
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChattingBinding.inflate(inflater, container, false)

        viewModel.initializeData(args.chatModel)

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI(viewModel.isScrollToBottom)
        })
        viewModel.messagePosting.observe(viewLifecycleOwner, Observer {
            updatePostingUI()
        })
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Keyboard Listener
        KeyboardHelper.listenKeyboardEvent(this, this)

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Title
        binding.tvTitle.text = viewModel.chatModel?.titleChat
        binding.tvDescription.visibility = View.GONE

        // Menu Button
        binding.btnSettings.setOnSingleClickListener(this)

        // Date TextView
        updateDateUI(true)

        // Recycler View
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapterList

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                layoutManager.findFirstVisibleItemPosition().takeIf { it >= 0 }?.also {
                    if (it < adapterList.itemCount) {
                        adapterList.listItems[it].createdAt?.also { createdAt ->
                            updateDateUI(numDate = createdAt.toDouble())
                        }
                    }
                }

                binding.btnScrollDown.visibility = if (layoutManager.findLastVisibleItemPosition() == adapterList.itemCount - 1) View.GONE else View.VISIBLE
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        updateDateUI(true)
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        updateDateUI(false)
                    }
                }
            }
        })

        //Scroll Down Button
        binding.btnScrollDown.visibility = View.GONE
        binding.btnScrollDown.setOnSingleClickListener(this)

        // Typing Users UI
        binding.tvTypingUsers.visibility = View.GONE

        // Posting UI
        binding.btnSend.setOnSingleClickListener(this)
        binding.btnAttach.setOnSingleClickListener(this)
        binding.imgvUserImage.setUserImage(UserInfo.profileUrl)

        updatePostingUI()

        // Get Messages
        viewModel.getSomeOneIsType{
            updateTypingUsersUI()
        }

        // Get Event
        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack && !isUpdateLocation) {
            viewModel.getChatData()
        }
        return isBack
    }

    override fun onResume() {
        super.onResume()
        // Join Chat
        viewModel.joinChat()
        binding.etMessage.addTextChangedListener (this)
    }

    override fun onPause() {
        super.onPause()
        // Close Chat
        viewModel.closeChat()
        binding.etMessage.removeTextChangedListener (this)
    }

    private fun updateUI(isScrollToBottom: Boolean = true) {
        if (viewModel.chatModel?.isEventChat == true ) {
            saveInfo(viewModel.chatModel?.eventId)
        }else if (viewModel.chatModel?.isGroup == false) {
            saveInfo(userId = viewModel.chatModel?.profileUser?.id)
        }

        binding.viewModel = viewModel
        adapterList.updateAdapter(viewModel.chatModel, viewModel.listMessages, this)
        updatePostingUI()
        updateAccessUI()
        updateTypingUsersUI(isScrollToBottom)

        binding.apply {
            tvTitle.text = viewModel?.chatModel?.titleChat
            tvDescription.visibility = if (viewModel?.chatModel?.isGroup == true) {
                val count = viewModel?.chatModel?.people?.size ?: 0
                tvDescription.text = "$count ${if(count > 1) "people" else "person"}"
                View.VISIBLE
            } else View.GONE

            if (isScrollToBottom) {
                scrollToBottom()
            }
        }

    }

    private fun updateDateUI(isHidden: Boolean? = null, numDate: Double? = null, text: String? = null) {
        binding.apply {
            isHidden?.also { tvDate.visibility = if (it) View.GONE else View.VISIBLE }
            text?.takeIf { it.isNotEmpty() }?.also { tvDate.text = it }
            numDate?.also { tvDate.text = it.toLocalDateTime().stringDate() }
        }
    }

    private fun updateAccessUI() {
        binding.apply {
            val event = viewModel?.eventModel
            val chat = viewModel?.chatModel
            tvAccessMsg.visibility = if (event != null && event.isChatEnd == true && event.userId != UserInfo.userId) {
                hideKeyboard()
                tvAccessMsg.text = if (event.isActive == false || event.isCancel == true){
                    "This event is cancelled."
                }else if (event.isEnded == 1){
                    "This event has ended."
                }else if (event.isExpired == true){
                    "This event has expired."
                }else {
                    "This event isn't available for you."
                }
                layoutSending.visibility = View.GONE
                View.VISIBLE
            }else if (chat != null && !chat.isGroup && chat.profileUser?.isActive == false){
                tvAccessMsg.text = "This user is unavailable on Plans."
                layoutSending.visibility = View.GONE
                View.VISIBLE
            }else {
                layoutSending.visibility = View.VISIBLE
                View.GONE
            }
        }
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

    private fun updateTypingUsersUI(isScrollToBottom: Boolean = true) {
        binding.viewModel = viewModel
        binding.apply {
            tvTypingUsers.visibility = if(viewModel?.typingData?.isTyping == false) View.GONE else {
                tvTypingUsers.text = viewModel?.typingData?.textTyping
                View.VISIBLE
            }
            if (isScrollToBottom) {
                scrollToBottom()
            }
        }
    }

    private fun scrollToBottom() {
        binding.apply {
            adapterList.itemCount.takeIf { it > 0 }?.also {
                recyclerView.scrollToPosition(it - 1)
            }
        }
    }

    private fun sendMessage() {
        viewModel.sendMessage()
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                btnSettings -> {
                    gotoChatSettings(viewModel?.chatModel)
                }
                btnScrollDown -> {
                    scrollToBottom()
                }
                btnSend -> {
                    sendMessage()
                    ChatService.updateIsTyping(viewModel?.chatModel?.id, false)
                }
                btnAttach -> {
                    showCameraGalleryWithPermissionCheck()
                }
            }
        }
    }

    //******************************* Permissions for Camera and Gallery **************************************//
    @NeedsPermission(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showCameraGallery() {
        MediaPickerHelper.showPickerOptions(this, this)
    }

    @OnShowRationale(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForPermissions(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    @OnNeverAskAgain(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionNeverAskAgain() {
        ToastHelper.showMessage("Permission denied for Camera and Gallery, you can turn them on on Settings.")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }


    //********************************* MediaPickerListener **************************************//
    override fun onSelectedPhoto(filePath: String?) {
        viewModel.urlPhotoPosting = filePath
        viewModel.urlVideoPosting = null
        sendMessage()
    }

    override fun onSelectedVideo(filePath: String?) {
        viewModel.urlVideoPosting = filePath
        viewModel.urlPhotoPosting = null
        sendMessage()
    }

    // **************************** TextWatcher *******************************************//
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        ChatService.updateIsTyping(viewModel.chatModel?.id, true)
    }

    //************************************* Keyboard Listener ***********************************//
    override fun onShownKeyboard() {
        super.onShownKeyboard()
        scrollToBottom()
    }



}