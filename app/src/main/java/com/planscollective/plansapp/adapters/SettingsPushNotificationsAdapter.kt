package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.databinding.CellSettingOptionBinding
import com.planscollective.plansapp.interfaces.PlansActionListener
import com.planscollective.plansapp.models.dataModels.SettingOptionModel
import com.planscollective.plansapp.viewholders.BaseViewHolder

class SettingsPushNotificationsAdapter : RecyclerView.Adapter<SettingsPushNotificationsAdapter.OptionVH>() {

    private var listOptions = ArrayList<SettingOptionModel>()
    private var listener: PlansActionListener? = null
    private var isActive = true

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: ArrayList<SettingOptionModel>? = null,
                      listener: PlansActionListener? = null,
                      isActive: Boolean = true
    ) {
        list?.also{
            listOptions.clear()
            listOptions.addAll(it)
        }

        listener?.also {
            this.listener = it
        }
        this.isActive = isActive

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionVH {
        val itemBinding = CellSettingOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionVH(itemBinding)
    }

    override fun onBindViewHolder(holder: OptionVH, position: Int) {
        holder.bind(listOptions[position], isLast = position == (listOptions.size - 1))
    }

    override fun getItemCount(): Int {
        return listOptions.size
    }


    inner class OptionVH(var itemBinding: CellSettingOptionBinding) : BaseViewHolder<SettingOptionModel>(itemBinding.root) {
        var option : SettingOptionModel? = null

        init {
            itemBinding.apply {
                switchOption.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked != option?.status) {
                        listener?.onChangedSettingOption(option, isChecked)
                    }
                }
            }
        }

        override fun bind(item: SettingOptionModel?, data: Any?, isLast: Boolean) {
            option = item
            itemBinding.apply {
                switchOption.text = option?.name
                switchOption.isChecked = option?.status ?: false
                switchOption.isClickable = isActive
            }
        }
    }

}