package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.models.dataModels.CategoryModel
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM

class LocationDiscoveryVM(application: Application) : ListBaseVM(application) {

    var searchedPlace : PlaceModel? = null
    var selectedPlace : PlaceModel? = null

    var typeSelection = LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY

    var places = ArrayList<PlaceModel>()
    var canMoveCamera = true

    var selectedCategory: CategoryModel? = null
    var categories = CategoryModel.plansCategories

    var isMapReady = false
    var cityName = ""

    override fun getList(
                         isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int) {

    }

}