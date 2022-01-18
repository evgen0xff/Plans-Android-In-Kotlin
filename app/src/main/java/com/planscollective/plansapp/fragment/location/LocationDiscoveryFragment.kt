package com.planscollective.plansapp.fragment.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.facebook.bolts.Task.Companion.delay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.planscollective.plansapp.NavDashboardDirections
import com.planscollective.plansapp.R
import com.planscollective.plansapp.adapters.CategoriesAdapter
import com.planscollective.plansapp.adapters.PlacesAdapter
import com.planscollective.plansapp.constants.Keys
import com.planscollective.plansapp.databinding.FragmentLocationDiscoveryBinding
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.interfaces.OnItemTouchListener
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.dataModels.CategoryModel
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.LocationDiscoveryVM
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import com.planscollective.plansapp.webServices.place.PlaceWebservice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import permissions.dispatcher.ktx.constructPermissionsRequest
import kotlin.math.abs

class LocationDiscoveryFragment : PlansBaseFragment<FragmentLocationDiscoveryBinding>(),
    OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, OnItemTouchListener {

    private val viewModel: LocationDiscoveryVM by viewModels()
    lateinit var args: LocationDiscoveryFragmentArgs
    private var mapFragment: SupportMapFragment? = null
    private var mapGoogle: GoogleMap? = null
    private var adapterPlaces: PlacesAdapter? = null
    private var adapterCategories: CategoriesAdapter? = null
    override var screenName: String? = "Location_Screen"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationDiscoveryBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })

        try {
            args = navArgs<LocationDiscoveryFragmentArgs>().value
            viewModel.typeSelection = LocationSelectionVM.LocationSelectionType.valueOf(args.typeSelection)
        }catch (e: Exception) {
            e.printStackTrace()
        }

        binding.viewModel = viewModel
        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Top bar UI
        binding.btnBack.setOnSingleClickListener(this)
        binding.btnBack.visibility = if(viewModel.typeSelection == LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) View.INVISIBLE else View.VISIBLE
        binding.btnSearch.setOnSingleClickListener(this)

        // Search this area
        binding.layoutSearchThisArea.setOnSingleClickListener(this)

        // Map View
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // My Location Button
        binding.btnMyLocation.setOnSingleClickListener(this)

        // Categories RecyclerView
        adapterCategories = CategoriesAdapter(this)
        binding.pagerCategories.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.pagerCategories.adapter = adapterCategories

        // Guide View
        binding.layoutTutorial.setOnSingleClickListener(this)

        binding.pagerCategories.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.selectedCategory = null
                }
            })


        // Places RecyclerView
        adapterPlaces = PlacesAdapter(this)
        binding.pagerPlaces.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.pagerPlaces.adapter = adapterPlaces

        binding.pagerPlaces.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val place = viewModel.places[position]
                    place.marker?.showInfoWindow()
                    moveCameraTo(place)
                }
            })

        // Select Location
        curBackStackEntry?.savedStateHandle?.getLiveData<PlaceModel>(Keys.SELECTED_LOCATION)?.observe(this){
            if (it != null) {
                viewModel.selectedCategory = null
                updateSearchedPlace(it.latitude?.toDouble(), it.longitude?.toDouble())
                curBackStackEntry?.savedStateHandle?.set(Keys.SELECTED_LOCATION, null)
            }
        }

        updateUI()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    private fun updateUI() {
        _binding?.viewModel = viewModel

        adapterCategories?.updateAdapter(viewModel.categories, this)
        adapterPlaces?.updateAdapter(viewModel.places, this)

        _binding?.apply {
            if (viewModel?.selectedCategory == null){
                pagerCategories.visibility = View.VISIBLE
                pagerPlaces.visibility = View.GONE
            } else {
                pagerPlaces.visibility = if (!viewModel?.places.isNullOrEmpty()){
                    pagerCategories.visibility = View.GONE
                    View.VISIBLE
                } else {
                    pagerCategories.visibility = View.VISIBLE
                    View.GONE
                }
            }
            updateSearchThisAreaUI(mapGoogle?.cameraPosition?.target?.latitude, mapGoogle?.cameraPosition?.target?.longitude)
        }

        updateGuideView()
    }

    private fun updateSearchedPlace(lat: Double?, long: Double?, isAddPinForSearchedPlace: Boolean = true) {
        val latitude = lat ?: return
        val longitude = long ?: return

        // Set the searched Place place on map
        viewModel.searchedPlace = PlaceModel(latitude, longitude)

        // Remove all places searched in past
        removeAllPlaces(isAddPinForSearchedPlace)

        // Move the map camera to the searched place
        moveCameraTo(viewModel.searchedPlace)

        // Fetch City Name of the searched place
        PlaceWebservice.getAddressFrom(latitude, longitude, requireContext()) {
                address, message ->
            if (address != null) {
                viewModel.cityName = address.locality?.takeIf { it.isNotEmpty() } ?: address.adminArea
                binding.tvTitle.text = viewModel.cityName
            }
        }

        // Fetch Places matched in the search area
        if (viewModel.searchedPlace != null){
            viewModel.canMoveCamera = false
            fetchPlaces(viewModel.selectedCategory){
                viewModel.canMoveCamera = true
            }
        }
    }

    private fun updateSearchThisAreaUI(lat: Double?, long: Double?) {
        val latitude = lat ?: return
        val longitude = long ?: return
        val deltaLat = abs(latitude - (viewModel.searchedPlace?.latitude?.toDouble() ?: 0.0))
        val deltaLong = abs(longitude - (viewModel.searchedPlace?.longitude?.toDouble() ?: 0.0))
        _binding?.layoutSearchThisArea?.visibility = if ((deltaLat > 0.001 || deltaLong > 0.001) && viewModel.selectedCategory != null) {
            View.VISIBLE
        }else {
            View.INVISIBLE
        }
    }

    private fun updateGuideView() {
        _binding?.layoutTutorial?.visibility = if (UserInfo.isSeenGuideLocationDiscovery) View.GONE else View.VISIBLE
    }


    private fun fetchPlaces(category: CategoryModel?, complete:(() -> Unit)? = null) {
        val model = category ?: run{
            complete?.also{ it()}
            return
        }

        BusyHelper.show(requireContext())
        PlaceWebservice.fetchPlacesNear(viewModel.searchedPlace?.latitude?.toDouble(), viewModel.searchedPlace?.longitude?.toDouble(), 2000.0, model.types) {
            places, message ->
            BusyHelper.hide()
            if (places != null) {
                addPlaces(places)
                if (places.isEmpty()) {
                    ToastHelper.showMessage("No results found")
                }
            }else {
                ToastHelper.showMessage(message)
            }
            complete?.also{ it()}
        }

    }

    private fun addPlaces(places: ArrayList<PlaceModel>?) {
        removeAllPlaces()
        places?.forEach { place ->
            place.category = viewModel.selectedCategory
            if (place.isValidPlace()) {
                addPin(place)
                viewModel.places.add(place)
            }
        }

        updateUI()

        if (!viewModel.places.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                delay(100)
                withContext(Dispatchers.Main) {
                    _binding?.pagerPlaces?.currentItem = 0
                }
            }
        }
    }

    private fun removeAllPlaces(isAddPinForSearchedPlace: Boolean = true) {
        mapGoogle?.clear()
        if (isAddPinForSearchedPlace) {
            addPinForSearchLocation()
        }
        viewModel.places.clear()

        updateUI()
    }

    private fun addPinForSearchLocation() {
        if (viewModel.searchedPlace?.latitude != null && viewModel.searchedPlace?.longitude != null ) {
            addPin(viewModel.searchedPlace, R.drawable.ic_pin_map_purple_filled)
        }
    }

    private fun addPin(place: PlaceModel?, iconMarker: Int? = null) {
        val location = place?.location ?: return
        val iconId = iconMarker ?: R.drawable.ic_pin_map_green_filled

        val latLng = LatLng(location.latitude, location.longitude)
        place.marker = mapGoogle?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(place.name)
                .icon(BitmapDescriptorFactory.fromResource(iconId))
        )
    }


    private fun moveCameraTo(place: PlaceModel? = null, latLng: LatLng? = null) {
        if (!viewModel.canMoveCamera) return

        val location = latLng ?: place?.location?.let { LatLng(it.latitude, it.longitude) } ?: return
        mapGoogle?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun gotoPlaceDetails(place: PlaceModel?,
                                 typeSelection: LocationSelectionVM.LocationSelectionType = LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) {
        val placeId = place?.place_id?.takeIf { it.isNotEmpty() } ?: return
        val action = NavDashboardDirections.actionGlobalPlaceDetailsFragment(placeId)
        action.placeModel = place
        action.typeSelection = typeSelection.name

        navigate(directions = action)
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnSearch -> {
                gotoLocationSelection()
            }
            binding.btnMyLocation -> {
                updateSearchedPlace(UserInfo.latitude, UserInfo.longitude)
            }
            binding.layoutSearchThisArea -> {
                updateSearchedPlace(mapGoogle?.cameraPosition?.target?.latitude, mapGoogle?.cameraPosition?.target?.longitude)
            }
            binding.layoutTutorial -> {
                UserInfo.isSeenGuideLocationDiscovery = true
                updateGuideView()
            }
        }
    }


    //********************************* Google Map Listeners **************************************//
    override fun onMapReady(googleMap: GoogleMap) {
        mapGoogle = googleMap
        mapGoogle?.uiSettings?.isMyLocationButtonEnabled = false
        mapGoogle?.setOnMarkerClickListener(this)
        mapGoogle?.setOnCameraIdleListener(this)

        if (!viewModel.isMapReady) {
            viewModel.isMapReady = true
            updateSearchedPlace(UserInfo.latitude, UserInfo.longitude, false)
        }

        val action = {
            mapGoogle?.isMyLocationEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            constructPermissionsRequest(Manifest.permission.ACCESS_FINE_LOCATION) {
                constructPermissionsRequest(Manifest.permission.ACCESS_COARSE_LOCATION){
                    action()
                }.launch()
            }.launch()
        }else {
            action()
        }
    }

    override fun onCameraIdle() {
        if (_binding != null) {
            updateSearchThisAreaUI(mapGoogle?.cameraPosition?.target?.latitude, mapGoogle?.cameraPosition?.target?.longitude)
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        viewModel.places.indexOfFirst { it.marker == marker }.takeIf { it >= 0 }?.also {
            binding.pagerPlaces.currentItem = it
        }
        return false
    }


    //********************************* OnItemTouchListener **************************************//
    override fun onItemClick(position: Int, data: Any?) {
        when(data) {
            is CategoryModel -> {
                viewModel.selectedCategory = viewModel.categories[position]
                fetchPlaces(viewModel.selectedCategory)
            }
            is PlaceModel -> {
                viewModel.selectedPlace = viewModel.places[position]
                gotoPlaceDetails(viewModel.selectedPlace, viewModel.typeSelection)
            }
        }
    }

    override fun onItemSwipeDown(position: Int, data: Any?) {
        when(data) {
            is PlaceModel -> {
                binding.apply {
                    viewModel?.selectedCategory = null
                    updateUI()
                }
            }
            else -> {}
        }

    }


}