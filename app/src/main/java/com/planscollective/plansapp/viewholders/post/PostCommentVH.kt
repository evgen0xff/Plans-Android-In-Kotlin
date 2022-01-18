package com.planscollective.plansapp.viewholders.post

import android.view.View
import com.planscollective.plansapp.R
import com.planscollective.plansapp.classes.OnSingleClickListener
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.CellPostCommentBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class PostCommentVH(
    private val itemBinding: CellPostCommentBinding,
    var listener: PlansActionListener? = null,
    var type: HolderType = HolderType.EVENT_POSTS
) : BaseViewHolder<PostModel>(itemBinding.root), OnSingleClickListener {

    enum class HolderType {
        EVENT_POSTS,
        POST_DETAILS,
        POST_COMMENTS
    }

    private var event: EventModel? = null
    private var post: PostModel? = null
    private var content: PostModel? = null

    init {
        if (type == HolderType.EVENT_POSTS) {
            itemView.setOnSingleClickListener(this)
        }
        itemBinding.let {
            it.btnUserImage.setOnSingleClickListener(this)
            it.btnMenu.setOnSingleClickListener(this)
            it.layoutLikesAndComments.setOnSingleClickListener(this)
            if (type == HolderType.POST_DETAILS) {
                it.layoutMedia.setOnSingleClickListener(this)
            }
        }
    }

    override fun bind(item: PostModel?, data: Any?, isLast: Boolean) {
        content = item
        post = (data as? HashMap<*, *>)?.get("post") as? PostModel
        event = (data as? HashMap<*, *>)?.get("event") as? EventModel

        if (content != null) {
            configureUI(isLast)
            itemBinding.apply {
                // User Image
                imvUserImage.setUserImage(content?.user?.profileImage)

                // User Name
                tvUserName.text = "${content?.user?.firstName} ${content?.user?.lastName}"

                // Organizer Mark
                layoutOrganizer.visibility = if (content?.user?.id == event?.userId) View.VISIBLE else View.GONE

                // Time Posted
                tvTime.text = content?.createdAt?.toLocalDateTime()?.timeAgoSince()

                // Text Post
                layoutMessage.visibility = if (content?.text.isNullOrEmpty()) View.GONE else {
                    tvMessage.text = content?.text
                    View.VISIBLE
                }

                // Media Post
                layoutMedia.visibility = if (content?.isMediaType == false) View.GONE else {
                    val width = (content?.width ?: 0).toFloat()
                    val height = (content?.height ?: 0).toFloat()
                    val heightLayout = if ( width > 0 && height > 0) {
                        val max = OSHelper.widthScreen.toFloat()
                        val tempHeight = (height / width) * max
                        if (tempHeight > max) max.toInt() else tempHeight.toInt()
                    }else 200.toPx()
                    layoutMedia.setLayoutHeight(heightLayout)

                    if (content?.type == "video"){
                        videoView.visibility = View.VISIBLE
                        videoView.playVideoUrl(content?.urlMedia, content?.urlThumbnail)
                    }else {
                        videoView.visibility = View.GONE
                        imgvCoverImage.setEventImage(content?.urlMedia)
                    }
                    View.VISIBLE
                }

                // Likes Image/Count
                if (content?.isLike == true) {
                    imgvLike.setImageResource(R.drawable.ic_heart_filled_green)
                    tvLikes.setTextColor(PlansColor.TEAL_MAIN)
                }else {
                    imgvLike.setImageResource(R.drawable.ic_heart_outline_grey)
                    tvLikes.setTextColor(PlansColor.BLACK)
                }

                tvLikes.visibility = if ((content?.likesCounts ?: 0) > 0) {
                    tvLikes.text = "${content?.likesCounts}"
                    View.VISIBLE
                }else {
                    View.GONE
                }

                // Users liked
                val layoutsUsers = arrayListOf(layoutUser1, layoutUser2, layoutUser3).onEach { it.visibility = View.GONE }
                val imgvsUsers = arrayListOf(imgvUser1, imgvUser2, imgvUser3)

                content?.likes?.let {
                    for (i in 0 until it.size){
                        if (i < layoutsUsers.size) {
                            layoutsUsers[i].visibility = View.VISIBLE
                            imgvsUsers[i].setUserImage (it[i].userDetails?.profileImage)
                        }else {
                            break
                        }
                    }
                }

                // Comments Count
                val count = content?.commentsCounts ?: 0
                layoutComments.visibility = if ( count > 0) {
                    tvComments.text = "$count"
                    View.VISIBLE
                }else {
                    View.GONE
                }

            }
        }
    }

    private fun configureUI(isLast: Boolean = false) {
        itemBinding.apply {
            viewBottomSeparator.visibility = if (isLast) View.GONE else {
                viewBottomSeparator.setLayoutMargin(top = 0.toPx())
                View.VISIBLE
            }
            when(type) {
                HolderType.EVENT_POSTS -> {
                    btnMenu.visibility = View.GONE
                    layoutLikesAndComments.visibility = View.VISIBLE
                    tvMessage.linksClickable = false
                }
                HolderType.POST_DETAILS -> {
                    btnMenu.visibility = View.GONE
                    layoutLikesAndComments.visibility = View.GONE
                    viewBottomSeparator.visibility = if (content?.isMediaType ?: isLast) View.GONE else {
                        viewBottomSeparator.setLayoutMargin(top = 8.toPx())
                        View.VISIBLE
                    }
                    tvMessage.linksClickable = true
                }
                HolderType.POST_COMMENTS -> {
                    btnMenu.visibility = View.VISIBLE
                    layoutLikesAndComments.visibility = View.VISIBLE
                    tvMessage.linksClickable = true
                }
            }
        }
    }


    override fun onSingleClick(v: View?) {
        itemBinding.apply {
            when(v){
                itemView -> {
                    listener?.onClickPost(content, event)
                }
                btnUserImage -> {
                    listener?.onClickedUser(content?.user, event)
                }
                btnMenu -> {
                    listener?.onClickMoreMenuForContent(content, post, event)
                }
                layoutMedia -> {
                    if (content?.type == "video") {
                        listener?.onClickPlayVideo(content?.urlMedia, event)
                    }else if (content?.type == "image"){
                        listener?.onClickOpenPhoto(content?.urlMedia, event)
                    }
                }
                layoutLikesAndComments -> {
                    listener?.onClickLikeContent(content, post, event, !(content?.isLike ?: false))
                }
            }
        }
    }

}