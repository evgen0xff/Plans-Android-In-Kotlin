package com.planscollective.plansapp.fragment.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hbb20.CCPCountry
import com.hbb20.CountryCodePicker
import com.planscollective.plansapp.adapters.SingleItemAdapter
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.databinding.FragmentCountryCodeListBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.AuthBaseFragment
import com.planscollective.plansapp.interfaces.OnItemTouchListener

class CountryCodeListFragment : AuthBaseFragment<FragmentCountryCodeListBinding>(), OnItemTouchListener {

    lateinit var listCountry : List<CCPCountry>
    var searchedCountries: List<CCPCountry>? = null
    private val adapterItems = SingleItemAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCountryCodeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeData() {
        super.initializeData()
        listCountry = CCPCountry.getLibraryMasterCountryList(activity, CountryCodePicker.Language.ENGLISH)
    }

    override fun setupUI() {
        super.setupUI()

        // Back Button

        binding.btnBack.setOnSingleClickListener {
            gotoBack()
        }

        // Search EditText
        binding.etSearch.addTextChangedListener {
            val textSearch = binding.etSearch.text.toString().trim()
            actionSearch(textSearch)
        }

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapterItems
        adapterItems.listener = this

        updateUI()
    }

    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        searchedCountries?.get(position)?.apply {
            val selectedCountry = "$nameCode +$phoneCode"
            preBackStackEntry?.savedStateHandle?.set(Keys.SELECTED_COUNTRY, selectedCountry)
            gotoBack()
        }
    }

    private fun updateUI() {
        val textSearch = binding.etSearch.text.toString().trim()
        actionSearch(textSearch)
    }

    private fun actionSearch(search: String) {
        searchedCountries = if (search.isNotEmpty()) {
            listCountry.filter {
                return@filter it.name.toLowerCase().contains(search.toLowerCase())
            }
        }else {
            listCountry
        }

        val listCountryNames = searchedCountries?.map {
            return@map it.name
        } ?: listOf()

        updateList(listCountryNames)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(list: List<String>){
        adapterItems.items.clear()
        adapterItems.items.addAll(list)
        adapterItems.notifyDataSetChanged()
    }


}