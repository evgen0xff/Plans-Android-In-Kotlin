package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellLocationBinding
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener

class LocationItemAdapter : RecyclerView.Adapter<LocationItemAdapter.ViewHolder>(){

    private var items = ArrayList<String>()
    private var listener: OnItemTouchListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(items: ArrayList<String>? = null,
                      listener: OnItemTouchListener? = null) {
        items?.also{
            this.items.clear()
            this.items.addAll(it)
        }
        listener?.also {
            this.listener = it
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = CellLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(items[position])
    }

    inner class ViewHolder(private val itemBinding: CellLocationBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemView.setOnSingleClickListener{
                listener?.onItemClick(this, itemView, adapterPosition)
            }
        }
        fun setItem(item: String) {
            itemBinding.tvLocationName.text = item.removeOwnCountry()
        }
    }
}