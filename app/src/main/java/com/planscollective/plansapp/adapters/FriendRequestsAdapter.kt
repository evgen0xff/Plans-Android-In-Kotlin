package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellFriendRequestBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class FriendRequestsAdapter : RecyclerView.Adapter<BaseViewHolder<UserModel>>() {

    private var listUsers = ArrayList<UserModel>()
    private var listener: PlansActionListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<UserModel>? = null, listener: PlansActionListener? = null) {
        users?.also {
            listUsers.clear()
            listUsers.addAll(it)
        }
        listener?.also { this.listener = it}

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserModel> {
        val itemBinding = CellFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendRequestVH(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserModel>, position: Int) {
        val item = listUsers[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    inner class FriendRequestVH(var itemBinding: CellFriendRequestBinding) : BaseViewHolder<UserModel>(itemBinding.root) {
        private var user : UserModel? = null
        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedUser(user)
            }

            itemBinding.apply {
                btnAccept.setOnSingleClickListener {
                    listener?.onClickedFriendAction(user, "ACCEPT")
                }
                btnReject.setOnSingleClickListener {
                    listener?.onClickedFriendAction(user, "REJECT")
                }
            }
        }

        override fun bind(item: UserModel?, data: Any?, isLast: Boolean) {
            user = item
            itemBinding.apply {
                imvUserImage.setUserImage(user?.profileImage)
                tvUserName.text = user?.name ?: user?.fullName ?: "${user?.firstName ?: ""} ${user?.lastName ?: ""}"
                tvAddress.visibility = if (user?.userLocation.isNullOrEmpty()) View.GONE else {
                    tvAddress.text = user?.userLocation
                    View.VISIBLE
                }
            }
        }
    }

}