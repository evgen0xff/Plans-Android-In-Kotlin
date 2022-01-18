package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.*
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import com.planscollective.plansapp.viewholders.event.*

class EventListAdapter(
    private val context: Context,
    var listener: PlansActionListener? = null
) : RecyclerView.Adapter<EventBaseViewHolder>() {

    var listEvents = ArrayList<EventModel>()
    var listEventsHidden = ArrayList<EventModel>()

    private var listItems = ArrayList<Map<String, Any>>()

    companion object {
        private const val TYPE_EVENT_HEADER = 0
        private const val TYPE_EVENT_COVER = 1
        private const val TYPE_EVENT_USERS = 2
        private const val TYPE_EVENT_DATE_LOCATION = 3
        private const val TYPE_EVENT_HIDDEN_EVENTS = 4
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(events: ArrayList<EventModel>?, eventsHidden: ArrayList<EventModel>? = null) {
        listEvents.clear()
        events?.let {
            listEvents.addAll(it)
        }
        listEventsHidden.clear()
        eventsHidden?.let {
            listEventsHidden.addAll(it)
        }

        listItems.clear()
        listEvents.forEach {
            listItems.add(mapOf("type" to TYPE_EVENT_HEADER, "data" to it))
            listItems.add(mapOf("type" to TYPE_EVENT_COVER, "data" to it))
            if (it.invitations != null && it.invitations!!.size > 0){
                listItems.add(mapOf("type" to TYPE_EVENT_USERS, "data" to it))
            }
            listItems.add(mapOf("type" to TYPE_EVENT_DATE_LOCATION, "data" to it))
        }
        if (listEventsHidden.size > 0) {
            listItems.add(mapOf("type" to TYPE_EVENT_HIDDEN_EVENTS, "data" to listEventsHidden))
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventBaseViewHolder {
        return when(viewType) {
            TYPE_EVENT_HEADER -> {
                val itemBinding = CellEventHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
                EventHeaderViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_COVER -> {
                val itemBinding = CellEventCoverBinding.inflate(LayoutInflater.from(context), parent, false)
                EventCoverViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_USERS -> {
                val itemBinding = CellEventUsersBinding.inflate(LayoutInflater.from(context), parent, false)
                EventUsersViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_DATE_LOCATION -> {
                val itemBinding = CellEventDateLocationBinding.inflate(LayoutInflater.from(context), parent, false)
                EventDateLocationViewHolder(itemBinding, listener)
            }
            TYPE_EVENT_HIDDEN_EVENTS -> {
                val itemBinding = CellEventHiddenEventsBinding.inflate(LayoutInflater.from(context), parent, false)
                EventHiddenViewHolder(itemBinding, listener)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: EventBaseViewHolder, position: Int) {
        val item = listItems[position]
        if (item["type"] == TYPE_EVENT_HIDDEN_EVENTS) {
            holder.bind(data = item["data"] as? ArrayList<*>)
        }else {
            holder.bind(item["data"] as? EventModel)
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return listItems[position]["type"] as? Int ?: TYPE_EVENT_HEADER
    }

}