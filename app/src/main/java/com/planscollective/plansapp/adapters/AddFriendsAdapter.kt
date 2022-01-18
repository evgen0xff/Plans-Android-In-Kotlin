package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.databinding.CellSectionTitleBinding
import com.planscollective.plansapp.databinding.CellUserItemBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.interfaces.PlansItemMoveListener
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.models.viewModels.AddFriendsVM
import com.planscollective.plansapp.viewholders.BaseViewHolder
import com.planscollective.plansapp.viewholders.user.UserItemVH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFriendsAdapter : RecyclerView.Adapter<BaseViewHolder<*>>(), PlansItemMoveListener {

    private var listUsers = ArrayList<UserModel>()
    private val listContacts = ArrayList<UserModel>()
    private var listener: PlansActionListener? = null
    private var typeUser = AddFriendsVM.UserType.PLANS_USERS
    private var listItems = ArrayList<HashMap<String, Any>>()

    private var positionSelected: Int? = null
    private var userSelected: UserModel? = null
    private var isAnimating = false
    private var recyclerViewAttached: RecyclerView? = null

    enum class HolderType {
        USER_ITEM,
        SECTION_HEADER
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<UserModel>? = null,
                      contacts: ArrayList<UserModel>? = null,
                      userType: AddFriendsVM.UserType? = null,
                      listener: PlansActionListener? = null
    ) {
        users?.also {
            listUsers.clear()
            listUsers.addAll(it)
        }

        contacts?.also{
            listContacts.clear()
            listContacts.addAll(it)
        }

        userType?.also{
            typeUser = it
        }

        listener?.also { this.listener = it}

        val newList = ArrayList<HashMap<String, Any>>()
        listUsers.forEach { newList.add(hashMapOf("TYPE_HOLDER" to HolderType.USER_ITEM, "TYPE_USER" to typeUser, "DATA" to it)) }
        listContacts.takeIf{it.isNotEmpty()}?.also {
            newList.add(hashMapOf("TYPE_HOLDER" to HolderType.SECTION_HEADER, "DATA" to "Invite Contacts"))
            it.forEach { contact ->
                newList.add(hashMapOf("TYPE_HOLDER" to HolderType.USER_ITEM, "TYPE_USER" to typeUser, "DATA" to contact))
            }
        }

        if (positionSelected != null && userSelected != null) {
            moveSelectedItem(newList)
        }else {
            updateAll(newList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll(list: List<HashMap<String, Any>>?) {
        positionSelected = null
        userSelected = null
        isAnimating = false

        list?.also {
            listItems.clear()
            listItems.addAll(it)
        }
        notifyDataSetChanged()
    }

    private fun moveSelectedItem(newList: List<HashMap<String, Any>>?) {
        newList?.indexOfFirst {
            val user = it["DATA"] as? UserModel
            user != null && user.mobile == userSelected?.mobile
        }?.takeIf { it >= 0 && !isAnimating }?.also { toPosition ->
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
                            listItems.removeAt(positionSelected!!)
                            val newPosition = Integer.min(listItems.size, toPosition)
                            listItems.add(newPosition, newItem)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            HolderType.USER_ITEM.ordinal -> {
                val itemBinding = CellUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserItemVH(itemBinding,
                    listener,
                    if (typeUser == AddFriendsVM.UserType.PLANS_USERS) UserItemVH.HolderType.PLANS_USER else UserItemVH.HolderType.CONTACTS,
                    this
                )
            }
            HolderType.SECTION_HEADER.ordinal -> {
                val itemBinding = CellSectionTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SectionViewHolder(itemBinding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = listItems[position]
        when(holder) {
            is UserItemVH -> {
                val holderType = if (typeUser == AddFriendsVM.UserType.PLANS_USERS) UserItemVH.HolderType.PLANS_USER else UserItemVH.HolderType.CONTACTS
                (holder as? UserItemVH)?.bind(item["DATA"] as? UserModel, hashMapOf("HOLDER_TYPE" to holderType), isLast = position == (listItems.size - 1))
            }
            is SectionViewHolder -> {
                (holder as? SectionViewHolder)?.bind(item["DATA"] as? String, isLast = position == (listItems.size - 1))
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return (listItems[position]["TYPE_HOLDER"] as? HolderType)?.ordinal ?: HolderType.USER_ITEM.ordinal
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



    inner class SectionViewHolder(
        private val itemBinding: CellSectionTitleBinding
    ) : BaseViewHolder<String>(itemBinding.root) {

        override fun bind(item: String?, data: Any?, isLast: Boolean) {
            itemBinding.apply {
                tvHeader.text = item
            }
        }
    }

}