package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.extension.replace
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ContactsHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.user.UserWebService

class AddFriendsVM(application: Application) : ListBaseVM(application) {

    enum class UserType {
        PLANS_USERS,
        CONTACTS
    }

    var userId: String? = null
    var user: UserModel? = null
    var listUsers = ArrayList<UserModel>()
    var listContacts = ArrayList<UserModel>()
    var typeSelected = MutableLiveData<UserType>()
    var enableAccessForContacts = true
    var listMobiles: ArrayList<UserModel>? = null
    var listContactsOrigin: ArrayList<UserModel>? = null


    override var numberOfRows : Int = 20


    //*********************************** Plans Users ******************************************//
    private fun updatePlansUserList(list: ArrayList<UserModel>?, pageNo: Int = 1, count: Int = 20) {
        if (pageNo == 1) {
            listUsers.clear()
        }
        listUsers.replace(list, pageNo, count)
        didLoadData.value = true
    }

    private fun getAllPlansUserList(isShownLoading: Boolean = true,
                                    pageNumber: Int = 1,
                                    count: Int = 20) {
        if (keywordSearch.isBlank()) {
            listUsers.clear()
            didLoadData.value = true
            return
        }

        if (isShownLoading) {
            BusyHelper.show(context)
        }

        UserWebService.getPlansUserList(keywordSearch, pageNumber, count) { list, message ->
            BusyHelper.hide()
            if (list != null) {
                updatePlansUserList(list, pageNumber, count)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //*********************************** Phone Contacts ******************************************//
    private fun filterContacts() {
        listUsers.clear()
        val list = listMobiles

        // Filter non friends
        val listNonFriends = list?.filter { it.friendShipStatus != 1 && it.mobile != UserInfo.mobile }

        // Remove contacts who has a plan account from listContacts
        val newContactsInvite = ArrayList<UserModel>()
        listNonFriends?.forEach { user ->
            if (!user.id.isNullOrEmpty()) {
                listUsers.add(user)
            }else {
                listContactsOrigin.takeIf{ !it.isNullOrEmpty() }?.also { contacts ->
                    for (contact in contacts) {
                        if (contact.mobile == user.mobile) {
                            contact.invitedTime = user.invitedTime
                            newContactsInvite.add(contact)
                            break
                        }
                    }
                }
            }
        }
        listContacts = newContactsInvite

        // Filter plans users and contacts with search text
        if (keywordSearch.isNotBlank()) {
            listUsers = listUsers.filter {
                val name = (it.name ?: it.fullName ?: "${it.firstName ?: ""} ${it.lastName ?: ""}").lowercase()
                name.contains(keywordSearch.lowercase()) || it.mobile?.contains(keywordSearch.lowercase()) == true
            }.toArrayList()

            listContacts = listContacts.filter {
                val name = (it.name ?: it.fullName ?: "${it.firstName ?: ""} ${it.lastName ?: ""}").lowercase()
                name.contains(keywordSearch.lowercase()) || it.mobile?.contains(keywordSearch.lowercase()) == true
            }.toArrayList()
        }

        // Sort Plans Users by FriendShipStatus
        listUsers.sortBy { user ->
            var order  = (user.friendShipStatus ?: -1).toDouble()
            if (order == 0.0) {
                if (user.friendRequestSender == UserInfo.userId) {
                    order = 0.5
                }
            }
            order
        }

        // Sort Contacts by invitedTime
        listContacts.sortByDescending { it.invitedTime?.toLong() }

        didLoadData.value = true
    }


    private fun getUsersWithMobiles(isShownLoading: Boolean = true){
        val mobiles = listContactsOrigin?.map { it.mobile }?.takeIf { it.isNotEmpty() } ?: run {
            didLoadData.value = true
            return
        }

        if (isShownLoading) {
            BusyHelper.show(context)
        }

        UserWebService.getUsersWithMobiles(mobiles){ list, message ->
            BusyHelper.hide()
            if (message != null) {
                ToastHelper.showMessage(message)
            }else {
                listMobiles = list
                filterContacts()
            }
        }
    }

    private fun getContactList(isShownLoading: Boolean = true) {
        if (isShownLoading) {
            BusyHelper.show(context)
        }

        if (listContactsOrigin == null) {
            ContactsHelper.getContacts(context){ list, message ->
                BusyHelper.hide()
                if (list != null) {
                    listContactsOrigin = arrayListOf()
                    listContactsOrigin?.addAll(list)
                    getUsersWithMobiles(isShownLoading)
                }else {
                    ToastHelper.showMessage(message)
                    didLoadData.value = true
                }
            }
        }else {
            getUsersWithMobiles(isShownLoading)
        }

    }

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int
    ) {
        listContacts.clear()

        when (typeSelected.value) {
            UserType.PLANS_USERS -> {
                getAllPlansUserList(isShownLoading, pageNumber, count)
            }
            UserType.CONTACTS -> {
                getContactList(isShownLoading)
            }
        }

    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUsers.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    fun searchUsers() {
        when (typeSelected.value) {
            UserType.PLANS_USERS -> {
                getAllList(false)
            }
            UserType.CONTACTS -> {
                filterContacts()
            }
        }
    }



}