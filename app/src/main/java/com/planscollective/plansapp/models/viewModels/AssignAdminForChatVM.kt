package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.chat.ChatService

class AssignAdminForChatVM(application: Application) : ListBaseVM(application) {

    var chatId: String? = null
    var chat: ChatModel? = null
    var listMembers = ArrayList<UserModel>()
    var listSelected = ArrayList<UserModel>()

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int
    ) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }
        ChatService.getChatDetails(chatId) { chatModel, message ->
            BusyHelper.hide()
            if (chatModel != null) {
                updateData(chatModel)
            }else {
                ToastHelper.showMessage(message)
            }
        }

    }

    private fun updateData(chatModel: ChatModel?) {
        chat = chatModel
        listMembers.clear()
        chat?.members?.filter{ it.id != UserInfo.userId}?.takeIf { it.isNotEmpty() }?.also {
            keywordSearch.trim().lowercase().takeIf{ it.isNotEmpty()}?.also { search ->
                listMembers.addAll(it.filter{ member -> (member.fullName ?: member.name ?: "${member.firstName ?: ""} ${member.lastName ?: ""}").lowercase().contains(search)})
            } ?: run {
                listMembers.addAll(it)
            }
        }

        didLoadData.value = true
    }

    fun selectUser(user: UserModel?) {
        val selected = user ?: return
        if (listSelected.any { it.id == selected.id }) {
            listSelected.removeAll { it.id == selected.id }
        }else {
            listSelected.clear()
            listSelected.add(selected)
        }

        didLoadData.value = true
    }

}