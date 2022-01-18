package com.planscollective.plansapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellItemMenuBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.models.dataModels.MenuModel

class MenuItemAdapter : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>(){

    var items = ArrayList<MenuModel>()
    lateinit var listener: OnItemTouchListener

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = CellItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    inner class ViewHolder(private val itemBinding: CellItemMenuBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemView.setOnSingleClickListener{
                listener?.onItemClick(this, itemView, adapterPosition)
            }
        }

        fun setItem(item: MenuModel) {
            itemBinding.imageView.setImageResource(item.imageId)
            itemBinding.textView.text = item.titleText
        }
    }
}