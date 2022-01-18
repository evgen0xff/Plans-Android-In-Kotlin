package com.planscollective.plansapp.models.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.*
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.chat.ChatService
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.friend.FriendWebService
import java.util.*
import kotlin.collections.ArrayList

class ChattingVM(application: Application) : ListBaseVM(application) {

    var chatModel: ChatModel? = null
    var eventModel: EventModel? = null
    var listMessages = ArrayList<MessageModel>()
    var typingData = TypingModel()

    var messagePosting = MutableLiveData<String?>()
    var urlPhotoPosting: String? = null
    var urlVideoPosting: String? = null
    var isScrollToBottom = false
    var isRefreshAll = false
    var listBlockedUsers = ArrayList<UserModel>()

    fun initializeData(chat: ChatModel?) {
        if (chatModel != null){
            isScrollToBottom = false
            return
        }

        chatModel = chat
        eventModel = chat?.event
        isScrollToBottom = true

        ChatService.updateChatHidden(chatModel?.id, false)
    }

    fun joinChat() {
        ChatService.joinChat(chatModel?.id, true)
        ChatService.openOutputList()
    }

    fun closeChat() {
        ChatService.joinChat(chatModel?.id, false)
        ChatService.closeOutputList()
    }


    fun getSomeOneIsType(actionTyping: (() -> Unit)? = null) {
        ChatService.getSomeOneIsTyping{ userTyping, message ->
            if (userTyping != null) {
                if (typingData.updateTyping(userTyping, listBlockedUsers)) {
                    isScrollToBottom = true
                    actionTyping?.also { it() }
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun getMessages(complete: (() -> Unit)? = null) {
        ChatService.getMessages(chatModel){ chat, message ->
            if (chat != null) {
                chatModel = chat
                updateMessages(chatModel?.allMessages)
            }else {
                ToastHelper.showMessage(message)
            }
            complete?.also { it() }
        }
    }

    fun getEventDetails(complete: (() -> Unit)? = null) {
        EventWebService.getEventDetails(eventModel?.id) { event, _ ->
            if (event != null) {
                eventModel = event
            }
            complete?.also { it() }
        }
    }

    fun getChatData() {
        isRefreshAll = true
        FriendWebService.getBlockedUsers(false, 1, 1000){ list, _ ->
            listBlockedUsers.clear()
            list.takeIf { !it.isNullOrEmpty() }?.also {
                listBlockedUsers.addAll(it)
            }
            getEventDetails {
                getMessages()
            }
        }
    }


    private fun updateMessages(list: ArrayList<MessageModel>? = null) {
        ChatService.removeUnsendMessages(chatModel?.id, list)
        ChatService.readAllMessages(chatModel?.id, UserInfo.userId)

        val listNew  = list?.filter { it.chatId == chatModel?.id && !listBlockedUsers.any { blocked -> it.userId == blocked.id } }
        if (isRefreshAll) {
            listMessages.clear()
            isRefreshAll = false
        }

        listNew?.forEach { new ->
            if(!listMessages.any { it.id == new.id }) {
                listMessages.add(new)
            }
        }

        listMessages.sortBy { it.createdAt?.toLong() ?: 0 }

        didLoadData.value = true
        isScrollToBottom = false
    }

    fun sendMessage() {
        val message = prepareMessage() ?: return

        when(message.type) {
             MessageModel.MessageType.TEXT -> {
                 messagePosting.value = ""
                 sendTextMsg(message)
             }
             MessageModel.MessageType.IMAGE, MessageModel.MessageType.VIDEO -> {
                 urlPhotoPosting = null
                 urlVideoPosting = null
                sendMediaMsg(message)
             }
        }
    }

    private fun sendTextMsg(message: MessageModel) {
        isScrollToBottom = true
        ChatService.sendMessage(message)
        didLoadData.value = true

    }

    private fun sendMediaMsg(message: MessageModel) {
        isScrollToBottom = true
        ChatService.sendMediaMsg(message)
        didLoadData.value = true
    }


    private fun prepareMessage(): MessageModel? {
        val model = MessageModel().apply {
            chatId = chatModel?.id
            userId = UserInfo.userId
            sendingAt = Date().time / 1000.toLong()

            type = if(!urlPhotoPosting.isNullOrEmpty()) {
                imageUrl = urlPhotoPosting
                MessageModel.MessageType.IMAGE
            }else if (!urlVideoPosting.isNullOrEmpty()) {
                videoFile = urlVideoPosting
                MessageModel.MessageType.VIDEO
            }else if (!messagePosting.value.isNullOrEmpty()) {
                message = messagePosting.value
                MessageModel.MessageType.TEXT
            }else null
        }

        if (model.type == null) return null

        return model
    }

}