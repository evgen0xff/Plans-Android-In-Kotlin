package com.planscollective.plansapp.models.viewModels

import android.app.Application
import com.planscollective.plansapp.helper.BusyHelper
import com.planscollective.plansapp.helper.ToastHelper
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.models.viewModels.base.PlansBaseVM
import com.planscollective.plansapp.webServices.place.PlaceWebservice

class PlaceDetailsVM(application: Application) : PlansBaseVM(application) {
    var placeId: String? = null
    var placeModel : PlaceModel? = null
    var typeSelection = LocationSelectionVM.LocationSelectionType.LOCATION_DISCOVERY

    fun getPlaceDetails() {
        val place_Id = placeId?.takeIf{it.isNotEmpty()} ?: return

        BusyHelper.show(context)
        PlaceWebservice.getPlaceDetails(place_Id){
                gmsPlace, message ->
            BusyHelper.hide()
            if (gmsPlace != null) {
                placeModel?.gmsPlace = gmsPlace
            }else {
                ToastHelper.showMessage(message)
            }
            didLoadData.value = true
        }
    }


}