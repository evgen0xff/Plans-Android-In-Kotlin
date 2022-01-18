package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ContactsHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.event.EventWebService
import com.planscollective.plansapp.webServices.friend.FriendWebService
import com.planscollective.plansapp.webServices.user.UserWebService
import com.redmadrobot.inputmask.MaskedTextChangedListener

class EditInvitationVM(application: Application) : ListBaseVM(application) {

    enum class EditMode {
        CREATE,
        EDIT
    }

    class UserStatus (
        var isSelected: Boolean? = null,
        var isGrayed: Boolean = false,
        var isCrossed: Boolean = false,
        var isTrash: Boolean = false
    )

    var editMode = EditMode.EDIT
    var eventId: String? = null
    var usersSelected: ArrayList<InvitationModel>? = null
    var userActive: InvitationModel? = null

    var eventModel: EventModel? = null

    var listUsers = ArrayList<InvitationModel>()
    val listStatus : ArrayList<UserStatus>
        get() = listUsers.map {
            val status = UserStatus()
            status.isSelected = getIsSelected(it)
            when(selectedType.value) {
                InvitationModel.InviteType.FRIEND -> {
                    if (editMode != EditMode.CREATE) {
                        status.isGrayed = getIsSelected(it, false)
                        status.isCrossed = status.isGrayed
                        if (status.isGrayed){
                            status.isSelected = null
                        }
                    }
                }
                InvitationModel.InviteType.CONTACT -> {
                    status.isTrash = it.typeInvitation == InvitationModel.InviteType.MOBILE
                    if (editMode != EditMode.CREATE) {
                        status.isGrayed = getIsSelected(it, false)
                        if (status.isGrayed) {
                            status.isSelected = null
                            status.isCrossed = true
                            status.isTrash = false
                        }
                    }
                }
                InvitationModel.InviteType.EMAIL -> {
                    status.isTrash = true
                    if (editMode != EditMode.CREATE) {
                        status.isGrayed = getIsSelected(it, false)
                        if (status.isGrayed) {
                            status.isSelected = null
                            status.isCrossed = true
                            status.isTrash = false
                        }
                    }
                }
                else -> {}
            }
            status
        }.toArrayList()

    var selectedType = MutableLiveData<InvitationModel.InviteType>()
    val invitationTypes : ArrayList<InvitationModel.InviteType>
        get() {
            return when (editMode) {
                EditMode.CREATE -> {
                    arrayListOf(InvitationModel.InviteType.FRIEND,
                        InvitationModel.InviteType.CONTACT,
                        InvitationModel.InviteType.EMAIL)
                }
                EditMode.EDIT -> {
                    arrayListOf(InvitationModel.InviteType.FRIEND,
                        InvitationModel.InviteType.CONTACT,
                        InvitationModel.InviteType.EMAIL,
                        InvitationModel.InviteType.LINK)
                }
            }
        }

    val selectedTabIndex : Int
        get() = selectedType.value?.let { invitationTypes.indexOf(it) } ?: 0

    val listTabItems : ArrayList<String>
        get() = invitationTypes.map { it.title ?: "" }.toArrayList()

    // Friend List
    var listFriends = ArrayList<InvitationModel>()
    var selectedFriendsAlready = ArrayList<InvitationModel>()
    var selectedFriendsNew = ArrayList<InvitationModel>()

    // Contact lIst
    var nameCountryCode = "US"
    var phoneCode = "+1"
    var phoneFormatted = ""
    var phoneExtracted = ""
    var lengthMin = 10
    var listContacts = ArrayList<InvitationModel>()
    var listContactsOrigin: ArrayList<UserModel>? = null
    var selectedContactsAlready = ArrayList<InvitationModel>()
    var selectedContactsNew = ArrayList<InvitationModel>()
    var enableAccessForContacts = true

    // Email List
    @Bindable
    var emailAdding = MutableLiveData<String>()
    var listEmails = ArrayList<InvitationModel>()
    var selectedEmailsAlready = ArrayList<InvitationModel>()
    var selectedEmailsNew = ArrayList<InvitationModel>()


    fun initializeData(editMode : String = EditMode.EDIT.name,
                       eventId: String? = null,
                       usersSelected: ArrayList<InvitationModel>? = null,
                       actionEnterKey: ((view: View) -> Unit)? = null) {

        if (selectedType.value != null) return

        this.editMode = EditMode.valueOf(editMode)
        this.eventId = eventId
        this.usersSelected = usersSelected
        this.actionEnterKey = actionEnterKey

        // Selected Friends
        usersSelected?.filter{it.isFriend == true}?.also{ selectedFriendsAlready.addAll(it) }

        // Selected Contacts
        usersSelected?.filter{it.typeInvitation == InvitationModel.InviteType.MOBILE || it.typeInvitation == InvitationModel.InviteType.CONTACT }
            ?.also{ selectedContactsAlready.addAll(it) }

        // Selected Emails
        usersSelected?.filter{it.typeInvitation == InvitationModel.InviteType.EMAIL}?.also{ selectedEmailsAlready.addAll(it) }

        // Sync Mobile and Emails with Shared Preference
        val mobileList = UserInfo.mobileList
        selectedContactsAlready.forEach { contact ->
            mobileList.removeAll { it == contact.mobile }
            mobileList.add(contact.mobile ?: "")
        }
        UserInfo.mobileList = mobileList

        val emailList = UserInfo.emailList
        selectedEmailsAlready.forEach { email ->
            emailList.removeAll{ it.lowercase() == email.email?.lowercase()}
            emailList.add(email.email ?: "")
        }
        UserInfo.emailList = emailList
    }

    //****************************************** Common methods *********************************//
    private fun getIsSelected(user: InvitationModel?, isNew: Boolean? = null) : Boolean {
        return when(selectedType.value) {
            InvitationModel.InviteType.FRIEND -> {
                getSelectedStatus(user, selectedFriendsNew, selectedFriendsAlready, isNew)
            }
            InvitationModel.InviteType.CONTACT -> {
                getSelectedStatus(user, selectedContactsNew, selectedContactsAlready, isNew)
            }
            InvitationModel.InviteType.EMAIL -> {
                getSelectedStatus(user, selectedEmailsNew, selectedEmailsAlready, isNew, InvitationModel.InviteType.EMAIL)
            }
            else -> { false }
        }
    }

    private  fun getSelectedStatus(user: InvitationModel?,
                                   listNew: ArrayList<InvitationModel>?,
                                   listAlready: ArrayList<InvitationModel>?,
                                   isNew: Boolean? = null,
                                   userType: InvitationModel.InviteType = InvitationModel.InviteType.MOBILE
    ) : Boolean {
        var result = false
        val new = listNew?.any { it ->
            when (userType) {
                InvitationModel.InviteType.FRIEND, InvitationModel.InviteType.PLANS_USER -> {
                    it.id == user?.id
                }
                InvitationModel.InviteType.MOBILE -> {
                    it.mobile == user?.mobile
                }
                InvitationModel.InviteType.EMAIL -> {
                    it.email?.lowercase() == user?.email?.lowercase()
                }
                else -> { false }
            }
        } ?: false

        val alreay = listAlready?.any{
            when (userType) {
                InvitationModel.InviteType.FRIEND, InvitationModel.InviteType.PLANS_USER -> {
                    it.id == user?.id
                }
                InvitationModel.InviteType.MOBILE -> {
                    it.mobile == user?.mobile
                }
                InvitationModel.InviteType.EMAIL -> {
                    it.email?.lowercase() == user?.email?.lowercase()
                }
                else -> { false }
            }
        } ?: false

        result = if (isNew != null) {
            if (isNew) new else alreay
        }else {
            new || alreay
        }
        return result
    }

    fun cancelUser(user: InvitationModel?, complete: ((toPosition: Int?) -> Unit)? = null) {
        userActive = user

        if (user == null) {
            complete?.also { it(null) }
            return
        }else {
            when(selectedType.value) {
                InvitationModel.InviteType.FRIEND -> {
                    usersSelected?.removeAll{it.id == user.id}
                }
                InvitationModel.InviteType.CONTACT -> {
                    usersSelected?.removeAll{it.mobile == user.mobile}
                }
                InvitationModel.InviteType.EMAIL -> {
                    usersSelected?.removeAll{it.email?.lowercase() == user.email?.lowercase()}
                }
                else -> {}
            }
            val toPosition = unselectUser(user)
            complete?.also { it(toPosition) }
        }
    }

    fun selectUser(item: InvitationModel?): Int? {
        var toPosition: Int? = null
        val user = item ?: return toPosition

        userActive = user

        if (user.friendShipStatus == 1 && !selectedFriendsNew.any { it.id == user.id }) {
            selectedFriendsNew.add(user)
        }

        toPosition = when (selectedType.value) {
            InvitationModel.InviteType.FRIEND -> {
                reloadFriendList()
            }
            InvitationModel.InviteType.CONTACT -> {
                selectedContactsNew.add(user)
                reloadContactList()
            }
            InvitationModel.InviteType.EMAIL -> {
                selectedEmailsNew.add(user)
                reloadEmailList()
            }
            else -> { null }
        }

        return toPosition
    }

    fun unselectUser(user: InvitationModel?): Int? {
        var toPosition: Int? = null
        val item = user ?: return toPosition

        userActive = item

        if (item.friendShipStatus == 1) {
            selectedFriendsAlready.removeAll{it.id == item.id}
            selectedFriendsNew.removeAll{it.id == item.id}

            selectedContactsAlready.removeAll{it.mobile == item.mobile}
            selectedContactsNew.removeAll{it.mobile == item.mobile}

            selectedEmailsAlready.removeAll{it.email?.lowercase() == item.email?.lowercase()}
            selectedEmailsNew.removeAll{it.email?.lowercase() == item.email?.lowercase()}
        }

        toPosition = when(selectedType.value) {
            InvitationModel.InviteType.FRIEND -> {
                selectedFriendsAlready.removeAll { it.id == item.id}
                selectedFriendsNew.removeAll{ it.id == item.id}
                reloadFriendList()
            }
            InvitationModel.InviteType.CONTACT -> {
                selectedContactsAlready.removeAll { it.mobile == item.mobile}
                selectedContactsNew.removeAll{ it.mobile == item.mobile}
                reloadContactList()
            }
            InvitationModel.InviteType.EMAIL -> {
                selectedEmailsAlready.removeAll { it.email?.lowercase() == item.email?.lowercase()}
                selectedEmailsNew.removeAll{ it.email?.lowercase() == item.email?.lowercase()}
                reloadEmailList()
            }
            else -> { null }
        }

        return toPosition
    }

    fun getSelectedCounts(isRemoved: Boolean = false) : Triple<Int, Int, Int> {
        val friends = ArrayList<InvitationModel>()
        friends.addAll(selectedFriendsNew)
        selectedContactsNew.forEach{ contact ->
            if (contact.friendShipStatus == 1 && !friends.any{it.id == contact.id}) {
                friends.add(contact)
            }
        }
        selectedEmailsNew.forEach { email ->
            if (email.friendShipStatus == 1 && !friends.any{it.id == email.id}) {
                friends.add(email)
            }
        }

        var countFriends = friends.size
        var countContacts = selectedContactsNew.filter{it.friendShipStatus != 1}.size
        var countEmails = selectedEmailsNew.filter{it.friendShipStatus != 1}.size
        var countRemovedFriends = 0
        var countRemovedContacts = 0
        var countRemovedEmails = 0

        val friendsList = listFriends.takeIf{it.isNotEmpty()} ?: arrayListOf<InvitationModel>().apply { addAll(selectedFriendsAlready) }
        val contactsList = listContacts.takeIf{it.isNotEmpty()} ?: arrayListOf<InvitationModel>().apply { addAll(selectedContactsAlready) }
        val emailsList = listEmails.takeIf{it.isNotEmpty()} ?: arrayListOf<InvitationModel>().apply { addAll(selectedEmailsAlready) }

        val friendsAlready = friendsList.filter{ friend -> selectedFriendsAlready.any{it.id == friend.id}}
        val contactsAlready = contactsList.filter{ contact -> selectedContactsAlready.any{it.mobile == contact.mobile}}
        val emailsAlready = emailsList.filter{ email -> selectedEmailsAlready.any{it.email?.lowercase() == email.email?.lowercase()}}

        when (editMode) {
            EditMode.CREATE -> {
                friendsAlready.forEach { friend ->
                    if (friend.friendShipStatus == 1 && !friends.any{it.id == friend.id}){
                        friends.add(friend)
                    }
                }
                contactsAlready.forEach { contact ->
                    if (contact.friendShipStatus == 1 && !friends.any{it.mobile == contact.mobile}){
                        friends.add(contact)
                    }
                }
                emailsAlready.forEach { email ->
                    if (email.friendShipStatus == 1 && !friends.any{it.email?.lowercase() == email.email?.lowercase()}){
                        friends.add(email)
                    }
                }
                countFriends = friends.size
                countContacts += contactsAlready.filter { it.friendShipStatus != 1 }.size
                countEmails += emailsAlready.filter{ it.friendShipStatus != 1}.size
            }
            EditMode.EDIT -> {
                val oriFriends = friendsList.filter{ friend -> usersSelected?.any{it.mobile == friend.mobile || it.email?.lowercase() == friend.email?.lowercase()} ?: false}
                val oriContacts = contactsList.filter{ contact -> contact.friendShipStatus != 1 && (usersSelected?.any { it.mobile == contact.mobile && it.typeInvitation == InvitationModel.InviteType.MOBILE } ?: false)}
                val oriEmails = emailsList.filter{ email -> email.friendShipStatus != 1 && (usersSelected?.any{it.email?.lowercase() == email.email?.lowercase() && it.typeInvitation == InvitationModel.InviteType.EMAIL} ?: false)}

                countRemovedFriends = oriFriends.size - friendsAlready.size - contactsAlready.filter{it.friendShipStatus == 1}.size - emailsAlready.filter{it.friendShipStatus == 1}.size
                countRemovedContacts = oriContacts.size - contactsAlready.filter{it.friendShipStatus != 1}.size
                countRemovedEmails = oriEmails.size - emailsAlready.filter{it.friendShipStatus != 1}.size
            }
        }

        return if (isRemoved) Triple(countRemovedFriends, countRemovedContacts, countRemovedEmails) else Triple(countFriends, countContacts, countEmails)
    }

    fun getSelectedUsers() : ArrayList<InvitationModel> {
        val result = ArrayList<InvitationModel>()
        val friends = ArrayList<InvitationModel>()
        val people = ArrayList<InvitationModel>()

        // Friends
        selectedFriendsAlready.forEach { user ->
            listFriends.firstOrNull { it.id == user.id }?.let {
                it.typeInvitation = InvitationModel.InviteType.FRIEND
                friends.add(it)
            }
        }
        selectedFriendsNew.forEach { user ->
            listFriends.firstOrNull { it.id == user.id }?.let {
                it.typeInvitation = InvitationModel.InviteType.FRIEND
                friends.add(it)
            }
        }

        // Contacts
        people.addAll(selectedContactsAlready.map{
            it.typeInvitation = InvitationModel.InviteType.MOBILE
            it
        })
        people.addAll(selectedContactsNew.map{
            it.typeInvitation = InvitationModel.InviteType.MOBILE
            it
        })

        // Emails
        people.addAll(selectedEmailsAlready.map{
            it.typeInvitation = InvitationModel.InviteType.EMAIL
            it
        })
        people.addAll(selectedEmailsNew.map{
            it.typeInvitation = InvitationModel.InviteType.EMAIL
            it
        })

        people.forEach { item ->
            if (item.friendShipStatus == 1) {
                if (!friends.any{it.mobile == item.mobile || it.email?.lowercase() == item.email?.lowercase()}) {
                    item.typeInvitation = InvitationModel.InviteType.FRIEND
                    friends.add(item)
                }
            }
        }

        result.addAll(friends)
        result.addAll(people)
        return result
    }

    fun updateInvitations(
        list: ArrayList<InvitationModel>?,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        val event_id = eventId ?: return
        BusyHelper.show(context)
        EventWebService.updateInvitations(event_id, list){
            success, message ->
            BusyHelper.hide()
            complete?.let{ it(success, message)}
        }
    }

    //****************************************** Friend List *************************************//
    private fun getFriendList(isShownLoading: Boolean = true) {
        if (isShownLoading)
            BusyHelper.show(context)

        FriendWebService.getFriendList(1, 1000){
            list, message ->
            BusyHelper.hide()
            if (list != null) {
                updateFriendList(list)
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun updateFriendList(list: ArrayList<InvitationModel>?) {
        list?.let{
            listFriends.clear()
            listFriends.addAll(it)
        } ?: return

        reloadFriendList()
    }


    private fun reloadFriendList(): Int? {
        listFriends.sortWith(compareByDescending<InvitationModel>{ selectedFriendsAlready.any { it1 -> it.id == it1.id }}
            .thenByDescending { selectedFriendsNew.any { it1 -> it.id == it1.id } }
            .thenBy{ it.fullName ?: it.name ?: "${it.firstName ?: ""} ${it.lastName ?: ""}"}
        )
        return filterFriendsBy(keywordSearch)
    }

    private fun filterFriendsBy(text: String? = null): Int? {
        listUsers.clear()
        keywordSearch.trim().takeIf{ it.isNotEmpty()}?.let {
            listUsers.addAll(listFriends.filter{ it1 -> (it1.fullName ?: it1.name ?: "${it1.firstName ?: ""} ${it1.lastName ?: ""}").lowercase().contains(it.lowercase())})
        } ?: run {
            listUsers.addAll(listFriends)
        }
        didLoadData.value = true
        return listUsers.indexOfFirst { it.id == userActive?.id }.takeIf { it >= 0 }
    }


    //****************************************** Email List *************************************//
    private fun getEmailList() {
        listEmails.clear()
        UserInfo.emailList.map {
            val item = InvitationModel()
            item.email = it
            item.typeInvitation = InvitationModel.InviteType.EMAIL
            item
        }.also {
            listEmails.addAll(it)
        }
        getUsersWithEmails()
    }

    private fun getUsersWithEmails() {
        val emails = listEmails.map { it.email }.takeIf { it.isNotEmpty() } ?: run {
            updateEmailList()
            return
        }

        BusyHelper.show(context)
        UserWebService.getUsersWithEmails(emails){
                list, message ->
            BusyHelper.hide()
            if (message != null) {
                ToastHelper.showMessage(message)
            }else {
                updateEmailList(list)
            }
        }
    }

    private fun updateEmailList(list: ArrayList<UserModel>? = null) {
        list?.forEach { user ->
            listEmails.indexOfFirst { it.email?.lowercase() == user.email?.lowercase() }.takeIf { it >= 0 }?.also {
                if (user.id.isNullOrEmpty()) {
                    listEmails[it].invitedTime = user.invitedTime
                }else {
                    val newEmail = InvitationModel(user).apply { typeInvitation = listEmails[it].typeInvitation }
                    listEmails[it] = newEmail
                }
            }
        }
        reloadEmailList()
    }

    private fun reloadEmailList(): Int? {
        listEmails.sortWith(compareByDescending<InvitationModel>{ selectedEmailsAlready.any { it1 -> it.email?.lowercase() == it1.email?.lowercase() }}
            .thenByDescending { selectedEmailsNew.any { it1 -> it.email?.lowercase() == it1.email?.lowercase() } }
            .thenByDescending { !it.id.isNullOrEmpty() }
            .thenBy{ it.fullName ?: it.name ?: "${it.firstName ?: ""} ${it.lastName ?: ""}"}
        )

        return filterEmailsBy(keywordSearch)
    }

    private fun filterEmailsBy(text: String? = null): Int? {
        listUsers.clear()
        keywordSearch.trim().takeIf{ it.isNotEmpty()}?.also {
            listUsers.addAll(
                listEmails.filter{ it1 ->
                    (it1.email?.lowercase()?.contains(it.lowercase()) ?: false) ||
                    (it1.fullName ?: it1.name ?: "${it1.firstName ?: ""} ${it1.lastName ?: ""}").lowercase().contains(it.lowercase())
                }
            )
        } ?: run {
            listUsers.addAll(listEmails)
        }
        didLoadData.value = true
        return listUsers.indexOfFirst { it.email == userActive?.email }.takeIf { it >= 0 }
    }

    fun deleteEmail(item: InvitationModel?) {
        val email = item?.email?.lowercase() ?: return
        UserInfo.emailList?.toArrayList()?.let {
            it.removeAll{it1 -> it1.lowercase() == email}
            UserInfo.emailList = it
        }

        selectedEmailsAlready.removeAll{it.email?.lowercase() == email}
        selectedEmailsNew.removeAll{it.email?.lowercase() == email}
        listEmails.removeAll{it.email?.lowercase() == email}

        reloadEmailList()
    }

    fun addEmail() {
        val email = emailAdding.value?.trim()?.takeIf { it.isNotEmpty() } ?: return
        if (email.lowercase() == UserInfo.email?.lowercase()) {
            ToastHelper.showMessage("This email address is yours, it can't be added here.")
        }else if (listEmails.any{it.email?.lowercase() == email.lowercase()}) {
            ToastHelper.showMessage("This email address have already been added.")
        }else {
            val emails = UserInfo.emailList.apply {
                removeAll { it.lowercase() == email.lowercase() }
                add(email)
            }
            UserInfo.emailList = emails
            emailAdding.value = ""
            notifyChange(BR.emailAdding)

            getEmailList()
        }
    }

    //****************************************** Contact List *************************************//
    private fun getContactList() {
        listContacts.clear()
        UserInfo.mobileList.map {
            val item = InvitationModel()
            item.mobile = it
            item.typeInvitation = InvitationModel.InviteType.MOBILE
            item
        }.let {
            listContacts.addAll(it)
        }

        val action = { list: ArrayList<UserModel>? ->
            if (!list.isNullOrEmpty()) {
                list.forEach { it1 ->
                    if (it1.mobile != UserInfo.mobile) {
                        val userInvite = InvitationModel(it1)
                        userInvite.typeInvitation = InvitationModel.InviteType.CONTACT
                        listContacts.removeAll{ it2 -> it2.mobile == it1.mobile }
                        listContacts.add(userInvite)
                    }
                }
            }
            getUsersWithMobiles()
        }

        if (listContactsOrigin == null) {
            BusyHelper.show(context)
            ContactsHelper.getContacts(context){ list, message ->
                BusyHelper.hide()
                list.takeIf{ !it.isNullOrEmpty()}?.also{
                    listContactsOrigin = arrayListOf()
                    listContactsOrigin?.addAll(it)
                }
                action(listContactsOrigin)
            }
        }else {
            action(listContactsOrigin)
        }

    }

    private fun getUsersWithMobiles(){
        val mobiles = listContacts.map { it.mobile }.takeIf { it.isNotEmpty() } ?: run {
            updateContacts()
            return
        }
        BusyHelper.show(context)
        UserWebService.getUsersWithMobiles(mobiles){
            list, message ->
            BusyHelper.hide()
            if (message != null) {
                ToastHelper.showMessage(message)
            }else {
                updateContacts(list)
            }
        }
    }

    private fun updateContacts(list: ArrayList<UserModel>? = null) {
        list?.forEach { user ->
            listContacts.indexOfFirst { it.mobile == user.mobile }.takeIf { it >= 0 }?.also {
                if (user.id.isNullOrEmpty()) {
                    listContacts[it].invitedTime = user.invitedTime
                }else {
                    val newContact = InvitationModel(user).apply { typeInvitation = listContacts[it].typeInvitation }
                    listContacts[it] = newContact
                }
            }
        }
        reloadContactList()
    }

    private fun reloadContactList(): Int? {
        listContacts.sortWith(compareByDescending<InvitationModel>{ selectedContactsAlready.any { it1 -> it.mobile == it1.mobile }}
            .thenByDescending { selectedContactsNew.any { it1 -> it.mobile == it1.mobile } }
            .thenByDescending { it.typeInvitation == InvitationModel.InviteType.MOBILE }
            .thenByDescending { !it.id.isNullOrEmpty() }
            .thenBy{ it.fullName ?: it.name ?: "${it.firstName ?: ""} ${it.lastName ?: ""}"}
        )

        return filterContactsBy(keywordSearch)
    }

    private fun filterContactsBy(text: String? = null): Int? {
        listUsers.clear()
        keywordSearch.trim().takeIf{ it.isNotEmpty()}?.also {
            listUsers.addAll(
                listContacts.filter{ it1 ->
                    (it1.mobile?.lowercase()?.contains(it.lowercase()) ?: false) ||
                    (it1.fullName ?: it1.name ?: "${it1.firstName ?: ""} ${it1.lastName ?: ""}").lowercase().contains(it.lowercase())
                }
            )
        } ?: run {
            listUsers.addAll(listContacts)
        }
        didLoadData.value = true

        return listUsers.indexOfFirst { it.mobile == userActive?.mobile }.takeIf { it >= 0 }
    }

    fun deleteContact(item: InvitationModel?) {
        val contact = item ?: return
        val mobile = contact.mobile ?: return

        UserInfo.mobileList?.toArrayList()?.let {
            it.removeAll{ it1 -> it1 == mobile}
            UserInfo.mobileList = it
        }

        selectedContactsAlready.removeAll{it.mobile == mobile}
        selectedContactsNew.removeAll{it.mobile == mobile}
        listContacts.removeAll{it.mobile == mobile}

        reloadContactList()
    }

    fun addPhoneNumber(editText: EditText? = null) {
        val mobile = phoneCode + phoneExtracted
        if (mobile == UserInfo.mobile) {
            ToastHelper.showMessage("This phone number is yours, it can't be added here.")
        }else if (listContacts.any { it.mobile == mobile }) {
            ToastHelper.showMessage("This phone number has already been added.")
        }else {
            val mobiles = UserInfo.mobileList
            mobiles.removeAll{it == mobile}
            mobiles.add(mobile)
            UserInfo.mobileList = mobiles
            phoneExtracted = ""
            phoneFormatted = ""
            editText?.setText("")
            getContactList()
        }
    }

    //******************************************** Share Link *********************************//
    private fun getEvent() {
        val event_id = eventId.takeIf { !it.isNullOrEmpty() } ?: return

        BusyHelper.show(context)
        EventWebService.getEventDetails(event_id) {
            event, message ->
            BusyHelper.hide()
            if (event != null) {
                eventModel = event
                didLoadData.value = true
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    //********************************************** All List ***********************************//
    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int

    ) {
        when (selectedType.value) {
            InvitationModel.InviteType.FRIEND -> {
                getFriendList(isShownLoading)
            }
            InvitationModel.InviteType.CONTACT -> {
                getContactList()
            }
            InvitationModel.InviteType.EMAIL -> {
                getEmailList()
            }
            InvitationModel.InviteType.LINK -> {
                getEvent()
            }
            else -> {}
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
        pageNumber = (listUsers.size / numberOfRows) + 1
        getList(isShownLoading, pageNumber, numberOfRows)
    }

    fun searchUsers(text: String? = null) {
        when (selectedType.value) {
            InvitationModel.InviteType.FRIEND -> {
                filterFriendsBy(text)
            }
            InvitationModel.InviteType.CONTACT -> {
                filterContactsBy(text)
            }
            InvitationModel.InviteType.EMAIL -> {
                filterEmailsBy(text)
            }
            else -> {}
        }
    }


}