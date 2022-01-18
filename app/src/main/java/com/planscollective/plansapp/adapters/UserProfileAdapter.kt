package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.*
import com.planscollective.plansapp.extension.setLayoutHeight
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.EventType
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import com.planscollective.plansapp.viewholders.event.*
import com.planscollective.plansapp.viewholders.user.UserProfileVH

class UserProfileAdapter (var typeProfile: ProfileType = ProfileType.MY_PROFILE) : RecyclerView.Adapter<BaseViewHolder<*>>(), ViewTreeObserver.OnGlobalLayoutListener {

    enum class ProfileType {
        MY_PROFILE,
        USER_PROFILE
    }

    var listEvents = ArrayList<EventModel>()
    var listener: PlansActionListener? = null
    var userModel: UserModel? = null
    var typeEvent = EventType.ORGANIZED

    var bindingUserProfile: CellUserProfileBinding? = null
    var bindingEmpty: CellEmptyProfileBinding? = null

    private var listItems = ArrayList<Map<String, Any>>()

    companion object {
        private const val TYPE_USER_PROFILE = 0
        private const val TYPE_EVENT_HEADER = 1
        private const val TYPE_EVENT_COVER = 2
        private const val TYPE_EVENT_USERS = 3
        private const val TYPE_EVENT_DATE_LOCATION = 4
        private const val TYPE_EVENT_SAVED = 5
        private const val TYPE_EMPTY = 6
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(
        user: UserModel? = null,
        events: ArrayList<EventModel>? = null,
        type: EventType? = null,
        listener: PlansActionListener? = null ) {

        events?.also {
            listEvents.clear()
            listEvents.addAll(it)
        }

        user?.also {
            userModel = it
        }

        type?.also {
            typeEvent = type
        }

        listener?.also {
            this.listener = it
        }

        listItems.clear()

        // User Profile
        userModel?.also {
            listItems.add(mapOf("type" to TYPE_USER_PROFILE, "data" to it))
        }

        when(typeProfile) {
            ProfileType.MY_PROFILE -> {}
            ProfileType.USER_PROFILE -> {
                userModel?.getAccessForMe()?.takeIf { !it.first }?.also {
                    listEvents.clear()
                }
            }
        }

        listEvents.forEach {
            when(typeEvent) {
                EventType.SAVED -> {
                    listItems.add(mapOf("type" to TYPE_EVENT_SAVED, "data" to it))
                }
                else -> {
                    listItems.add(mapOf("type" to TYPE_EVENT_HEADER, "data" to it))
                    listItems.add(mapOf("type" to TYPE_EVENT_COVER, "data" to it))
                    if (it.invitations != null && it.invitations!!.size > 0){
                        listItems.add(mapOf("type" to TYPE_EVENT_USERS, "data" to it))
                    }
                    listItems.add(mapOf("type" to TYPE_EVENT_DATE_LOCATION, "data" to it))
                }
            }
        }

        if (listEvents.isEmpty()) {
            listItems.add(mapOf("type" to TYPE_EMPTY, "data" to typeEvent))
        }


        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            TYPE_USER_PROFILE -> {
                bindingUserProfile = CellUserProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val typeHolder = if (typeProfile == ProfileType.MY_PROFILE) UserProfileVH.HolderType.MINE else UserProfileVH.HolderType.OTHERS
                UserProfileVH(bindingUserProfile!!, listener, this, typeHolder)
            }
            TYPE_EVENT_HEADER -> {
                val itemBinding = CellEventHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventHeaderViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_COVER -> {
                val itemBinding = CellEventCoverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventCoverViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_USERS -> {
                val itemBinding = CellEventUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventUsersViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_DATE_LOCATION -> {
                val itemBinding = CellEventDateLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventDateLocationViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_SAVED -> {
                val itemBinding = CellSearchEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SearchEventVH(itemBinding, listener, SearchEventVH.CellType.SAVED_EVENT)
            }
            TYPE_EMPTY -> {
                bindingEmpty = CellEmptyProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EmptyViewHolder(bindingEmpty!!, listener)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = listItems[position]
        when(holder) {
            is UserProfileVH -> {
                (holder as? UserProfileVH)?.bind(item["data"] as? UserModel, typeEvent)
            }
            is EventBaseViewHolder -> {
                (holder as? EventBaseViewHolder)?.bind(item["data"] as? EventModel, isLast = position == (listItems.size - 1))
            }
            is EmptyViewHolder -> {
                (holder as? EmptyViewHolder)?.bind(item["data"] as? EventType)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return listItems[position]["type"] as? Int ?: TYPE_USER_PROFILE
    }

    override fun onGlobalLayout() {
        bindingUserProfile?.root?.measuredHeight?.takeIf { it > 0 }?.also{
            bindingEmpty?.root?.apply {
                val bottomHeight = if (typeProfile == ProfileType.MY_PROFILE) 57.toPx() else 0
                val height = (OSHelper.heightScreen - (OSHelper.statusBarHeight + it + bottomHeight)).takeIf { it1 -> it1 > 200.toPx() } ?: 200.toPx()
                setLayoutHeight(height)
                bindingUserProfile?.root?.viewTreeObserver?.removeOnGlobalLayoutListener(this@UserProfileAdapter)
            }
            listener?.onViewSize(Size(bindingUserProfile!!.root.measuredWidth, it))
        }
    }

    inner class EmptyViewHolder (
        var itemBinding: CellEmptyProfileBinding,
        var listener: PlansActionListener? = null,
    ) : BaseViewHolder<EventType>(itemBinding.root) {

        var type : EventType? = null

        init {
            itemBinding.apply {
                btnCreateEvent.setOnSingleClickListener {
                    listener?.onClickedCreateEvent()
                }
            }
        }

        override fun bind(item: EventType?, data: Any?, isLast: Boolean) {
            type = item
            itemBinding.apply {
                if (typeProfile == ProfileType.MY_PROFILE) {
                    imvMark.visibility = View.GONE
                    when(type) {
                        EventType.ORGANIZED -> {
                            btnCreateEvent.visibility = View.VISIBLE
                            tvMessage.text = "Make Plans.\nGo Places"
                        }
                        EventType.ATTENDING -> {
                            btnCreateEvent.visibility = View.GONE
                            tvMessage.text = "Make Plans.\nJoin Friends."
                        }
                        EventType.SAVED -> {
                            btnCreateEvent.visibility = View.GONE
                            tvMessage.text = "You have no saved events."
                        }
                    }
                }else {
                    btnCreateEvent.visibility = View.GONE
                    userModel?.getAccessForMe()?.also {
                        imvMark.visibility = if (!it.first) {
                            if (it.second) {
                                tvMessage.text = "This Account is Blocked"
                                imvMark.setImageResource(R.drawable.ic_user_blocked_grey)
                            }else if (it.third) {
                                tvMessage.text = "This Account is Private"
                                imvMark.setImageResource(R.drawable.ic_lock_grey)
                            }
                            View.VISIBLE
                        }else {
                            if(listEvents.isEmpty()) {
                                tvMessage.text = "Make Plans.\nJoin Friends"
                            }
                            View.GONE
                        }
                    }
                }
            }
        }
    }

}