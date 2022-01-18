package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.chat.ChatService
import com.planscollective.plansapp.webServices.event.EventWebService

class ChatSettingsVM(application: Application) : PlansBaseVM(application) {

    var chatId: String? = null
    var chat: ChatModel? = null
    var event: EventModel? = null

    var listPeople = ArrayList<UserModel>()

    fun getChatDetails() {
        BusyHelper.show(context)
        ChatService.getChatDetails(chatId) { chatModel, message ->
            BusyHelper.hide()
            if (chatModel != null) {
                updateData(chatModel)
                if (chat?.isEventChat == true) {
                    getEventDetails(chat?.event?.id)
                }else {
                    didLoadData.value = true
                }
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun getEventDetails(eventId: String?) {
        EventWebService.getEventDetails(eventId) { eventModel, message ->
            if (event != null) {
                event = eventModel
                chat?.event = event
                didLoadData.value = true
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    fun updateGroupName(name: String?) {
        val nameGroup = name ?: return
        val chat_id = chat?.id ?: chatId ?: return

        BusyHelper.show(context)
        ChatService.updateGroupChatName(nameGroup, chat_id){ chatModel, message ->
            BusyHelper.hide()
            if (chatModel != null) {
                updateData(chatModel)
                didLoadData.value = true
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun updateData(chatModel: ChatModel?) {
        chat = chatModel
        listPeople.clear()
        chat?.people?.also {
            listPeople.addAll(it)
        }
    }



}