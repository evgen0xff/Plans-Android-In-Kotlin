package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.CellUserInvitationBinding
import com.planscollective.plansapp.extension.*
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.interfaces.PlansItemMoveListener
import com.planscollective.plansapp.models.dataModels.EventModel
import com.planscollective.plansapp.models.dataModels.InvitationModel
import com.planscollective.plansapp.models.viewModels.EditInvitationVM
import com.planscollective.plansapp.viewholders.BaseViewHolder
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditInvitationAdapter : PlansRecyclerViewAdapter<InvitationModel>() {

    private var listUsers = ArrayList<InvitationModel>()
    private var listStatus = ArrayList<EditInvitationVM.UserStatus>()
    private var listener: PlansActionListener? = null
    private var eventModel: EventModel? = null
    private var typeInvitation = InvitationModel.InviteType.FRIEND
    private var positionSelected: Int? = null
    private var userSelected: InvitationModel? = null
    private var isBottomMargin = true
    private var isAnimating = false
    private var recyclerViewAttached: RecyclerView? = null


    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(users: ArrayList<InvitationModel>? = null,
                      statuses: ArrayList<EditInvitationVM.UserStatus>? = null,
                      event: EventModel? = null,
                      listener: PlansActionListener? = null,
                      type: InvitationModel.InviteType? = null
    ) {
        listener?.let { this.listener = it}
        event?.let { eventModel = it}
        type?.let { typeInvitation = it}

        if (positionSelected != null && userSelected != null) {
            moveSelectedItem(users, statuses)
        }else {
            updateAll(users, statuses)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll(users: List<InvitationModel>?, statuses: List<EditInvitationVM.UserStatus>?) {
        positionSelected = null
        userSelected = null
        isBottomMargin = true
        isAnimating = false

        users?.let {
            listUsers.clear()
            listUsers.addAll(it)
        }

        statuses?.let {
            listStatus.clear()
            listStatus.addAll(it)
        }

        notifyDataSetChanged()
    }

    private fun moveSelectedItem(usersNew: List<InvitationModel>?, statusesNew: List<EditInvitationVM.UserStatus>?) {
        usersNew?.indexOfFirst {
            when (typeInvitation) {
                InvitationModel.InviteType.FRIEND -> {
                    it.id == userSelected?.id
                }
                InvitationModel.InviteType.CONTACT -> {
                    it.mobile == userSelected?.mobile
                }
                InvitationModel.InviteType.EMAIL -> {
                    it.email == userSelected?.email
                }
                else -> false
            }
        }?.takeIf { it >= 0 && !isAnimating }?.also { toPosition ->
            if (toPosition == positionSelected) {
                updateAll(usersNew, statusesNew)
            }else {
                isAnimating = true
                val newItem = usersNew[toPosition]
                val newStatus = statusesNew?.get(toPosition) ?: listStatus[positionSelected!!]
                listUsers[positionSelected!!] = newItem
                listStatus[positionSelected!!] = newStatus

                notifyItemChanged(positionSelected!!)

                PLANS_APP.currentActivity?.lifecycleScope?.launch(Dispatchers.IO){
                    delay(200)
                    withContext(Dispatchers.Main) {
                        if (isAnimating) {
                            updateLastItem()

                            listUsers.removeAt(positionSelected!!)
                            listStatus.removeAt(positionSelected!!)

                            val newPosition = Integer.min(listUsers.size, toPosition)
                            listUsers.add(newPosition, newItem)
                            listStatus.add(newPosition, newStatus)

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
            updateAll(usersNew, statusesNew)
        }

    }


    private fun updateLastItem(isMargin: Boolean = false) {
        isBottomMargin = isMargin
        listUsers.size.takeIf { it > 0 }?.also {
            notifyItemChanged(it - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<InvitationModel> {
        val itemBinding = CellUserInvitationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserInvitationVH(itemBinding, this)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<InvitationModel>, position: Int) {
        val item = listUsers[position]
        val status = listStatus[position]

        holder.bind(item, status, (position == (listUsers.size - 1)) && isBottomMargin)
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    override fun onItemSelected(position: Int?, data: Any?) {
        positionSelected = position
        userSelected = data as? InvitationModel
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewAttached = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerViewAttached = null
    }


    inner class UserInvitationVH(private val itemBinding: CellUserInvitationBinding,
                                 var listenerMoveListener: PlansItemMoveListener? = null) : BaseViewHolder<InvitationModel>(itemBinding.root) {
        private var userModel : InvitationModel? = null
        private var status : EditInvitationVM.UserStatus? = null

        init {
            itemBinding.apply {
                root.setOnSingleClickListener {
                    listener?.onClickedUser(userModel)
                }
                btnSelect.setOnSingleClickListener{
                    listenerMoveListener?.onItemSelected(layoutPosition, userModel)
                    listener?.onClickedSelect(userModel)
                }
                btnSelected.setOnSingleClickListener{
                    listenerMoveListener?.onItemSelected(layoutPosition, userModel)
                    listener?.onClickedSelected(userModel)
                }
                btnInvited.setOnSingleClickListener{
                    listenerMoveListener?.onItemSelected(layoutPosition, userModel)
                    listener?.onClickedInvited(userModel)
                }
                btnMenu.setOnSingleClickListener{
                    listener?.onClickedMenuInvite(userModel)
                }
            }
        }

        override fun bind(item: InvitationModel?, data: Any?, isLast: Boolean) {
            userModel = item
            status = data as? EditInvitationVM.UserStatus

            updateUI()
            itemView.setLayoutMargin(bottom = if (isLast && isBottomMargin) 150.toPx() else 0)
        }

        private fun updateUI() {
            itemBinding.apply {
                // User Image
                imvUserImage.setUserImage(userModel?.profileImage)

                // User Name
                val name = userModel?.fullName ?: userModel?.name ?: "${userModel?.firstName ?: ""} ${userModel?.lastName ?: ""}"
                tvUserName.visibility = if (name.trim().isEmpty()) View.GONE else {
                    tvUserName.text = name
                    View.VISIBLE
                }

                // Description
                tvDescription.visibility = View.GONE

                // Status Buttons
                if (status != null) {
                    btnSelect.visibility = if (status?.isSelected != false) View.GONE else View.VISIBLE
                    btnSelected.visibility = if (status?.isSelected != true) View.GONE else View.VISIBLE
                    btnInvited.visibility = if (status?.isCrossed == true) View.VISIBLE else View.GONE
                    btnMenu.visibility = if (status?.isTrash == true) View.VISIBLE else View.GONE
                }else {
                    btnSelect.visibility = View.GONE
                    btnSelected.visibility = View.GONE
                    btnInvited.visibility = View.GONE
                }

                when(typeInvitation) {
                    InvitationModel.InviteType.CONTACT -> {
                        val textView = if (name.trim().isEmpty()) tvUserName else tvDescription
                        textView.visibility = if (userModel?.mobile.isNullOrEmpty()) View.GONE else {
                            textView.text = userModel?.mobile
                                ?.formatPhoneNumber(PLANS_APP.currentActivity,
                                    numberFormat = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL,
                                    separator = "-",
                                    isRemovedOwnCountryCode = true)

                            View.VISIBLE
                        }
                        if (userModel?.profileImage.isNullOrEmpty() && userModel?.id.isNullOrEmpty()) {
                            imvUserImage.setUserImage(defaultImage = R.drawable.ic_phone_circle_green)
                        }
                    }
                    InvitationModel.InviteType.EMAIL -> {
                        val textView = if (name.trim().isEmpty()) tvUserName else tvDescription
                        textView.visibility = if (userModel?.email.isNullOrEmpty()) View.GONE else {
                            textView.text = userModel?.email
                            View.VISIBLE
                        }
                        if (userModel?.profileImage.isNullOrEmpty() && userModel?.id.isNullOrEmpty()) {
                            imvUserImage.setUserImage(defaultImage = R.drawable.ic_at_mark_cricle_green)
                        }
                    }
                    else -> {}
                }
            }
        }

    }

}