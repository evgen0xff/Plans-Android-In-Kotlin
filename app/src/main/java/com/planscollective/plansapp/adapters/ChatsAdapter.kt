package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.planscollective.plansapp.databinding.CellChatListItemBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.MessageModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class ChatsAdapter : PlansRecyclerViewAdapter<ChatModel>() {

    private var listener: PlansActionListener? = null
    private var listChats = ArrayList<ChatModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(listChats: ArrayList<ChatModel>? = null,
                      listener: PlansActionListener? = null
    ) {
        listChats?.also {
            this.listChats.clear()
            this.listChats.addAll(it)
        }
        listener?.also {
            this.listener = it
        }

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ChatModel> {
        val itemBinding = CellChatListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListItemVH(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ChatModel>, position: Int) {
        val item = listChats[position]
        when(holder) {
            is ChatListItemVH -> {
                (holder as? ChatListItemVH)?.bind(item, isLast = position == (listChats.size - 1), data = position == 0)
            }
        }
    }

    override fun getItemCount(): Int {
        return listChats.size
    }

    fun updateGuideChatView() {
        listChats.takeIf { it.isNotEmpty() }?.also {
            notifyItemChanged(0)
        }
    }

    //***************************** Chat List ViewHolder **************************************//
    inner class ChatListItemVH(var itemBinding: CellChatListItemBinding): BaseViewHolder<ChatModel>(itemBinding.root) {
        private var chatModel: ChatModel? = null
        private var isFirst = false

        init {
            itemView.setOnSingleClickListener {
                listener?.onClickChatListItem(chatModel)
            }
            itemView.setOnLongClickListener {
                UserInfo.isSeenGuideTapHoldChat = true
                updateGuideChatView()
                listener?.onLongClickChatListItem(chatModel)
                true
            }
            itemBinding.apply {
                layoutUsers.setOnSingleClickListener {
                    if (chatModel?.isEventChat == true) {
                        chatModel?.event?.apply {
                            val access = if (isActive == false) false
                            else if (chatModel?.organizer?.id == UserInfo.userId) true
                            else if (isCancel == true) false
                            else isExpired != true

                            if (access) {
                                listener?.onClickedEvent(this)
                            }
                        }
                    }else {
                        listener?.onClickedUser(chatModel?.profileUser)
                    }
                }
            }
        }

        override fun bind(item: ChatModel?, data: Any?, isLast: Boolean) {
            chatModel = item
            isFirst = (data as? Boolean) ?: false

            itemBinding.apply {
                // Bottom Separator
                separatorBottom.visibility = if (isLast) View.GONE else View.VISIBLE

                // Users profile Images
                imvUser1.setUserImage(chatModel?.profileImage)
                layoutUser2.visibility = if (chatModel?.profileNextUser == null) View.GONE else View.VISIBLE
                imvUser2.setUserImage(chatModel?.profileNextUser?.profileImage)

                // Chat Title
                tvName.text = chatModel?.titleChat

                // Event Status
                chatModel?.takeIf { it.isEventChat }?.event?.getEventStatus()?.also {
                    imvMarkLive.visibility = if (it.third == EventModel.EventStatus.LIVED) {
                        tvLive.visibility = View.GONE
                        View.VISIBLE
                    } else {
                        tvLive.visibility = View.VISIBLE
                        View.GONE
                    }
                    tvDot.visibility = View.VISIBLE
                    tvLive.text = it.first
                    tvLive.setTextColor(it.second)
                } ?: run{
                    tvDot.visibility = View.GONE
                    imvMarkLive.visibility = View.GONE
                    tvLive.visibility = View.GONE
                }

                // Last Message Ago Time
                tvTime.visibility = if (chatModel?.lastMessageTime == null) View.GONE else {
                    tvTime.text = chatModel?.lastMessageTime?.toLocalDateTime()?.timeAgoSince()
                    View.VISIBLE
                }

                // Last Message
                val userName = if (chatModel?.lastMessage?.user?.id == UserInfo.userId) "You: " else if (chatModel?.isGroup == true) {
                    "${chatModel?.lastMessage?.user?.firstName ?: ""}: "
                }else null

                var html = if (!userName.isNullOrEmpty()) "<b>$userName</b>" else ""

                chatModel?.lastMessage?.also {
                    if (it.type == MessageModel.MessageType.TEXT && !it.message.isNullOrEmpty()) {
                        html += it.message
                        layoutVideo.visibility = View.GONE
                        layoutPhoto.visibility = View.GONE
                    }else if (it.type == MessageModel.MessageType.VIDEO) {
                        layoutVideo.visibility = View.VISIBLE
                        layoutPhoto.visibility = View.GONE
                    }else if (it.type == MessageModel.MessageType.IMAGE) {
                        layoutPhoto.visibility = View.VISIBLE
                        layoutVideo.visibility = View.GONE
                    }
                } ?: run{
                    layoutVideo.visibility = View.GONE
                    layoutPhoto.visibility = View.GONE
                }

                tvLastMessage.visibility = if (html.isEmpty()) View.GONE else {
                    tvLastMessage.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                    else
                        Html.fromHtml(html)
                    View.VISIBLE
                }

                // Unread Message count
                tvUnreadCount.apply {
                    visibility = if (chatModel?.unreadMessages.isNullOrEmpty()) View.GONE else {
                        text = "${chatModel?.unreadMessages?.size}"
                        View.VISIBLE
                    }
                }

                // Mute Mark
                imvMute.visibility = if (chatModel?.isMuteNotification == true) View.VISIBLE else View.GONE

                // Guild View
                imvGuideTapHoldChat.visibility = if (isFirst && !UserInfo.isSeenGuideTapHoldChat) View.VISIBLE else View.GONE

                adjustLayouts()
            }
        }

        private fun adjustLayouts() {

            itemBinding.apply {
                val spaceUsedBase = 16 * 2 + 44 + 8 + (if (layoutUser2.visibility == View.VISIBLE) 25 else 0)

                // Adjust Chat Name textview
                var spaceUsed = spaceUsedBase

                if (tvTime.visibility == View.VISIBLE)
                    spaceUsed += tvTime.getTextWidth().toDp() + 10

                if (tvLive.visibility == View.VISIBLE)
                    spaceUsed += tvLive.getTextWidth().toDp() + 4

                if (imvMarkLive.visibility == View.VISIBLE)
                    spaceUsed += 40 + 4

                if (tvDot.visibility == View.VISIBLE)
                    spaceUsed += tvDot.getTextWidth().toDp() + 4

                tvName.maxWidth = OSHelper.widthScreen - spaceUsed.toPx()

                // Adjust Last Message textview
                spaceUsed = spaceUsedBase

                if (tvUnreadCount.visibility == View.VISIBLE)
                    spaceUsed += tvUnreadCount.getTextWidth().toDp() + 8 + 12

                if (imvMute.visibility == View.VISIBLE)
                    spaceUsed += 16 + 8

                if (layoutPhoto.visibility == View.VISIBLE)
                    spaceUsed += 16 + 4 + tvPhoto.getTextWidth().toDp()

                if (layoutVideo.visibility == View.VISIBLE)
                    spaceUsed += 16 + 4 + tvVideo.getTextWidth().toDp()

                if (tvLastMessage.visibility == View.VISIBLE)
                    tvLastMessage.maxWidth = OSHelper.widthScreen - spaceUsed.toPx()
            }
        }

    }
}