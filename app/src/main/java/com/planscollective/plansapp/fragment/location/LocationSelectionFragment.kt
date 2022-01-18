package com.planscollective.plansapp.fragment.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.LocationItemAdapter
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.databinding.FragmentLocationSelectionBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import com.planscollective.plansapp.webServices.place.PlaceWebservice

class LocationSelectionFragment : PlansBaseFragment<FragmentLocationSelectionBinding>(), OnItemTouchListener{

    private val viewModel: LocationSelectionVM by navGraphViewModels(R.id.locationSelectionFragment)
    private val adapterItems = LocationItemAdapter()
    private val args : LocationSelectionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLocationSelectionBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            updateUI()
        })
        viewModel.placeSelected.observe(requireActivity(), {
            selectLocation(it)
        })

        viewModel.actionEnterKey = actionEnterKey
        viewModel.typeSelection = LocationSelectionVM.LocationSelectionType.valueOf(args.typeSelection)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener (this)

        // Search UI
        binding.etSearch.addTextChangedListener {
            refreshAll(false)
        }

        // Current Location UI
        binding.layoutCurrentLocation.setOnSingleClickListener(this)

        // Event List
        val layoutMangerList = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutMangerList
        binding.recyclerView.adapter = adapterItems

        // Find Place UI
        binding.btnFindPlaces.setOnSingleClickListener(this)

        // Refresh All
        refreshAll(false)
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    private fun updateUI() {
        adapterItems.updateAdapter(viewModel.listItems, this)

        binding.apply {
            layoutEmpty.visibility = when (viewModel?.typeSelection) {
                LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY -> View.GONE
                else -> {
                    if (viewModel?.listPlaces.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun getPlaceDetails(placeId: String?) {
        val place_Id = placeId?.takeIf{it.isNotEmpty()} ?: return
        BusyHelper.show(requireContext())
        PlaceWebservice.getPlaceDetails(place_Id){
            gmsPlace, message ->
            BusyHelper.hide()
            if (gmsPlace != null) {
                val place = PlaceModel(gmsPlace)
                viewModel.placeSelected.value = place
            }else {
                ToastHelper.showMessage(message)
            }
        }
    }

    private fun selectLocation(place: PlaceModel?) {
        preBackStackEntry?.savedStateHandle?.set(Keys.SELECTED_LOCATION, place)
        gotoBack(R.id.locationSelectionFragment, true)
    }

    private fun actionCurrentLocation() {
        viewModel.placeSelected.value = UserInfo.userPlace
    }

    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.layoutCurrentLocation -> {
                actionCurrentLocation()
            }
            binding.btnFindPlaces -> {
                gotoFindPlaces(viewModel.typeSelection)
            }
        }
    }

    override fun onItemClick(holder: RecyclerView.ViewHolder?, view: View?, position: Int) {
        hideKeyboard()
        if (position < viewModel.listPlaces.size) {
            val place = viewModel.listPlaces[position]
            getPlaceDetails(place.placeId)
        }
    }

}