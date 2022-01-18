package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellEventInvitationBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class EventInvitationsAdapter : RecyclerView.Adapter<BaseViewHolder<EventModel>>() {

    private var listEvents = ArrayList<EventModel>()
    private var listener: PlansActionListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<EventModel>? = null, listener: PlansActionListener? = null) {
        users?.also {
            listEvents.clear()
            listEvents.addAll(it)
        }
        listener?.also { this.listener = it}

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EventModel> {
        val itemBinding = CellEventInvitationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventInvitationVH(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<EventModel>, position: Int) {
        holder.bind(listEvents[position])
    }

    override fun getItemCount(): Int {
        return listEvents.size
    }

    //************************************* Event Invitation VewHolder ************************//
    inner class EventInvitationVH(var itemBinding: CellEventInvitationBinding) : BaseViewHolder<EventModel>(itemBinding.root) {
        private var event : EventModel? = null
        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedEvent(event)
            }
            itemBinding.apply {
                imvUserImage.setOnSingleClickListener {
                    listener?.onClickedUser(event?.eventCreatedBy)
                }
                btnGoing.setOnSingleClickListener {
                    listener?.onClickEventInvitationAction("GOING", event)
                }
                btnMaybe.setOnSingleClickListener {
                    listener?.onClickEventInvitationAction("MAYBE", event)
                }
                btnNextTime.setOnSingleClickListener {
                    listener?.onClickEventInvitationAction("NEXT TIME", event)
                }
            }
        }

        override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
            event = item
            itemBinding.apply {
                imvUserImage.setUserImage(event?.eventCreatedBy?.profileImage)
                tvEventName.text = event?.eventName
                tvOrganizedBy.text = "Organized by ${event?.eventCreatedBy?.firstName ?: ""} ${event?.eventCreatedBy?.lastName ?: ""}"
                tvAgoTime.text = event?.invitations?.firstOrNull { it.id == UserInfo.userId }?.invitationTime?.toLocalDateTime()?.timeAgoSince()
                val maxWidth = OSHelper.widthScreen - (32 + 40 + 8 + 16 + 4).toPx()
                tvEventDate.text = event?.getStartEndTime(maxWidth, tvEventDate)
            }
        }
    }

}