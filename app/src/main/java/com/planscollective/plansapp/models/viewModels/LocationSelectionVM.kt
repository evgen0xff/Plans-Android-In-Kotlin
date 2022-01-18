package com.planscollective.plansapp.models.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.planscollective.plansapp.extension.toArrayList
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.base.ListBaseVM
import com.planscollective.plansapp.webServices.place.PlaceWebservice

class LocationSelectionVM(application: Application) : ListBaseVM(application) {

    enum class LocationSelectionType {
        LOCATION_DISCOVERY,
        CREATE_EVENT,
        EDIT_EVENT,
    }

    var listPlaces = ArrayList<AutocompletePrediction>()
    val listItems : ArrayList<String>
        get() {
            return listPlaces.map { it.getFullText(null).toString() }.toArrayList()
        }

    var typeSelection = LocationSelectionType.LOCATION_DISCOVERY

    var placeSelected = MutableLiveData<PlaceModel>()

    override fun getList(isShownLoading: Boolean,
                         pageNumber: Int,
                         count: Int
    ) {
        PlaceWebservice.findPredictions(keywordSearch){
            list, message ->
            if (list != null){
                updateData(list)
            }
            didLoadData.value = true
        }
    }

    override fun getNextPage(context: Context, isShownLoading: Boolean) {
        super.getNextPage(context, isShownLoading)
    }

    private fun updateData(list: List<AutocompletePrediction>?){
        listPlaces.clear()
        list?.takeIf { it.isNotEmpty() }?.also { listPlaces.addAll(it) }
    }

}