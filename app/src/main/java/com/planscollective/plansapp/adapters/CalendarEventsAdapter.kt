package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellEventCalendarBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.viewholders.EventBaseViewHolder
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.threeten.bp.temporal.ChronoUnit

class CalendarEventsAdapter(
    var listEvents: ArrayList<EventModel> = ArrayList<EventModel>(),
    var listener: PlansActionListener? = null,
    var selectedDay: CalendarDay? = null
) : RecyclerView.Adapter<EventBaseViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(events: ArrayList<EventModel>? = null, listener: PlansActionListener? = null, selectedDay: CalendarDay? = null) {
        events?.let {
            listEvents.clear()
            listEvents.addAll(it)
        }
        listener?.let{
            this.listener = it
        }
        selectedDay?.let {
            this.selectedDay = it
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventBaseViewHolder {
        val itemBinding = CellEventCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: EventBaseViewHolder, position: Int) {
        val item = listEvents[position]
        holder.bind(item, isLast = position == (listEvents.size - 1))
    }

    override fun getItemCount(): Int {
        return listEvents.size
    }

    inner class ViewHolder(private val itemBinding: CellEventCalendarBinding) : EventBaseViewHolder(itemBinding.root){
        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedEvent(eventModel)
            }
        }

        override fun bind(item: EventModel?, data: Any?, isLast: Boolean) {
            eventModel = item.takeIf { it != null } ?: return

            // Event Name
            itemBinding.tvEventName.text = eventModel?.eventName

            // Event Location
            var placeName = "TBD"
            var name = ""
            var address = ""
            if (!eventModel?.address.isNullOrEmpty()){
                address = eventModel!!.address!!
                placeName = address
            }
            if (!eventModel?.locationName.isNullOrEmpty()) {
                name = eventModel!!.locationName!!
                placeName = name
            }
            if (address.isNotEmpty() && name.isNotEmpty() && address.substring(0, name.count()) == name) {
                placeName = address
            }
            itemBinding.tvEventLocation.text = placeName.removeOwnCountry()

            // Bottom Separator
            itemBinding.viewBottomSpace.visibility = if (isLast) View.GONE else View.VISIBLE

            // Start/End date/time
            val startDay = eventModel?.startTime?.toLocalDate() ?: return
            val endDay = eventModel?.endTime?.toLocalDate() ?: return
            val curDay = selectedDay?.date ?: return

            val totalDays = ChronoUnit.DAYS.between(startDay, endDay).toInt()
            val passedDays = ChronoUnit.DAYS.between(startDay, curDay).toInt()
            val remainedDays = ChronoUnit.DAYS.between(curDay, endDay).toInt()

            // Days of Event
            itemBinding.tvDayOfEvent.visibility = if (totalDays > 0) {
                itemBinding.tvDayOfEvent.text = "Day ${passedDays + 1}/${totalDays + 1}"
                View.VISIBLE
            }else View.GONE

            // Start Date - End Date
            var strStartTime: String? = null
            var strEndTime: String? = null
            if (totalDays == 0) {
                strStartTime = eventModel?.startTime?.toLocalDateTime()?.toFormatString("h:mm a")
                strEndTime = eventModel?.endTime?.toLocalDateTime()?.toFormatString("h:mm a")
            }else if (passedDays == 0) { // First Day
                strStartTime = eventModel?.startTime?.toLocalDateTime()?.toFormatString("h:mm a")
                strEndTime = "12:00 AM"
            }else if (remainedDays == 0) { // Last Day
                strStartTime = "12:00 AM"
                strEndTime = eventModel?.endTime?.toLocalDateTime()?.toFormatString("h:mm a")
            }else {
                strStartTime = "All-day"
            }

            itemBinding.apply {
                tvStartTime.visibility = if (strStartTime.isNullOrEmpty()) View.GONE else {
                    tvStartTime.text = strStartTime
                    View.VISIBLE
                }
                tvEndTime.visibility = if (strEndTime.isNullOrEmpty()) View.GONE else {
                    tvEndTime.text = strEndTime
                    View.VISIBLE
                }
                tvDash.visibility = if (strStartTime.isNullOrEmpty() || strEndTime.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

        }

    }



}