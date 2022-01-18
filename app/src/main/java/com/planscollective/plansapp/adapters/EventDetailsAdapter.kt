package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.*
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.PostModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import com.planscollective.plansapp.viewholders.event.*
import com.planscollective.plansapp.viewholders.header.SectionViewHolder
import com.planscollective.plansapp.viewholders.post.PostCommentVH

class EventDetailsAdapter(
    var eventModel: EventModel? = null,
    var listener: PlansActionListener? = null
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    var listPosts = ArrayList<PostModel>()
    private var listItems = ArrayList<HashMap<String, Any>>()

    companion object {
        private const val TYPE_COVER = 0
        private const val TYPE_HEADER = 1
        private const val TYPE_DATE_LOCATION = 2
        private const val TYPE_OPTIONS = 3
        private const val TYPE_LIVE_MOMENT = 4
        private const val TYPE_USERS = 5
        private const val TYPE_POST = 6
        private const val TYPE_SECTION_HEADER = 7
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(eventModel: EventModel? = null, listPosts: ArrayList<PostModel>? = null, listener: PlansActionListener? = null) {
        eventModel?.let {
            this.eventModel = it
        }

        listPosts?.let {
            this.listPosts.clear()
            this.listPosts.addAll(it)
        }

        listener?.let {
            this.listener = it
        }

        listItems.clear()
        this.eventModel?.let { it ->
            // Event Cover
            listItems.add(hashMapOf("type" to TYPE_COVER, "data" to it))

            // Event Header
            listItems.add(hashMapOf("type" to TYPE_HEADER, "data" to it))

            // Event Location
            listItems.add(hashMapOf("type" to TYPE_DATE_LOCATION, "data" to it))

            // Event Options
            listItems.add(hashMapOf("type" to TYPE_OPTIONS, "data" to it))

            // Event Live Moment
            if (it.isEnableAddLiveMoment() || (it.countLiveMoments ?: 0) > 0){
                listItems.add(hashMapOf("type" to TYPE_LIVE_MOMENT, "data" to it))
            }

            // Event Guests
            if ((it.invitations?.size ?: 0) > 0){
                listItems.add(hashMapOf("type" to TYPE_USERS, "data" to it))
            }

            // Posts
            if (this.listPosts.size > 0) {
                listItems.add(hashMapOf("type" to TYPE_SECTION_HEADER, "data" to "Posts"))
                this.listPosts.forEach {
                    listItems.add(hashMapOf("type" to TYPE_POST, "post" to it, "data" to hashMapOf("event" to eventModel)))
                }
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            TYPE_COVER -> {
                val itemBinding = CellEventCoverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventCoverViewHolder(itemBinding, listener, EventCoverViewHolder.EventCoverType.EVENT_DETAILS)
            }
            TYPE_HEADER -> {
                val itemBinding = CellEventHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventHeaderViewHolder(itemBinding, listener, EventHeaderViewHolder.HeaderType.EVENT_DETAILS)
            }
            TYPE_DATE_LOCATION -> {
                val itemBinding = CellEventDateLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventDateLocationViewHolder(itemBinding, listener, EventDateLocationViewHolder.HolderType.EVENT_DETAILS)
            }
            TYPE_OPTIONS -> {
                val itemBinding = CellEventOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventOptionsViewHolder(itemBinding, listener)
            }
            TYPE_LIVE_MOMENT -> {
                val itemBinding = CellEventLivemomentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventLiveMomentsViewHolder(itemBinding, listener)
            }
            TYPE_USERS -> {
                val itemBinding = CellEventUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventUsersViewHolder(itemBinding, listener, EventUsersViewHolder.HolderType.EVENT_DETAILS)
            }
            TYPE_SECTION_HEADER -> {
                val itemBinding = CellSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SectionViewHolder(itemBinding)
            }
            TYPE_POST -> {
                val itemBinding = CellPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostCommentVH(itemBinding, listener, PostCommentVH.HolderType.EVENT_POSTS)
            }


            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = listItems[position]
        when(holder) {
            is EventBaseViewHolder -> {
                (holder as? EventBaseViewHolder)?.bind(eventModel, item["data"], position == (listItems.size - 1))
            }
            is PostCommentVH -> {
                (holder as? PostCommentVH)?.bind(item["post"] as? PostModel, item["data"], position == (listItems.size - 1))
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
        return listItems[position]["type"] as? Int ?: TYPE_COVER
    }

}