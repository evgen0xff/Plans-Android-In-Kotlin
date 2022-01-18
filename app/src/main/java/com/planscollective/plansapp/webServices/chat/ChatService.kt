package com.planscollective.plansapp.webServices.chat

import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Urls
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.manager.AnalyticsManager
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.*
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.ResponseModel
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URISyntaxException


object ChatService: BaseWebService() {

    private val webService = WebServiceBuilder.buildService(ChatAPIs::class.java)
    private lateinit var socket: Socket

    private var dateLastTyping : Long? = null
    var chatMessagesUnsent: HashMap<String, ArrayList<MessageModel>> = UserInfo.chatMessagesUnsent
    var activeChatId: String? = null

    init {
        try{
            socket = IO.socket(Urls.BASE_URL_CHAT)
        }catch (e : URISyntaxException) {}
    }

    fun initialize(complete: (() -> Unit)? = null) {
        connectSocket(complete)
    }

    fun initializeForLogout() {
        chatMessagesUnsent = HashMap()
        closeChatList()
        closeConnection()
    }

    fun connectSocket(complete: (() -> Unit)? = null) {
        if (!socket.connected()) {
            socket.on(Socket.EVENT_CONNECT) {
                sendUnsentMessages()
                complete?.also { it() }
            }
            socket.connect()
        }else {
            complete?.also { it() }
        }
    }

    fun closeConnection() {
        socket.disconnect()
    }

    fun closeChatList() {
        socket.off("getChatList")
    }

    fun closeOutputList() {
        socket.off("output")
        socket.off("typing")
    }

    fun openOutputList() {
        socket.on("output", listenerGetMessages)
        socket.on("typing", listenerTyping)
    }


    fun addUnsentMessage(message: MessageModel?) {
        val chatId = message?.chatId?.takeIf { it.isNotEmpty() } ?: return

        chatMessagesUnsent[chatId]?.also {
            if (!it.any { unSent ->
                    unSent.chatId == chatId &&
                    unSent.userId == message.userId &&
                    unSent.type == message.type &&
                    unSent.sendingAt?.toLong() == message.sendingAt?.toLong()
            }) {
                it.add(message)
                chatMessagesUnsent[chatId] = it
            }
        } ?: run {
            chatMessagesUnsent[chatId] = arrayListOf(message)
        }
    }

    fun removeUnsendMessages(chatId: String?, messagesSent: ArrayList<MessageModel>?) {
        if (chatId.isNullOrEmpty() || messagesSent.isNullOrEmpty()) return
        chatMessagesUnsent[chatId]?.takeIf { it.isNotEmpty() }?.also {
            messagesSent.forEach { sent ->
                it.indexOfFirst { unSent ->
                    unSent.chatId == sent.chatId &&
                    unSent.userId == sent.userId &&
                    unSent.type == sent.type &&
                    unSent.sendingAt?.toLong() == sent.createdAt?.toLong()
                }.takeIf { index -> index >= 0 && index < it.size }?.also { index ->
                    it.removeAt(index)
                }
            }
            chatMessagesUnsent[chatId] = it
        } ?: return
    }

    //*************************************** Socket-IO Client APIs ****************************//
    fun sendUnsentMessages() {

    }

    fun getChatListInRealTime(
        complete: ((list: ArrayList<ChatModel>?, message: String?) -> Unit)? = null
    ) {
        socket.on("getChatList"){
            (it?.firstOrNull() as? JSONObject)?.takeIf { json -> json["userId"] as? String == UserInfo.userId }
                ?.let { json ->
                    json["listChats"] as? JSONArray
                }?.also { jsonArray ->
                    var list : ArrayList<ChatModel>? = null
                    var message : String? = null
                    try {
                        list = Gson().fromJson<ArrayList<ChatModel>>(jsonArray.toString(), object:TypeToken<ArrayList<ChatModel>>(){}.type)
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (list == null) {
                        message = "Failed to get chat list"
                    }
                    complete?.also{
                        PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                            it(list, message)
                        }
                    }
                }
        }

        val json = JSONObject()
        json.put("userId", UserInfo.userId)
        socket.emit("getChatList", json)
    }


    fun muteNotification(
        chatId: String?,
        isMute: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        val chat_id = chatId.takeIf { !it.isNullOrEmpty() } ?: run {
            complete?.also { it(false, "Invalid Chat Id") }
            return
        }

        val json = JSONObject()
        json.put("chatId", chat_id)
        json.put("userId", UserInfo.userId)
        json.put("status", if(isMute) 1 else 0)

        socket.emit("muteNotification", json, Ack{
            var success = false
            var message: String? = null

            (it?.firstOrNull() as? JSONObject)?.also { json ->
                try {
                    success = json.getBoolean("success")
                    message = json.getString("message")
                }catch (e : Exception) {}
            }

            complete?.also {
                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                    it(success, message)
                }
            }
        })
    }

    fun updateChatHidden(
        chatId: String?,
        isHidden: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        val chat_id = chatId.takeIf { !it.isNullOrEmpty() } ?: run {
            complete?.also { it(false, "Invalid Chat Id") }
            return
        }

        val json = JSONObject()
        json.put("chatId", chat_id)
        json.put("userId", UserInfo.userId)
        json.put("isHidden", isHidden)

        socket.emit("updateShowStatus", json, Ack{
            var success = false
            var message: String? = null

            (it?.firstOrNull() as? JSONObject)?.also { json ->
                try {
                    success = json.getBoolean("success")
                    message = json.getString("message")
                }catch (e : Exception) {}
            }

            complete?.also {
                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                    it(success, message)
                }
            }
        })
    }

    private var actionGetMessages : ((chat: ChatModel?, message: String?) -> Unit)? = null
    private val listenerGetMessages = Emitter.Listener {
        (it?.firstOrNull() as? JSONObject)?.also { json ->
                var chatModel : ChatModel? = null
                var message : String? = null
                try {
                    chatModel = Gson().fromJson<ChatModel>(json.toString(), object:TypeToken<ChatModel>(){}.type)
                }catch (e: Exception) {
                    e.printStackTrace()
                }
                if (chatModel == null) {
                    message = "Failed to get the messages"
                }
                actionGetMessages?.also{
                    PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                        if (activeChatId == null || activeChatId == chatModel?.id){
                            it(chatModel, message)
                        }
                    }
                }
            }
    }

    fun getMessages(
        chatModel: ChatModel? = null,
        chatId: String? = null,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ) {
        val chat = chatModel ?: (if (!chatId.isNullOrEmpty()) ChatModel(id = chatId) else null) ?: run {
            complete?.also { it(null, "Invalid Params") }
            return
        }

        actionGetMessages = complete
        socket.on("output", listenerGetMessages)

        val json = JSONObject()
        chat.id?.takeIf { it.isNotEmpty() }?.also {
            json.put("chatId", it)
        }
        chat.eventId?.takeIf { it.isNotEmpty() }?.also {
            json.put("eventId", it)
        }
        chat.members?.takeIf { it.isNotEmpty() }?.also {
            json.put("memberIds", JSONArray(it.map { member -> member.id }))
        }
        UserInfo.userId?.takeIf { it.isNotEmpty() }?.also {
            json.put("userId", it)
        }

        socket.emit("getMessages", json)
    }

    private var actionTyping : ((userTyping: TypingUserModel?, message: String?) -> Unit)? = null
    private val listenerTyping = Emitter.Listener {
        (it?.firstOrNull() as? JSONObject)?.also { json ->
            var userTyping : TypingUserModel? = null
            var message : String? = null
            try {
                userTyping = Gson().fromJson<TypingUserModel>(json.toString(), object:TypeToken<TypingUserModel>(){}.type)
            }catch (e: Exception) {
                e.printStackTrace()
            }
            if (userTyping == null) {
                message = "Failed to check if someone is typing"
            }
            actionTyping?.also{
                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                    if (activeChatId == null || activeChatId == userTyping?.chatId){
                        it(userTyping, message)
                    }
                }
            }
        }
    }

    fun getSomeOneIsTyping(
        complete: ((userTyping: TypingUserModel?, message: String?) -> Unit)? = null
    ) {
        actionTyping = complete
        socket.on("typing", listenerTyping)
    }

    var isPosting = false
    fun readAllMessages(chatId: String?, userId: String?) {
        val chat_id = chatId.takeIf { !it.isNullOrEmpty() } ?: return
        val user_id = userId.takeIf { !it.isNullOrEmpty() } ?: return

        if (isPosting) return

        val json = JSONObject()
        json.put("chatId", chat_id)
        json.put("userId", user_id)

        isPosting = true
        socket.emit("viewerId", json, Ack{
            isPosting = false
        })
    }

    fun joinChat(chatId: String?, isJoin: Boolean = true) {
        isPosting = false
        val joinedChatId = chatId ?: activeChatId

        if (!chatId.isNullOrEmpty()) {
            activeChatId = if (isJoin) chatId else null
        }

        if (joinedChatId.isNullOrEmpty() || UserInfo.userId.isNullOrEmpty()) return

        val json = JSONObject()
        json.put("chatId", joinedChatId)
        json.put("userId", UserInfo.userId)
        json.put("isJoin", isJoin)

        socket.emit("joinChat", json)
    }

    fun sendMessage(
        message: MessageModel?,
        isAddUnsent: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        if (message == null) {
            complete?.also { it(false, "Invalid Params") }
            return
        }

        if (isAddUnsent)
            addUnsentMessage(message)

        val json = JSONObject().apply {
            message.chatId?.takeIf { it.isNotEmpty() }?.also { put("chatId", it) }
            message.userId?.takeIf { it.isNotEmpty() }?.also { put("userId", it) }
            message.type?.value?.takeIf { it.isNotEmpty() }?.also { put("type", it) }
            message.message?.takeIf { it.isNotEmpty() }?.also { put("message", it) }
            message.imageUrl?.takeIf { it.isNotEmpty() }?.also { put("imageUrl", it) }
            message.videoFile?.takeIf { it.isNotEmpty() }?.also { put("videoFile", it) }
            message.sendingAt?.also { put("createdAt", it) }
        }

        socket.emit("input", json, Ack {
            message.createdAt = message.sendingAt
            removeUnsendMessages(message.chatId, arrayListOf(message))
            AnalyticsManager.logEvent(AnalyticsManager.EventType.CHAT_MESSAGE)
            complete?.also {
                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.Main) {
                    it(true, null)
                }
            }
        })
    }

    fun sendMediaMsg(
        message: MessageModel?,
        isAddUnsent: Boolean = true,
        complete: ((success: Boolean, message: String?) -> Unit)? = null
    ){
        if (message == null) {
            complete?.also { it(false, "Invalid Params") }
            return
        }

        if (isAddUnsent)
            addUnsentMessage(message)

        uploadMedia(message){ response, msg ->
            if (response != null) {
                response.imageUrl?.takeIf { it.isNotEmpty() }?.also { message.imageUrl = it }
                response.videoUrl?.takeIf { it.isNotEmpty() }?.also { message.videoFile = it }
                sendMessage(message, false, complete)
            }else {
                complete?.also { it(false, msg) }
            }
        }
    }


    fun updateIsTyping(chatId: String?, isTyping: Boolean = false) {
        val chat_id = chatId?.takeIf { it.isNotEmpty() } ?: return
        val userName = UserInfo.fullName?.takeIf { it.isNotEmpty() } ?: return
        val userId = UserInfo.userId?.takeIf { it.isNotEmpty() } ?: return
        val profileImage = UserInfo.profileUrl

        val json = JSONObject().apply {
            put("chatId", chat_id)
            put("userName", userName)
            put("profileImage", profileImage)
            put("userId", userId)
            put("isTyping", isTyping)
        }

        socket.emit("typing", json)

        if (isTyping) {
            dateLastTyping = System.currentTimeMillis()
            PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.IO){
                delay(5000)
                if (dateLastTyping != null && dateLastTyping!! <= (System.currentTimeMillis() - 5000)) {
                    updateIsTyping(chatId, false)
                }
            }
        }
    }

    fun appWillTerminate() {
        UserInfo.chatMessagesUnsent = chatMessagesUnsent
        updateIsTyping(activeChatId, false)
        joinChat(activeChatId, false)
    }

    //*************************************** Web APIs *****************************************//

    fun getChatDetails(
        chatId: String?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getChatDetails(UserInfo.accessToken, chatId)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get the chat details")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the chat details")
                }
            }
        })

    }

    fun updateGroupChatName(
        nameGroup: String?,
        chatId: String?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.updateGroupChatName(UserInfo.accessToken, chatId, nameGroup)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to update the group chat name")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to update the group chat name")
                }
            }
        })

    }

    fun getChatList(complete: ((list: ArrayList<ChatModel>?, message: String?) -> Unit)? = null) {

        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.getChatList(UserInfo.accessToken, UserInfo.userId)?.enqueue(object:
            Callback<ResponseModel<ArrayList<ChatModel>>> {
            override fun onResponse(
                call: Call<ResponseModel<ArrayList<ChatModel>>>,
                response: Response<ResponseModel<ArrayList<ChatModel>>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to get the chat list")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ArrayList<ChatModel>>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to get the chat list")
                }
            }
        })
    }

    fun removeUserInChat(
        userId: String?,
        chatId: String?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        webService.removeUserInChat(UserInfo.accessToken, userId, chatId)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to remove the user in the chat")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to remove the user in the chat")
                }
            }
        })
    }

    fun createChatGroup(
        members: ArrayList<UserModel>?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (members.isNullOrEmpty()){
            complete?.also { it(null, "Invalid params") }
            return
        }

        val chatInfo = ChatModel().apply {
            organizer = ChatUserModel().apply { id = UserInfo.userId }
            this.members = members.map {
                ChatUserModel().apply {
                    id = it.id
                    firstName = it.firstName
                    lastName = it.lastName
                    email = it.email
                    mobile = it.mobile
                    profileImage = it.profileImage
                }
            }.toArrayList()
        }

        webService.createChatGroup(UserInfo.accessToken, chatInfo)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to create the chat group")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to create the chat group")
                }
            }
        })
    }


    fun updateChatGroup(
        chatId: String?,
        members: ArrayList<UserModel>?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (members.isNullOrEmpty() || chatId.isNullOrEmpty()){
            complete?.also { it(null, "Invalid params") }
            return
        }

        val chatInfo = ChatModel(id = chatId).apply {
            organizer = ChatUserModel().apply { id = UserInfo.userId }
            this.members = members.filter { it.id != UserInfo.userId }.map {
                ChatUserModel().apply {
                    id = it.id
                    firstName = it.firstName
                    lastName = it.lastName
                    email = it.email
                    mobile = it.mobile
                    profileImage = it.profileImage
                }
            }.toArrayList()
        }

        webService.updateChatGroup(UserInfo.accessToken, chatInfo)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to update the chat group")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to update the chat group")
                }
            }
        })
    }

    fun assignAdminForGroupChat(
        chatId: String?,
        adminId: String?,
        complete: ((chat: ChatModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (adminId.isNullOrEmpty() || chatId.isNullOrEmpty()){
            complete?.also { it(null, "Invalid params") }
            return
        }

        webService.assignAdminForGroupChat(UserInfo.accessToken, chatId, adminId)?.enqueue(object:
            Callback<ResponseModel<ChatModel>> {
            override fun onResponse(
                call: Call<ResponseModel<ChatModel>>,
                response: Response<ResponseModel<ChatModel>>
            ) {
                complete?.let{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to assign the chat admin")
                    }
                }

            }

            override fun onFailure(
                call: Call<ResponseModel<ChatModel>>,
                t: Throwable
            ) {
                if (complete != null) {
                    complete(null, "Failed to assign the chat admin")
                }
            }
        })
    }

    private fun uploadMedia(
        message: MessageModel?,
        complete: ((response: ChatResponseModel?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val accessToken = UserInfo.accessToken ?: run {
            complete?.also{ it(null, "Invalid access token") }
            return
        }

        val fileInfo: MutableMap<String, RequestBody?> = HashMap()
        UserInfo.userId?.also{
            fileInfo["userId"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        message?.type?.value?.also{
            fileInfo["mediaType"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        var fileBody : MultipartBody.Part? = null
        message?.imageUrl?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("imageOrVideo", file.name, requestFile)
            }
        }

        message?.videoFile?.takeIf { it.isNotEmpty() }?.also {
            val file = File(it)
            if (file.exists()) {
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                fileBody = MultipartBody.Part.createFormData("imageOrVideo", file.name, requestFile)
            }
        }

        webService.uploadMedia(accessToken, fileInfo, fileBody)?.enqueue(object:Callback<ResponseModel<ChatResponseModel>>{
            override fun onFailure(call: Call<ResponseModel<ChatResponseModel>>, t: Throwable) {
                complete?.also{ it(null, "Failed to update the media") }
            }
            override fun onResponse(
                call: Call<ResponseModel<ChatResponseModel>>,
                response: Response<ResponseModel<ChatResponseModel>>
            ) {
                complete?.also{
                    if (response.isSuccessful){
                        val result = response.body()
                        if (result?.error != null) {
                            it(null, handleError(result.error))
                        }else {
                            it(result?.response, null)
                        }
                    }else {
                        it(null, "Failed to update the media")
                    }
                }
            }
        })
    }


}