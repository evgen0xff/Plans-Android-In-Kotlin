package com.planscollective.plansapp.fragment.utils

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.planscollective.plansapp.adapters.MenuItemAdapter
import com.planscollective.plansapp.databinding.FragmentPlansBottomDialogBinding
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.interfaces.OnSelectedMenuItem
import com.planscollective.plansapp.models.dataModels.MenuModel

class PlansBottomDialogFragment(
    var items: ArrayList<MenuModel>? = null,
    var listenerMenuItem: OnSelectedMenuItem? = null,
    var data: Any? = null
) : BottomSheetDialogFragment(), OnItemTouchListener {

    companion object {
        const val  TAG = "PlansBottomDialogFragment"
    }

    private var binding: FragmentPlansBottomDialogBinding? = null
    private val adapterItems = MenuItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlansBottomDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding?.recyclerView?.layoutManager = layoutManager
        items?.apply {
            adapterItems.items = this
        }
        binding?.recyclerView?.adapter = adapterItems
        adapterItems.listener = this
    }

    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        dismiss()
        if (position < (items?.size ?: 0)) {
            listenerMenuItem?.onSelectedMenuItem(position, items?.get(position), data)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}