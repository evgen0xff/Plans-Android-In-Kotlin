package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.CellBoundaryBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener

class BoundaryAdapter(private var items: ArrayList<String>? = null) : RecyclerView.Adapter<BoundaryAdapter.ViewHolder>(){

    private var listener: OnItemTouchListener? = null
    private var selectedItem: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(items: ArrayList<String>? = null,
                      listener: OnItemTouchListener? = null,
                      selectedItem: Int? = 0) {
        selectedItem?.let {
            this.selectedItem = it
        }
        items?.let{
            this.items = arrayListOf()
            this.items?.addAll(it)
        }
        listener?.let {
            this.listener = it
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = CellBoundaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let{
            holder.setItem(it, position == selectedItem)
        }
    }

    inner class ViewHolder(private val itemBinding: CellBoundaryBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemView.setOnSingleClickListener{
                listener?.onItemClick(this, itemView, adapterPosition)
            }
        }

        fun setItem(item: String, isSelected: Boolean = false) {
            itemBinding.tvBoundary.text = item + "ft"
            val resourceId = if (isSelected) R.drawable.button_bkgnd_purple else R.drawable.button_bkgnd_gray
            itemBinding.btnBoundary.background = ContextCompat.getDrawable(PLANS_APP, resourceId)
        }
    }
}