package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellSearchEventBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import com.planscollective.plansapp.viewholders.event.SearchEventVH

class SearchEventAdapter(
    private var cellType: SearchEventVH.CellType = SearchEventVH.CellType.SEARCH_EVENT
) : RecyclerView.Adapter<EventBaseViewHolder>() {

    private var listEvents = ArrayList<EventModel>()
    private var listener: PlansActionListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(events: ArrayList<EventModel>? = null, listener: PlansActionListener? = null) {
        events?.let {
            listEvents.clear()
            listEvents.addAll(it)
        }
        listener?.let {
            this.listener = listener
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventBaseViewHolder {
        val itemBinding = CellSearchEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchEventVH(itemBinding, listener, cellType)
    }

    override fun onBindViewHolder(holder: EventBaseViewHolder, position: Int) {
        val item = listEvents[position]
        holder.bind(item, isLast = position == (listEvents.size - 1))
    }

    override fun getItemCount(): Int {
        return listEvents.size
    }

}