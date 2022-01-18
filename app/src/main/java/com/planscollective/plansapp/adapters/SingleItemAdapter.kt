package com.planscollective.plansapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellItemSingleBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener

class SingleItemAdapter : RecyclerView.Adapter<SingleItemAdapter.ViewHolder>(){

    var items = ArrayList<String>()
    lateinit var listener: OnItemTouchListener

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = CellItemSingleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    inner class ViewHolder(private val itemBinding: CellItemSingleBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemView.setOnSingleClickListener{
                listener?.onItemClick(this, itemView, adapterPosition)
            }
        }

        fun setItem(item: String) {
            itemBinding.text.text = item
        }
    }
}