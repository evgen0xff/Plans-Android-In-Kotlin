package com.planscollective.plansapp.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.MutableLiveData
import com.planscollective.plansapp.extension.formatPhoneNumber
import com.planscollective.plansapp.models.dataModels.UserModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object ContactsHelper {
    enum class StatusType {
        NONE,
        LOADING,
        LOADED
    }

    private var listContacts: ArrayList<UserModel>? = null
    private var statusLoading = MutableLiveData<StatusType>()
    private var didLoaded: ((ArrayList<UserModel>?, String?) -> Unit)? = null

    init {
        statusLoading.value = StatusType.NONE
        statusLoading.observeForever {
            if (it == StatusType.LOADED) {
                GlobalScope.launch(Dispatchers.Main){
                    didLoaded?.also { it(listContacts, null) }
                }
            }
        }
    }

    private val PROJECTION_CONTACTS = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    )
    private const val SELECTION_CONTACTS: String = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    private const val SORT_ORDER_CONTACTS: String = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC"


    private val PROJECTION_CONTACT_DETAILS = arrayOf(
        ContactsContract.Data._ID,
        ContactsContract.Data.MIMETYPE,
        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY,
        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
        ContactsContract.CommonDataKinds.Phone.TYPE,
        ContactsContract.CommonDataKinds.Phone.DATA,
        ContactsContract.CommonDataKinds.Phone.LABEL,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Email.ADDRESS
    )
    private const val SELECTION_CONTACT_DETAILS_BY_KEY: String = "${ContactsContract.Data.LOOKUP_KEY} = ?"
    private const val SELECTION_CONTACT_DETAILS_BY_CONTACT_ID: String = "${ContactsContract.Data.CONTACT_ID} = ?"
    private const val SORT_ORDER_CONTACT_DETAILS: String = ContactsContract.Data.MIMETYPE

    @SuppressLint("Recycle")
    fun getContacts(
        context: Context?,
        keyword: String? = null,
        isRefresh: Boolean = false,
        complete: ((list: ArrayList<UserModel>?, message: String?) -> Unit)? = null
    ) {
        val contentResolver = context?.contentResolver ?: run {
            complete?.also{it(null, "Invalid params")}
            return
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            complete?.also{it(null, "Permission denied to read the contacts.")}
            return
        }

        didLoaded = complete

        if (!isRefresh && statusLoading.value == StatusType.LOADED) {
            statusLoading.value = StatusType.LOADED
            return
        }

        if (statusLoading.value == StatusType.LOADING){
            return
        }

        statusLoading.value = StatusType.LOADING

        var selectionArgs : Array<String>? = null
        var selection: String? = null

        keyword?.takeIf { it.isNotEmpty() }?.also {
            selectionArgs =  arrayOf("%$it%")
        }

        selectionArgs?.takeIf { it.isNotEmpty() }?.also {
            selection = SELECTION_CONTACTS
        }

        GlobalScope.launch (Dispatchers.IO){
            val list = arrayListOf<UserModel>()
            try {
                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    PROJECTION_CONTACTS,
                    selection,
                    selectionArgs,
                    SORT_ORDER_CONTACTS
                )
                cursor?.takeIf { it.count > 0 }?.also {
                    it.moveToFirst()
                    do {
                        val contactId = it.getLongOrNull(it.getColumnIndex(ContactsContract.Contacts._ID))
                        val contactKey = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                        getContactDetails(context, contactId, contactKey)?.also{ user ->
                            list.add(user)
                        }
                    }while (it.moveToNext())
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }

            listContacts = arrayListOf()
            listContacts?.addAll(list)
            withContext(Dispatchers.Main) {
                statusLoading.value = StatusType.LOADED
            }
        }
    }

    @SuppressLint("Recycle")
    fun getContactDetails(context: Context?, contactId: Long? = null, contactKey: String? = null) : UserModel? {
        val contentResolver = context?.contentResolver ?: return null
        val selectionArgs = arrayOf("")

        val selection = if (!contactKey.isNullOrEmpty()) {
            selectionArgs[0] = contactKey
            SELECTION_CONTACT_DETAILS_BY_KEY
        } else if (contactId != null) {
            selectionArgs[0] = "$contactId"
            SELECTION_CONTACT_DETAILS_BY_CONTACT_ID
        }else return null

        var user: UserModel? = null
        val mobiles = HashMap<Int?, String?>()

        try{
            val cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                PROJECTION_CONTACT_DETAILS,
                selection,
                selectionArgs,
                SORT_ORDER_CONTACT_DETAILS
            )

            cursor?.takeIf { it.count > 0 }?.also {
                user = UserModel()
                it.moveToFirst()
                do {
                    val mineType = it.getStringOrNull(it.getColumnIndex(ContactsContract.Data.MIMETYPE))
                    when(mineType) {
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                            user?.fullName = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY))
                            user?.name = user?.fullName
                            user?.firstName = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                            user?.lastName = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                        }
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                            val phoneTypeInt = it.getIntOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                            mobiles[phoneTypeInt] = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                ?.formatPhoneNumber(context, numberFormat = PhoneNumberUtil.PhoneNumberFormat.E164)
                        }
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                            user?.email = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        }
                        else -> {}
                    }
                }while (it.moveToNext())
            }
        }catch (e : Exception) {
            e.printStackTrace()
        }

        user?.mobile = if (mobiles.containsKey(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)) {
            mobiles[ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE]
        }else if (mobiles.containsKey(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)) {
            mobiles[ContactsContract.CommonDataKinds.Phone.TYPE_HOME]
        }else if (mobiles.containsKey(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)) {
            mobiles[ContactsContract.CommonDataKinds.Phone.TYPE_WORK]
        }else if (mobiles.containsKey(null)) {
            mobiles[null]
        }else null

        return user
    }

}