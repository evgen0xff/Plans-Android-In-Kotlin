package com.planscollective.plansapp.fragment.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.planscollective.plansapp.R
import com.planscollective.plansapp.databinding.FragmentPlaceDetailsBinding
import com.planscollective.plansapp.extension.getDistance
import com.planscollective.plansapp.extension.removeOwnCountry
import com.planscollective.plansapp.extension.setOnSingleClickListener
import com.planscollective.plansapp.fragment.base.PlansBaseFragment
import com.planscollective.plansapp.manager.UserInfo
import com.planscollective.plansapp.models.viewModels.LocationSelectionVM
import com.planscollective.plansapp.models.viewModels.PlaceDetailsVM
import com.planscollective.plansapp.webServices.place.PlaceWebservice

class PlaceDetailsFragment : PlansBaseFragment<FragmentPlaceDetailsBinding>() {

    private var vmLocationSelection: LocationSelectionVM? = null
    private val args : PlaceDetailsFragmentArgs by navArgs()
    private val viewModel : PlaceDetailsVM by viewModels()
    private val arrayItems = ArrayList<View>()
    private val arrayItemsSeparator = ArrayList<View>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPlaceDetailsBinding.inflate(inflater, container, false)
        viewModel.didLoadData.observe(viewLifecycleOwner, {
            updateUI()
        })

        viewModel.typeSelection = LocationSelectionVM.LocationSelectionType.valueOf(args.typeSelection)
        viewModel.placeId = args.placeId
        viewModel.placeModel = args.placeModel
        binding.viewModel = viewModel

        try {
            vmLocationSelection = navGraphViewModels<LocationSelectionVM>(R.id.locationSelectionFragment).value
        }catch (e : Exception) {
            e.printStackTrace()
        }

        return binding.root
    }

    override fun setupUI() {
        super.setupUI()

        // Back button
        binding.btnBack.setOnSingleClickListener (this)
        binding.btnCreateEvent.setOnSingleClickListener(this)
        binding.layoutPlaceAddress.setOnSingleClickListener(this)
        binding.layoutPlacePhone.setOnSingleClickListener(this)
        binding.layoutPlaceWebSite.setOnSingleClickListener(this)

        binding.apply {
            arrayItems.clear()
            arrayItemsSeparator.clear()
            arrayItems.addAll(arrayOf(layoutPlaceAddress, layoutPlacePhone, layoutPlaceTime, layoutPlaceRating, layoutPlaceWebSite))
            arrayItemsSeparator.addAll(arrayOf(separatorPlaceAddress, separatorPlacePhone, separatorPlaceTime, separatorPlaceRating, separatorPlaceWebSite))
        }

        // Refresh All
        if (viewModel.placeModel?.gmsPlace == null) {
            viewModel.getPlaceDetails()
        }

        updateUI()
    }

    override fun refreshAll(isShownLoading: Boolean, isUpdateLocation: Boolean): Boolean {
        val isBack = super.refreshAll(isShownLoading, isUpdateLocation)
        if (!isBack) {
        }
        return isBack
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        binding.viewModel = viewModel
        val placeModel = viewModel.placeModel ?: return
        binding.apply {
            // Create Event button
            tvCreateEvent.text = if (viewModel?.typeSelection == LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) "CREATE EVENT" else "SELECT"

            // Place Cover Image
            layoutImageNotAvailable.visibility = View.INVISIBLE
            imvPlaceCover.visibility = View.INVISIBLE
            if (placeModel.photoImage != null) {
                imvPlaceCover.setImageBitmap(placeModel.photoImage)
                imvPlaceCover.visibility = View.VISIBLE
                layoutImageNotAvailable.visibility = View.INVISIBLE
            }else {
                PlaceWebservice.getPlacePhoto(placeModel.place_id) { image, message ->
                    if (image != null) {
                        placeModel.photoImage = image
                        imvPlaceCover.setImageBitmap(image)
                        imvPlaceCover.visibility = View.VISIBLE
                        layoutImageNotAvailable.visibility = View.INVISIBLE
                    }else {
                        imvPlaceCover.visibility = View.INVISIBLE
                        layoutImageNotAvailable.visibility = View.VISIBLE
                    }
                }
            }

            // Place Name
            tvPlaceName.text = placeModel.name

            // Distance Miles
            tvPlaceMile.text = placeModel.location?.getDistance(UserInfo.latitude, UserInfo.longitude)?.let {
                if (it >= 2) "%.2f".format(it) + " Miles" else "%.2f".format(it) + " Mile"
            }

            // Types
            tvPlaceTypes.visibility = placeModel.getFormattedTypes()?.takeIf { it.isNotEmpty() }?.let {
                tvPlaceTypes.text = it
                View.VISIBLE
            } ?: View.GONE

            // Address
            layoutPlaceAddress.visibility = (placeModel.gmsPlace?.address ?: placeModel.address)?.takeIf { it.isNotEmpty() }?.let{
                tvPlaceAddress.text = it.removeOwnCountry()
                placeModel.formattedAddress = it
                View.VISIBLE
            } ?: View.GONE

            // Phone Number
            layoutPlacePhone.visibility = placeModel.gmsPlace?.phoneNumber?.takeIf { it.isNotEmpty() }?.let{
                tvPlacePhone.text = it
                placeModel.phoneNumber = it
                View.VISIBLE
            } ?: View.GONE

            // Open Hours
            layoutPlaceTime.visibility = placeModel.getOpenString()?.takeIf { it.isNotEmpty() }?.let{
                tvPlaceTime.text = it
                View.VISIBLE
            } ?: View.GONE

            // Rating Score
            layoutPlaceRating.visibility = placeModel.gmsPlace?.rating?.takeIf { it > 0 }?.let{
                tvPlaceRating.text = "%.1f".format(it)
                View.VISIBLE
            } ?: View.GONE

            // Rating Score
            layoutPlaceWebSite.visibility = placeModel.gmsPlace?.websiteUri?.let{
                tvPlaceWebSite.text = it.host
                placeModel.website = it.toString()
                View.VISIBLE
            } ?: View.GONE

            // Separators
            arrayItemsSeparator.forEach { it.visibility = View.VISIBLE }
            arrayItems.indexOfLast { it.visibility == View.VISIBLE }.takeIf { it >= 0 }?.also {
                arrayItemsSeparator[it].visibility = View.GONE
            }
        }
    }

    override fun onSingleClick(v: View?) {
        binding.apply {
            when(v) {
                btnBack -> {
                    gotoBack()
                }
                btnCreateEvent -> {
                    if (viewModel?.typeSelection == LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY) {
                        gotoCreateEvent(viewModel?.placeModel)
                    }else {
                        vmLocationSelection?.placeSelected?.value = viewModel?.placeModel
                    }
                }
                layoutPlaceAddress -> {
                    gotoGoogleMap(viewModel?.placeModel?.latitude?.toDouble(),
                        viewModel?.placeModel?.longitude?.toDouble(),
                        viewModel?.placeModel?.formattedAddress
                    )
                }
                layoutPlacePhone -> {
                    gotoPhoneCall(viewModel?.placeModel?.gmsPlace?.phoneNumber)
                }
                layoutPlaceWebSite -> {
                    gotoOpenUrl(viewModel?.placeModel?.gmsPlace?.websiteUri?.toString())
                }

            }
        }
    }

}