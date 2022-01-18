package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.*
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.MessageModel
import com.planscollective.plansapp.models.dataModels.MessageShapeModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.webServices.chat.ChatService

class ChattingAdapter : RecyclerView.Adapter<BaseViewHolder<MessageModel>>() {

    private var chat: ChatModel? = null
    private var listener: PlansActionListener? = null
    var listItems = ArrayList<MessageModel>()

    enum class ViewType {
        MY_TEXT_MSG,
        MY_MEDIA_MSG,
        OTHERS_TEXT_MSG,
        OTHERS_MEDIA_MSG,
        DATE_MSG
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(chatModel: ChatModel? = null, messages: ArrayList<MessageModel>? = null, listener: PlansActionListener? = null) {
        chatModel?.also { chat = it }
        listener?.also { this.listener = it }

        // 1. Add Date Separator item
        var textDate = ""
        listItems.clear()
        messages?.forEach { msg ->
            msg.createdAt?.also {
                val strDate = it.toLocalDateTime().stringDate()
                if (strDate != textDate) {
                    textDate = strDate
                    val msgDate = MessageModel().apply {
                        type = MessageModel.MessageType.DATE
                        createdAt = it
                        message = textDate
                    }
                    listItems.add(msgDate)
                }
            }
            listItems.add(msg)
        }

        // 2. Add Unsent Messages
        if (!chat?.id.isNullOrEmpty()) {
            ChatService.chatMessagesUnsent[chat!!.id!!]?.takeIf { it.isNotEmpty() }?.also {
                listItems.addAll(it)
            }
        }

        // 3. Update Message ViewModel for each item.
        listItems.forEachIndexed { index, item ->
            val shape = MessageShapeModel().apply {
                // Owner Type
                ownerType = if (item.userId == UserInfo.userId)
                    MessageShapeModel.OwnerType.MINE
                else if (item.type == MessageModel.MessageType.DATE)
                    MessageShapeModel.OwnerType.DATE
                else MessageShapeModel.OwnerType.OTHER

                // Position Type
                val preIndex = index - 1
                var isExistPreItem = false
                var isExistNextItem = false

                if (preIndex >= 0 &&
                    preIndex < listItems.size &&
                    listItems[preIndex].type != MessageModel.MessageType.DATE &&
                    listItems[preIndex].userId == item.userId ) {
                    isExistPreItem = true
                }

                val nextIndex = index + 1
                if (nextIndex >= 0 &&
                    nextIndex < listItems.size &&
                    listItems[nextIndex].type != MessageModel.MessageType.DATE &&
                    listItems[nextIndex].userId == item.userId ) {
                    isExistNextItem = true
                }

                positionType = if (isExistPreItem && isExistNextItem)
                    MessageShapeModel.PositionType.MEDIUM
                else if (isExistPreItem)
                    MessageShapeModel.PositionType.END
                else if (isExistNextItem)
                    MessageShapeModel.PositionType.START
                else MessageShapeModel.PositionType.NORMAL
            }

            item.shapeModel = shape
        }

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<MessageModel> {
        return when(viewType) {
            ViewType.MY_TEXT_MSG.ordinal -> {
                val itemBinding = CellMyTextMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MyTextMsgVH(itemBinding)
            }
            ViewType.MY_MEDIA_MSG.ordinal -> {
                val itemBinding = CellMyMediaMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MyMediaMsgVH(itemBinding)
            }
            ViewType.OTHERS_TEXT_MSG.ordinal -> {
                val itemBinding = CellOthersTextMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OthersTextMsgVH(itemBinding)
            }
            ViewType.OTHERS_MEDIA_MSG.ordinal -> {
                val itemBinding = CellOthersMediaMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OthersMediaMsgVH(itemBinding)
            }
            ViewType.DATE_MSG.ordinal -> {
                val itemBinding = CellDateMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateMsgVH(itemBinding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<MessageModel>, position: Int) {
        holder.bind(listItems[position])
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = listItems[position]
        return when(message.shapeModel?.ownerType) {
            MessageShapeModel.OwnerType.MINE -> {
                if (message.type == MessageModel.MessageType.TEXT) ViewType.MY_TEXT_MSG.ordinal else ViewType.MY_MEDIA_MSG.ordinal
            }
            MessageShapeModel.OwnerType.OTHER -> {
                if (message.type == MessageModel.MessageType.TEXT) ViewType.OTHERS_TEXT_MSG.ordinal else ViewType.OTHERS_MEDIA_MSG.ordinal
            }
            MessageShapeModel.OwnerType.DATE -> {
                ViewType.DATE_MSG.ordinal
            }
            else -> ViewType.DATE_MSG.ordinal
        }
    }

    inner class MyTextMsgVH(var itemBinding: CellMyTextMsgBinding) : BaseViewHolder<MessageModel>(itemBinding.root) {
        private var message: MessageModel? = null
        override fun bind(item: MessageModel?, data: Any?, isLast: Boolean) {
            message = item
            itemBinding.apply {
                var maxWidth = OSHelper.widthScreen - (67 + 16 + 16 + 8 + 5 + 5).toPx()
                // CreateAt Text and Sending Mark
                tvCreatedAt.visibility = if (message?.createdAt == null) {
                    imvSending.visibility = View.VISIBLE
                    View.GONE
                } else {
                    tvCreatedAt.text = message?.createdAt?.toLocalDateTime()?.toFormatString("h:mm a")
                    imvSending.visibility = View.GONE
                    maxWidth -= tvCreatedAt.getTextWidth()
                    View.VISIBLE
                }

                // Message Text
                tvMessage.maxWidth = maxWidth
                tvMessage.text = message?.message

                // Background
                val ignores = message?.shapeModel?.cornersNonRounding
                val backgroundId = if (ignores.isNullOrEmpty())
                    R.drawable.bkgnd_teal_corner20
                else if (ignores.size == 1) {
                    if (ignores.firstOrNull() == MessageShapeModel.CornerType.TOP_RIGHT)
                        R.drawable.bkgnd_teal_corner20_ignore_top_right
                    else
                        R.drawable.bkgnd_teal_corner20_ignore_bottom_right
                }else  R.drawable.bkgnd_teal_corner20_ignore_right
                layoutBackground.setBackgroundResource(backgroundId)
            }
        }
    }

    inner class MyMediaMsgVH(var itemBinding: CellMyMediaMsgBinding) : BaseViewHolder<MessageModel>(itemBinding.root) {
        private var message: MessageModel? = null
        init {
            itemBinding.layoutBackground.setOnSingleClickListener {
                if (message?.type == MessageModel.MessageType.IMAGE) {
                    listener?.onClickOpenPhoto(message?.imageUrl)
                }else if (message?.type == MessageModel.MessageType.VIDEO) {
                    listener?.onClickPlayVideo(message?.videoFile)
                }
            }
        }

        override fun bind(item: MessageModel?, data: Any?, isLast: Boolean) {
            message = item
            itemBinding.apply {
                // CreateAt, Loading Spinner, Sending Mark
                layoutCreateAt.visibility = if (!message?.id.isNullOrEmpty()) {
                    spinner.isIndeterminate = false
                    spinner.visibility = View.GONE
                    imvSending.visibility = View.GONE
                    tvCreateAt.text = message?.createdAt?.toLocalDateTime()?.toFormatString("h:mm a")
                    View.VISIBLE
                }else {
                    spinner.isIndeterminate = true
                    spinner.visibility = View.VISIBLE
                    imvSending.visibility = View.VISIBLE
                    View.GONE
                }

                // Media Content
                videoView.visibility = if (message?.videoFile.isNullOrEmpty()) {
                    imvCoverImage.setEventImage(message?.imageUrl)
                    View.GONE
                }else {
                    videoView.playVideoUrl(message?.videoFile, message?.imageUrl)
                    View.VISIBLE
                }
            }
        }
    }

    inner class OthersTextMsgVH(var itemBinding: CellOthersTextMsgBinding): BaseViewHolder<MessageModel>(itemBinding.root) {
        private var message: MessageModel? = null
        init {
            itemBinding.apply {
                imvUser.setOnSingleClickListener {
                    listener?.onClickedUser(message?.user)
                }
            }
        }
        override fun bind(item: MessageModel?, data: Any?, isLast: Boolean) {
            message = item
            itemBinding.apply {
                var maxWidth = OSHelper.widthScreen - (16 + 62 + 40 + 8 + 16 + 16).toPx()
                // User Profile Image
                imvUser.visibility = if (message?.shapeModel?.isHiddenProfileImage == true) View.INVISIBLE else {
                    imvUser.setUserImage(message?.user?.profileImage)
                    View.VISIBLE
                }

                // CreateAt
                tvCreateAt.text = message?.createdAt?.toLocalDateTime()?.toFormatString("h:mm a")
                var widthHeader = tvCreateAt.getTextWidth() + 5.toPx()

                // User Name and organizer mark
                tvUserName.visibility = if (message?.shapeModel?.isHiddenOwnerName == true) View.GONE else {
                    val name = message?.user?.name ?: message?.user?.fullName ?: "${message?.user?.firstName ?: ""} ${message?.user?.lastName ?: ""}"
                    val spannable = if (message?.userId == chat?.organizer?.id && chat?.isEventChat == true)
                        getOrganizerMark(name)
                    else
                        SpannableStringBuilder(name)

                    tvUserName.text = spannable
                    widthHeader += tvUserName.getEditableTextWidth(spannable)
                    View.VISIBLE
                }

                // Message
                tvMessage.text = message?.message
                val widthMessage = tvMessage.getTextWidth()

                if (widthMessage < maxWidth && widthHeader < maxWidth) {
                    if (widthMessage < widthHeader) {
                        layoutHeader.setLayoutWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                        tvMessage.setLayoutWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                    }else {
                        layoutHeader.setLayoutWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                        tvMessage.setLayoutWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                }else {
                    layoutHeader.setLayoutWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                    tvMessage.setLayoutWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                }


                // Background
                val ignores = message?.shapeModel?.cornersNonRounding
                val backgroundId = if (ignores.isNullOrEmpty())
                    R.drawable.bkgnd_white_corner20
                else if (ignores.size == 1) {
                    if (ignores.firstOrNull() == MessageShapeModel.CornerType.TOP_LEFT)
                        R.drawable.bkgnd_white_corner20_ignore_top_left
                    else
                        R.drawable.bkgnd_white_corner20_ignore_bottom_left
                }else  R.drawable.bkgnd_white_corner20_ignore_left
                layoutBackground.setBackgroundResource(backgroundId)
            }
        }

        private fun getOrganizerMark(text: String?) : SpannableStringBuilder? {
            var name = text
            var spannable: SpannableStringBuilder?
            itemBinding.apply {
                val widthAvailable = OSHelper.widthScreen - (16 + 40 + 8 + 16 + 4 + 16 + 62).toPx() - tvCreateAt.getTextWidth()
                val widthName = tvUserName.getTextWidth(name)
                val widthNameWithMark = widthName + tvUserName.getTextWidth(" • ") + tvCreateAt.getTextWidth("Organizer")
                name += if (widthAvailable in widthName until widthNameWithMark) "\n" else " • "
                spannable = SpannableStringBuilder(name)
                val start = spannable?.length ?: 0
                spannable?.append("Organizer", AbsoluteSizeSpan(13, true), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable?.setSpan(ForegroundColorSpan(PlansColor.TEAL_MAIN), start, spannable?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val regularFont = Typeface.create(ResourcesCompat.getFont(itemView.context, R.font.product_sans_regular), Typeface.NORMAL)
                spannable?.setSpan(StyleSpan(regularFont.style), start, spannable?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannable
        }
    }

    inner class OthersMediaMsgVH(var itemBinding: CellOthersMediaMsgBinding): BaseViewHolder<MessageModel>(itemBinding.root) {

        private var message: MessageModel? = null
        init {
            itemBinding.apply {
                imvUser.setOnSingleClickListener {
                    listener?.onClickedUser(message?.user)
                }

                layoutMedia.setOnSingleClickListener {
                    if (message?.type == MessageModel.MessageType.IMAGE) {
                        listener?.onClickOpenPhoto(message?.imageUrl)
                    }else if (message?.type == MessageModel.MessageType.VIDEO) {
                        listener?.onClickPlayVideo(message?.videoFile)
                    }
                }
            }

        }

        override fun bind(item: MessageModel?, data: Any?, isLast: Boolean) {
            message = item
            itemBinding.apply {
                // User Profile Image
                imvUser.visibility = if (message?.shapeModel?.isHiddenProfileImage == true) View.INVISIBLE else {
                    imvUser.setUserImage(message?.user?.profileImage)
                    View.VISIBLE
                }

                // Header
                layoutHeader.visibility = when(message?.shapeModel?.positionType) {
                    MessageShapeModel.PositionType.NORMAL, MessageShapeModel.PositionType.START  -> {
                        // CreatedAt Time
                        layoutCreateAtBottom.visibility = View.GONE
                        tvCreateAtTop.text = message?.createdAt?.toLocalDateTime()?.toFormatString("h:mm a")

                        // User Name and organizer mark
                        tvUserName.visibility = if (message?.shapeModel?.isHiddenOwnerName == true) View.GONE else {
                            val name = message?.user?.name ?: message?.user?.fullName ?: "${message?.user?.firstName ?: ""} ${message?.user?.lastName ?: ""}"
                            val spannable = if (message?.userId == chat?.organizer?.id && chat?.isEventChat == true)
                                getOrganizerMark(name)
                            else
                                SpannableStringBuilder(name)

                            tvUserName.text = spannable
                            View.VISIBLE
                        }

                        View.VISIBLE
                    }
                    else -> {
                        layoutCreateAtBottom.visibility = View.VISIBLE
                        tvCreateAtBottom.text = message?.createdAt?.toLocalDateTime()?.toFormatString("h:mm a")
                        View.GONE
                    }
                }

                // Media Content
                videoView.visibility = if (message?.videoFile.isNullOrEmpty()) {
                    imvCoverImage.setEventImage(message?.imageUrl)
                    View.GONE
                }else {
                    videoView.playVideoUrl(message?.videoFile)
                    View.VISIBLE
                }
            }
        }

        private fun getOrganizerMark(text: String?) : SpannableStringBuilder? {
            var name = text
            var spannable: SpannableStringBuilder?
            itemBinding.apply {
                val widthAvailable = OSHelper.widthScreen - (16 + 40 + 8 + 16 + 4 + 16 + 62).toPx() - tvCreateAtTop.getTextWidth()
                val widthName = tvUserName.getTextWidth(name)
                val widthNameWithMark = widthName + tvUserName.getTextWidth(" • ") + tvCreateAtTop.getTextWidth("Organizer")
                name += if (widthAvailable in widthName until widthNameWithMark) "\n" else " • "
                spannable = SpannableStringBuilder(name)
                val start = spannable?.length ?: 0
                spannable?.append("Organizer", AbsoluteSizeSpan(13, true), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable?.setSpan(ForegroundColorSpan(PlansColor.TEAL_MAIN), start, spannable?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val regularFont = Typeface.create(ResourcesCompat.getFont(itemView.context, R.font.product_sans_regular), Typeface.NORMAL)
                spannable?.setSpan(StyleSpan(regularFont.style), start, spannable?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return spannable
        }
    }

    inner class DateMsgVH(var itemBinding: CellDateMsgBinding): BaseViewHolder<MessageModel>(itemBinding.root) {
        private var message: MessageModel? = null
        override fun bind(item: MessageModel?, data: Any?, isLast: Boolean) {
            message = item
            itemBinding.apply {
                tvDate.text = message?.message
            }
        }
    }

}