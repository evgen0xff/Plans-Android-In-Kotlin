package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellUserSelectionBinding
import com.planscollective.plansapp.extension.setLayoutMargin
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class FriendsSelectionAdapter : RecyclerView.Adapter<BaseViewHolder<UserModel>>() {

    private var listUsers = ArrayList<UserModel>()
    private var listSelected = ArrayList<UserModel>()
    private var listener: PlansActionListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<UserModel>? = null, selected: ArrayList<UserModel>? = null,  listener: PlansActionListener? = null) {
        users?.also {
            listUsers.clear()
            listUsers.addAll(it)
        }

        selected?.also {
            listSelected.clear()
            listSelected.addAll(it)
        }

        listener?.also { this.listener = it}

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserModel> {
        val itemBinding = CellUserSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserSelectionVH(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserModel>, position: Int) {
        val item = listUsers[position]
        holder.bind(item, isLast = position == (listUsers.size - 1))
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    inner class UserSelectionVH(var itemBinding: CellUserSelectionBinding) : BaseViewHolder<UserModel>(itemBinding.root) {
        private var user: UserModel? = null

        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedUser(user)
            }

            itemBinding.apply {
                checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked != listSelected.any { it.id == user?.id }) {
                        listener?.onSelectedUser(user)
                    }
                }
            }
        }

        override fun bind(item: UserModel?, data: Any?, isLast: Boolean) {
            user = item
            itemBinding.apply {
                // User Image
                imvUserImage.setUserImage(user?.profileImage)

                // User Name
                tvUserName.text = user?.fullName ?: user?.name ?: "${user?.firstName ?: ""} ${user?.lastName ?: ""}"

                // CheckBox
                checkbox.isChecked = listSelected.any { it.id == user?.id }
            }

            // Bottom
            itemView.setLayoutMargin(bottom = if(isLast) 100.toPx() else 0)
        }
    }
}