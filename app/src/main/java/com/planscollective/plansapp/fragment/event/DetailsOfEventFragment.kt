package com.planscollective.plansapp.fragment.event

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.planscollective.plansapp.R
import com.planscollective.plansapp.constants.PlansColor
import com.planscollective.plansapp.databinding.FragmentDetailsOfEventBinding
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.DetailsOfEventVM
import permissions.dispatcher.ktx.constructPermissionsRequest

class DetailsOfEventFragment : PlansBaseFragment<FragmentDetailsOfEventBinding>(),
    OnMapReadyCallback {

    private val viewModel : DetailsOfEventVM by viewModels()
    private val args: DetailsOfEventFragmentArgs by navArgs()
    private val viewsSeparators = ArrayList<View>()
    private var mapFragment : SupportMapFragment? = null
    private var mapGoogle: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsOfEventBinding.inflate(inflater, container, false)
        viewModel.eventId = args.eventId
        viewModel.didLoadData.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
        binding.viewModel = viewModel

        return binding.root
    }


    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener(this)

        // Details
        binding.layoutDetails.visibility = View.GONE
        binding.layoutDirection.visibility = View.GONE
        binding.btnMyLocation.setOnSingleClickListener(this)
        binding.btnDirection.setOnSingleClickListener(this)

        // Map View
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Loading Data from server
        refreshAll()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
            viewModel.getAllList(isShownLoading)
        }
        return isBack
    }

    private fun updateUI() {
        saveInfo(viewModel.eventId, viewModel.eventModel?.userId)

        binding.viewModel = viewModel

        viewsSeparators.clear()
        binding.apply {
            // Event Caption
            layoutCaption.visibility = if (viewModel?.eventModel?.caption.isNullOrEmpty()) View.GONE else {
                viewSeparatorCaption.visibility = View.VISIBLE
                viewsSeparators.add(viewSeparatorCaption)
                tvCaption.text = viewModel?.eventModel?.caption
                View.VISIBLE
            }

            // Event Description
            layoutDescription.visibility = if (viewModel?.eventModel?.details.isNullOrEmpty()) View.GONE else {
                viewSeparatorDescription.visibility = View.VISIBLE
                viewsSeparators.add(viewSeparatorDescription)
                tvDescription.text = viewModel?.eventModel?.details
                View.VISIBLE
            }

            // Event Location Name
            val address = viewModel?.eventModel?.address.takeIf { !it.isNullOrEmpty() }
            val locationName = viewModel?.eventModel?.locationName.takeIf { !it.isNullOrEmpty() }
            layoutLocationName.visibility = if (!address.isNullOrEmpty() && !locationName.isNullOrEmpty() && address.subSequence(0, locationName.length) != locationName) {
                viewSeparatorLocationName.visibility = View.VISIBLE
                viewsSeparators.add(viewSeparatorLocationName)
                tvLocationName.text = locationName.removeOwnCountry()
                View.VISIBLE
            }else View.GONE

            // Event Location Address
            tvLocationAddress.text = (address ?: locationName ?: "TBD").removeOwnCountry()
            viewSeparatorLocationAddress.visibility = View.VISIBLE
            viewsSeparators.add(viewSeparatorLocationAddress)
            layoutLocationAddress.visibility = View.VISIBLE

            layoutDetails.visibility = if (viewsSeparators.size > 0) View.VISIBLE else View.GONE
            viewsSeparators.lastOrNull()?.visibility = View.GONE

        }

        updateMapView()
    }

    private fun updateMapView() {

        binding.apply {
            layoutDirection.visibility = View.VISIBLE
            layoutMap.visibility = View.INVISIBLE
            btnDirection.visibility = View.INVISIBLE
            btnMyLocation.visibility = View.INVISIBLE

            val lat = viewModel?.eventModel?.lat?.toDouble() ?: return
            val long = viewModel?.eventModel?.long?.toDouble() ?: return
            val range = viewModel?.eventModel?.checkInRange?.toDouble() ?: return
            if (lat == 0.0 && long == 0.0) return

            layoutMap.visibility = View.VISIBLE
            btnDirection.visibility = View.VISIBLE
            btnMyLocation.visibility = View.VISIBLE

            mapGoogle?.uiSettings?.isMyLocationButtonEnabled = false
            mapGoogle?.clear()

            val locationEvent = LatLng(lat, long)

            mapGoogle?.addCircle(
                CircleOptions()
                    .center(locationEvent)
                    .radius(range)
                    .fillColor(PlansColor.TEAL_MAP_CIRCLE)
                    .strokeWidth(0f)
            )

            mapGoogle?.addMarker(
                MarkerOptions()
                    .position(locationEvent)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_map_purple_filled))
            )

            mapGoogle?.moveCamera(CameraUpdateFactory.newLatLngZoom(locationEvent, 15f))

        }
    }

    private fun moveToMyLocation() {
        val myLocation = LatLng(UserInfo.latitude, UserInfo.longitude)
        mapGoogle?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
    }

    //********************************* OnSingleClickListener **************************************//
    override fun onSingleClick(v: View?) {
        when(v) {
            binding.btnBack -> {
                gotoBack()
            }
            binding.btnMyLocation -> {
                moveToMyLocation()
            }
            binding.btnDirection -> {
                gotoGoogleMap(viewModel.eventModel)
            }
        }
    }

    //********************************* OnMapReadyCallback **************************************//
    override fun onMapReady(googleMap: GoogleMap) {
        mapGoogle = googleMap
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

}