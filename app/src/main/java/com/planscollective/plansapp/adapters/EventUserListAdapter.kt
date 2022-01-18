package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.CellCountNumberBinding
import com.planscollective.plansapp.databinding.CellUserImageBinding
import com.planscollective.plansapp.extension.setLayoutWidth
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class EventUserListAdapter(var typeAdapter: AdapterType = AdapterType.EVENT_FEED) : RecyclerView.Adapter<BaseViewHolder<HashMap<String, Any>>>() {

    enum class AdapterType {
        EVENT_FEED,
        EVENT_DETAILS,
    }

    enum class CellType {
        USER,
        MORE,
    }

    var event: EventModel? = null
    var list = ArrayList<HashMap<String, Any>>()
    var listener: PlansActionListener? = null

    companion object {
        var countItems = 8
        val widthItemCell : Int
            get() {
                return ((OSHelper.widthScreen - 22.toPx()) / countItems.toFloat()).toInt()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(eventModel: EventModel? = null) {
        while (widthItemCell < 50.toPx()) {
            countItems -= 1
        }
        eventModel?.let {
            event = it
        }
        loadData()
        notifyDataSetChanged()
    }

    private fun loadData() {
        list.clear()
        var totalCount = 0
        event?.invitations?.forEach {
            if (it.status == 2) {
                if (list.size < (countItems - 1)) {
                    val data = hashMapOf("cellType" to CellType.USER.ordinal, "data" to it)
                    list.add(data)
                }
                totalCount ++
            }
        }
        event?.let{
            val data = hashMapOf("cellType" to CellType.MORE.ordinal,
                "totalCount" to totalCount,
                "ignoreCount" to (countItems - 1),
                "data" to it)
            list.add(data)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<HashMap<String, Any>> {
        when(viewType) {
            CellType.USER.ordinal -> {
                val itemBinding = CellUserImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return UserViewHolder(itemBinding)
            }
            CellType.MORE.ordinal -> {
                val itemBinding = CellCountNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CountNumberViewHolder(itemBinding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder<HashMap<String, Any>>, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position]["cellType"] as Int
    }

    inner class UserViewHolder(private val itemBinding: CellUserImageBinding) : BaseViewHolder<HashMap<String, Any>>(itemBinding.root) {
        var user: UserModel? = null

        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedUser(user, event)
            }
            itemBinding.apply {
                containerView.setLayoutWidth(widthItemCell)
            }
        }

        override fun bind(item: HashMap<String, Any>?, data: Any?, isLast: Boolean) {
            user = item?.get("data") as? UserModel
            if (user != null) {
                itemBinding.apply {
                    imvUserImage.setUserImage(user!!.profileImage)
                    imvLingLive.visibility = if ((user as? InvitationModel)?.isLive == 1 && (event?.isEnded == 0)) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    inner class CountNumberViewHolder(private val itemBinding: CellCountNumberBinding) : BaseViewHolder<HashMap<String, Any>>(itemBinding.root) {

        init {
            itemView.setOnSingleClickListener {
                if (typeAdapter == AdapterType.EVENT_DETAILS) {
                    UserInfo.isSeenGuideGuestList = true
                }
                listener?.onClickedMoreUsers(event)
            }
            itemBinding.apply {
                containerView.setLayoutWidth(widthItemCell)
            }
        }

        override fun bind(item: HashMap<String, Any>?, data: Any?, isLast: Boolean) {
            if (item != null) {
                val totalCount = item["totalCount"] as? Int ?: 0
                val ignoreCount = item["ignoreCount"] as? Int ?: 0

                itemBinding.apply {
                    when(item["cellType"]) {
                        CellType.MORE.ordinal -> {
                            tvCountNumber.visibility = if (totalCount > 99) {
                                imgvBackground.setUserImage(defaultImage = R.drawable.ic_dots_3_circle_green)
                                View.GONE
                            }else if (totalCount > ignoreCount){
                                imgvBackground.setUserImage(defaultImage = R.drawable.ic_circle_green_filled)
                                tvCountNumber.text = "+${totalCount - ignoreCount}"
                                View.VISIBLE
                            }else {
                                imgvBackground.setUserImage(defaultImage = R.drawable.ic_users_circle_green)
                                View.GONE
                            }
                        }
                    }
                }
            }
        }
    }


}