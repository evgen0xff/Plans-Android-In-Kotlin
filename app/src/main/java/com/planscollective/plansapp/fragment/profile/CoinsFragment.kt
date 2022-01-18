package com.planscollective.plansapp.fragment.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.CellCoinBinding
import com.planscollective.plansapp.databinding.FragmentCoinsBinding
import com.planscollective.plansapp.extension.setLayoutSize
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.extension.toPx
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.OSHelper
import com.planscollective.plansapp.models.viewModels.CoinsVM
import com.planscollective.plansapp.viewholders.BaseViewHolder

class CoinsFragment : PlansBaseFragment<FragmentCoinsBinding> () {

    private val viewModel : CoinsVM by viewModels()
    private val args: CoinsFragmentArgs by navArgs()
    private val adapter = CoinsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCoinsBinding.inflate(inflater, container, false)

        viewModel.userId = args.userId
        viewModel.user = args.userModel

        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()
        binding.apply {
            btnBack.setOnSingleClickListener(this@CoinsFragment)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            recyclerView.adapter = adapter
        }
        refreshAll(false)
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getUserInfo(isShownLoading)
        }
        return isBack
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.viewModel = viewModel
        binding.apply {
            viewModel?.userName?.takeIf{it.isNotBlank()}?.also {
                tvTitle.text = "$it's Badges"
            }
        }
        adapter.notifyDataSetChanged()
    }


    //****************************************** OnSingleClickListener ****************************//

    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
            }
        }
    }

    //************************************** Coin List Adapter *********************************//
    inner class CoinsAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = CellCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(viewModel.arrayStars[position])
        }

        override fun getItemCount(): Int {
            return viewModel.arrayStars.size
        }
    }

    //************************************** View Holder  *********************************//
    inner class ViewHolder(var itemBinding: CellCoinBinding) : BaseViewHolder<Int>(itemBinding.root) {

        init {
            val width = (OSHelper.widthScreen - 12.toPx()) / 3.0
            itemView.setLayoutSize(width.toInt(), width.toInt())
        }

        override fun bind(item: Int?, data: Any?, isLast: Boolean) {
            val resId = item ?: R.drawable.ic_lock_circle_green_lagre
            itemBinding.apply {
                imvStar.setImageResource(resId)
            }
        }
    }

}