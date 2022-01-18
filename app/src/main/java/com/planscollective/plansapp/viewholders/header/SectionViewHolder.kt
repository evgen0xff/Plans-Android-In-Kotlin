package com.planscollective.plansapp.viewholders.header

import com.planscollective.plansapp.databinding.CellSectionHeaderBinding
import com.planscollective.plansapp.viewholders.BaseViewHolder

class SectionViewHolder(
    private val itemBinding: CellSectionHeaderBinding
) : BaseViewHolder<String>(itemBinding.root) {

    override fun bind(item: String?, data: Any?, isLast: Boolean) {
        itemBinding.apply {
            tvHeader.text = item
        }
    }
}