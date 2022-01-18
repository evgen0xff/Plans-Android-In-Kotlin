package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellPostLikedBinding
import com.planscollective.plansapp.extension.setEventImage
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.PostLikedModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class PostsLikedAdapter : RecyclerView.Adapter<PostsLikedAdapter.PostLikedVH>() {

    private var listPosts = ArrayList<PostLikedModel>()
    private var listener: PlansActionListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: ArrayList<PostLikedModel>? = null,
                      listener: PlansActionListener? = null) {
        list?.also{
            listPosts.clear()
            listPosts.addAll(it)
        }

        listener?.also {
            this.listener = it
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostLikedVH {
        val itemBinding = CellPostLikedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostLikedVH(itemBinding)
    }

    override fun onBindViewHolder(holder: PostLikedVH, position: Int) {
        holder.bind(listPosts[position], isLast = position == (listPosts.size - 1))
    }

    override fun getItemCount(): Int {
        return listPosts.size
    }


    inner class PostLikedVH(var itemBinding: CellPostLikedBinding) : BaseViewHolder<PostLikedModel>(itemBinding.root) {
        var post : PostLikedModel? = null

        init {
            itemView.setOnSingleClickListener {
                listener?.onClickPost(postId = post?.id, eventId = post?.eventId)
            }
            itemBinding.apply {
                layoutEvent.setOnSingleClickListener {
                    listener?.onClickedEvent(eventId = post?.eventId)
                }
                btnLike.setOnSingleClickListener{
                    listener?.onClickLikePost(post?.id, post?.eventId, false)
                }
            }
        }

        override fun bind(item: PostLikedModel?, data: Any?, isLast: Boolean) {
            post = item

            itemBinding.apply {
                // Event Cover Image
                imvEventCover.setEventImage(post?.eventImageUrl)

                // Event Name
                tvEventName.text = post?.eventName

                // Organized by ...
                tvOrganizedBy.text = "Organized by ${post?.firstName ?: ""} ${post?.lastName ?: ""}"

                // Post Text
                layoutPostText.visibility = if (post?.postText.isNullOrEmpty()) View.GONE else {
                    tvPostText.text = post?.postText
                    View.VISIBLE
                }

                // Media Post
                layoutMedia.visibility = if (post?.postType.isNullOrEmpty() || post?.postType == "text") View.GONE else {
                    if (post?.postType == "video"){
                        videoView.visibility = View.VISIBLE
                        videoView.playVideoUrl(post?.postMedia, post?.postImageUrl)
                    }else {
                        videoView.visibility = View.GONE
                        imgvCoverImage.setEventImage(post?.postImageUrl)
                    }
                    val width = (post?.width ?: 0).toFloat()
                    val height = (post?.height ?: 0).toFloat()
                    val lp = layoutMedia.layoutParams
                    if ( width > 0 && height > 0) {
                        val max = OSHelper.widthScreen.toFloat()
                        val tempHeight = (height / width) * max
                        if (tempHeight > max) {
                            lp.height = max.toInt()
                        }else {
                            lp.height = tempHeight.toInt()
                        }
                    }else {
                        lp.height = 200.toPx()
                    }
                    layoutMedia.layoutParams = lp
                    View.VISIBLE
                }

                // Bottom Separator
                separatorBottom.visibility = if (isLast) View.GONE else View.VISIBLE
            }
        }
    }

}