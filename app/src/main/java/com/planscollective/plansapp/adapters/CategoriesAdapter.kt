package com.planscollective.plansapp.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.planscollective.plansapp.databinding.CellCategoryBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.models.dataModels.CategoryModel

class CategoriesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var listCategories = ArrayList<CategoryModel>()
    private var listener: OnItemTouchListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(
        places: ArrayList<CategoryModel>? = null,
        listener: OnItemTouchListener? = null
    ) {
        places?.also {
            listCategories.clear()
            listCategories.addAll(it)
        }
        listener?.also { this.listener = it }
        notifyDataSetChanged()
    }

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment.getInstance(listCategories[position], position, listener)
    }

    override fun getItemCount(): Int {
        return listCategories.size
    }
}

class CategoryFragment(var category: CategoryModel?, var index: Int, var listener: OnItemTouchListener? = null) : Fragment() {
    lateinit var binding: CellCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CellCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        updateUI(category)
    }

    override fun onResume() {
        super.onResume()
        updateUI(category)
    }

    fun setupUI() {
        binding.root.setOnSingleClickListener {
            listener?.onItemClick(index, category)
        }
    }

    fun updateUI(category: CategoryModel?) {
        val item = category ?: return
        binding.apply {
            item.defaultImage?.also {
                imgCategoryIcon.setImageResource(it)
            }
            tvCategoryName.text = item.name
        }
    }

    companion object {
        fun getInstance(category: CategoryModel?, index: Int, listener: OnItemTouchListener? = null) : CategoryFragment {
            return CategoryFragment(category, index, listener)
        }
    }
}
