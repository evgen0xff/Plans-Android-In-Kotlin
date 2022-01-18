package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.databinding.CellUserItemBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.ChatModel
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.user.UserItemVH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatSettingsAdapter : PlansRecyclerViewAdapter<UserModel>() {

    private var chat: ChatModel? = null
    private var listener: PlansActionListener? = null
    private var listItems = ArrayList<UserModel>()
    private var positionSelected: Int? = null
    private var userSelected: UserModel? = null
    private var isBottomMargin = true
    private var isAnimating = false
    private var recyclerViewAttached: RecyclerView? = null


    fun updateAdapter(chatModel: ChatModel? = null, listener: PlansActionListener? = null) {
        chatModel?.also { chat = it }
        listener?.also { this.listener = it }

        var newList: List<UserModel>? = null
        chat?.people?.takeIf { it.isNotEmpty() }?.also {
            newList = it.sortedWith(compareByDescending<UserModel>{it.isFriend}
                .thenBy{ it.id == UserInfo.userId}
                .thenBy{ it.friendShipStatus }
                .thenBy{ it.friendRequestSender == UserInfo.userId}
                .thenBy{ it.fullName}
            )
        }

        if (positionSelected != null && userSelected != null) {
            moveSelectedItem(newList)
        }else {
            updateAll(newList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll(list: List<UserModel>?) {
        positionSelected = null
        userSelected = null
        isBottomMargin = true
        isAnimating = false

        list?.also {
            listItems.clear()
            listItems.addAll(it)
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
                listItems[positionSelected!!] = newItem
                notifyItemChanged(positionSelected!!)

                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.IO){
                    delay(200)
                    withContext(Dispatchers.Main) {
                        if (isAnimating) {
                            updateLastItem()

                            listItems.removeAt(positionSelected!!)
                            val newPosition = Integer.min(listItems.size, toPosition)
                            listItems.add(newPosition, newItem)
                            notifyItemMoved(positionSelected!!, newPosition)
                            if (positionSelected!! == 0) {
                                recyclerViewAttached?.layoutManager?.scrollToPosition(0)
                            }

                            positionSelected = null
                            userSelected = null
                            isBottomMargin = true
                            isAnimating = false
                        }
                    }
                }
            }
        } ?: run {
            updateAll(newList)
        }
    }
    private fun updateLastItem(isMargin: Boolean = false) {
        isBottomMargin = isMargin
        listItems.size.takeIf { it > 0 }?.also {
            notifyItemChanged(it - 1)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserModel> {
        val itemBinding = CellUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserItemVH(itemBinding, listener, UserItemVH.HolderType.CHAT_SETTINGS, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserModel>, position: Int) {
        holder.bind(listItems[position], chat, position == listItems.size - 1 && isBottomMargin)
    }

    override fun getItemCount(): Int {
        return listItems.size
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