package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.chat.ChatService

class ChatsVM(application: Application) : ListBaseVM(application) {

    override var numberOfRows: Int = 20
    var listChats = ArrayList<ChatModel>()
    var listSearched = ArrayList<ChatModel>()

    //********************************* Chat List *********************************************//
    fun updateChatList(list: ArrayList<ChatModel>?, pageNumber: Int = 1, count: Int = 20) {
        listChats.clear()

        list?.takeIf{it.isNotEmpty()}?.also {
            listChats.addAll(it)
        }
        searchChatList()
    }

    fun searchChatList() {
        listSearched.clear()
        keywordSearch.trim().lowercase().takeIf { it.isNotEmpty() }?.also { keyword ->
            listSearched = listChats.filter { it.titleChat?.lowercase()?.contains(keyword) == true}.toArrayList()
        } ?: run {
            listSearched.addAll(listChats)
        }

        didLoadData.value = true
    }

    fun getChatList(isShownLoading: Boolean,
                    pageNumber: Int,
                    count: Int) {
        if (isShownLoading)
            BusyHelper.show(context)

        ChatService.getChatList { list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateChatList(list, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //********************************* Common Methods *****************************************//

    override fun getList(isShownLoading: Boolean,
                     pageNumber: Int,
                     count: Int
    ) {
        getChatList(isShownLoading, pageNumber, count)
    }

    override fun getNextPage(context: Context,
                         isShownLoading: Boolean){
        getList(isShownLoading, pageNumber, numberOfRows)
    }

}