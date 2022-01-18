package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellTabItemBinding
import com.planscollective.plansapp.extension.setLayoutWidth
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener

class TabItemAdapter(private var items: ArrayList<String>? = null) : RecyclerView.Adapter<TabItemAdapter.ViewHolder>(){

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
            this.items?.clear()
            this.items = it
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
        val itemBinding = CellTabItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let{
            holder.setItem(it, position == selectedItem)
        }
    }

    inner class ViewHolder(private val itemBinding: CellTabItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        init {
            val count = (items?.size ?: 1).let {
                if (it > 4) 4 else if (it < 1) 1 else it
            }

            itemView.setLayoutWidth((OSHelper.widthScreen / count.toFloat()).toInt())
            itemView.setOnSingleClickListener{
                listener?.onItemClick(this, itemView, adapterPosition)
            }
        }

        fun setItem(item: String, isSelected: Boolean = false) {
            itemBinding.tvTitle.text = item.uppercase()
            itemBinding.viewBottomBar.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}