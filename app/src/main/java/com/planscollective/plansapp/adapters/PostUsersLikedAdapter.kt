package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellUserImageBinding
import com.planscollective.plansapp.extension.setLayoutWidth
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.setUserImage
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.UserModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class UsersLikedAdapter : RecyclerView.Adapter<BaseViewHolder<UserModel>>() {

    var list = ArrayList<UserModel>()
    var listener: PlansActionListener? = null

    private var widthItemCell = 50.toPx()


    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<UserModel>? = null, widthCell: Int? = null, listener: PlansActionListener? = null) {
        users?.let{
            list.clear()
            list.addAll(it)
        }
        widthCell?.let {
            widthItemCell = widthCell
        }
        listener?.let {
            this.listener = it
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserModel> {
        val itemBinding = CellUserImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserModel>, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class UserViewHolder(private val itemBinding: CellUserImageBinding) : BaseViewHolder<UserModel>(itemBinding.root) {
        var user: UserModel? = null

        init {
            itemView.setOnSingleClickListener {
                listener?.onClickedUser(user)
            }
            itemBinding.apply {
                containerView.setLayoutWidth(widthItemCell)
            }
        }

        override fun bind(item: UserModel?, data: Any?, isLast: Boolean) {
            user = item
            itemBinding.apply {
                imvLingLive.visibility = View.INVISIBLE
                imvUserImage.setUserImage(user?.profileImage)
            }
        }
    }

}