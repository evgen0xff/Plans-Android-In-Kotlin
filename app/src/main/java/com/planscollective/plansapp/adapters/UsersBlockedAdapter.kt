package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.databinding.CellUserItemBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.user.UserItemVH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersBlockedAdapter : PlansRecyclerViewAdapter<UserModel>() {

    private var listUsers = ArrayList<UserModel>()
    private var listener: PlansActionListener? = null
    private var positionSelected: Int? = null
    private var userSelected: UserModel? = null
    private var isAnimating = false
    private var recyclerViewAttached: RecyclerView? = null

    fun updateAdapter(users: ArrayList<UserModel>? = null, listener: PlansActionListener? = null) {
        listener?.also { this.listener = it}
        if (positionSelected != null && userSelected != null) {
            moveSelectedItem(users)
        }else {
            updateAll(users)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll(list: List<UserModel>?) {
        positionSelected = null
        userSelected = null
        isAnimating = false

        list?.also {
            listUsers.clear()
            listUsers.addAll(it)
        }
        notifyDataSetChanged()
    }

    private fun moveSelectedItem(newList: List<UserModel>?) {
        newList?.indexOfFirst { it.id == userSelected?.id }?.takeIf { it >= 0 && !isAnimating }?.also { toPosition ->
            if (toPosition == positionSelected) {
                updateAll(newList)
            }else {
                isAnimating = true
                val newItem = newList[toPosition]
                listUsers[positionSelected!!] = newItem
                notifyItemChanged(positionSelected!!)

                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.IO){
                    delay(200)
                    withContext(Dispatchers.Main) {
                        if (isAnimating) {
                            listUsers.removeAt(positionSelected!!)
                            val newPosition = Integer.min(listUsers.size, toPosition)
                            listUsers.add(newPosition, newItem)
                            notifyItemMoved(positionSelected!!, newPosition)
                            if (positionSelected!! == 0) {
                                recyclerViewAttached?.layoutManager?.scrollToPosition(0)
                            }

                            positionSelected = null
                            userSelected = null
                            isAnimating = false
                        }
                    }
                }
            }
        } ?: run {
            updateAll(newList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserModel> {
        val itemBinding = CellUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserItemVH(itemBinding, listener, UserItemVH.HolderType.PLANS_USER, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserModel>, position: Int) {
        val item = listUsers[position]
        holder.bind(item, isLast = position == (listUsers.size - 1))
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    override fun onItemSelected(position: Int?, data: Any?) {
        positionSelected = position
        userSelected = data as? UserModel
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewAttached = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerViewAttached = null
    }

}