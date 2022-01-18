package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellEmptyCommentBinding
import com.planscollective.plansapp.databinding.CellPostCommentBinding
import com.planscollective.plansapp.databinding.CellPostUsersLikedBinding
import com.planscollective.plansapp.databinding.CellSectionHeaderBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.header.SectionViewHolder
import com.planscollective.plansapp.viewholders.post.PostCommentVH
import com.planscollective.plansapp.viewholders.post.PostUsersLikedVH

class PostDetailsAdapter(
    var postModel: PostModel? = null,
    var eventModel: EventModel? = null,
    var listener: PlansActionListener? = null
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    var listComments = ArrayList<PostModel>()
    private var listItems = ArrayList<HashMap<String, Any?>>()

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_USERS_LIKED = 1
        private const val TYPE_SECTION_HEADER = 2
        private const val TYPE_COMMENT = 3
        private const val TYPE_EMPTY = 4
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(post: PostModel? = null,
                      event: EventModel? = null,
                      comments: ArrayList<PostModel>? = null,
                      listener: PlansActionListener? = null) {
        post?.let{
           postModel = it
        }
        event?.let {
            eventModel = it
        }
        comments?.let {
            listComments.clear()
            listComments.addAll(it)
        }
        listener?.let {
            this.listener = it
        }

        listItems.clear()
        postModel?.let { it ->
            // Post Cover
            listItems.add(hashMapOf("type" to TYPE_POST,
                                    "content" to it,
                                    "data" to hashMapOf("event" to eventModel)))

            // Users Liked
            listItems.add(hashMapOf("type" to TYPE_USERS_LIKED,
                                    "content" to it,
                                    "data" to hashMapOf("event" to eventModel)))

            // Comments
            if (listComments.size > 0) {
                // Comments Header
                val strComments = if (listComments.size > 1) "${listComments.size} Comments" else "1 Comment"
                listItems.add(hashMapOf("type" to TYPE_SECTION_HEADER, "data" to strComments))

                // Comments List
                listComments.forEach {
                    listItems.add(hashMapOf("type" to TYPE_COMMENT,
                                            "content" to it,
                                            "data" to hashMapOf("post" to postModel, "event" to eventModel)))
                }
            }else {
                listItems.add(hashMapOf("type" to TYPE_EMPTY, "content" to it))
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            TYPE_POST -> {
                val itemBinding = CellPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostCommentVH(itemBinding, listener, PostCommentVH.HolderType.POST_DETAILS)
            }
            TYPE_USERS_LIKED -> {
                val itemBinding = CellPostUsersLikedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostUsersLikedVH(itemBinding, listener)
            }
            TYPE_SECTION_HEADER -> {
                val itemBinding = CellSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SectionViewHolder(itemBinding)
            }
            TYPE_COMMENT -> {
                val itemBinding = CellPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostCommentVH(itemBinding, listener, PostCommentVH.HolderType.POST_COMMENTS)
            }
            TYPE_EMPTY -> {
                val itemBinding = CellEmptyCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BaseViewHolder<PostModel>(itemBinding.root)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = listItems[position]
        when(holder) {
            is PostCommentVH -> {
                (holder as? PostCommentVH)?.bind(item["content"] as? PostModel, item["data"], position == (listItems.size - 1))
            }
            is PostUsersLikedVH -> {
                (holder as? PostUsersLikedVH)?.bind(item["content"] as? PostModel, item["data"], position == (listItems.size - 1))
            }
            is SectionViewHolder -> {
                (holder as? SectionViewHolder)?.bind(item["data"] as? String, isLast = position == (listItems.size - 1))
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return listItems[position]["type"] as? Int ?: TYPE_POST
    }

}