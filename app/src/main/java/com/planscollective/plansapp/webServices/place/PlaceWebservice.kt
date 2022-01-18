package com.planscollective.plansapp.webServices.place

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import com.planscollective.plansapp.PLANS_APP
import com.planscollective.plansapp.constants.ConstantTexts
import com.planscollective.plansapp.constants.Constants
import com.planscollective.plansapp.constants.Urls
import com.planscollective.plansapp.models.dataModels.PlaceModel
import com.planscollective.plansapp.webServices.base.BaseWebService
import com.planscollective.plansapp.webServices.base.WebServiceBuilder
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object PlaceWebservice : BaseWebService() {
    var placesClient = PLANS_APP.currentActivity?.let { Places.createClient(it) }
    var webservice = WebServiceBuilder.buildService(PlaceAPIs::class.java, Urls.BASE_URL_GOOGLE_MAP_API)

    var indexType = 0
    var places = ArrayList<PlaceModel>()
    var placesTemp = ArrayList<PlaceModel>()

    fun findPredictions(
        search: String?,
        complete: ((list: List<AutocompletePrediction>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(search)
            .build()

        placesClient?.findAutocompletePredictions(request)?.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
            complete?.also { it(response.autocompletePredictions, null) }
        }?.addOnFailureListener { exception: Exception? ->
            complete?.also{ it(null, exception?.message)}
        }
    }

    fun getPlaceDetails(
        placeId: String?,
        complete: ((gmsPlace: Place?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val place_Id = placeId?.takeIf { it.isNotEmpty() } ?: run {
            complete?.also { it(null, "Failed to get the place details by invalid placeId") }
            return
        }

        val placeFields = listOf(
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME,
            Place.Field.OPENING_HOURS,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.PLUS_CODE,
            Place.Field.PRICE_LEVEL,
            Place.Field.RATING,
            Place.Field.TYPES,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.UTC_OFFSET,
            Place.Field.VIEWPORT,
            Place.Field.WEBSITE_URI
        )

        val request = FetchPlaceRequest.newInstance(place_Id, placeFields)

        placesClient?.fetchPlace(request)?.addOnSuccessListener { response: FetchPlaceResponse ->
            complete?.also { it(response.place, null) }
        }?.addOnFailureListener { exception: Exception ->
            complete?.also { it(null, exception.message) }
        }
    }

    fun getPlacePhoto(
        placeId: String?,
        complete: ((image: Bitmap?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        getPlaceDetails(placeId) { gmsPlace, message ->
            if (gmsPlace != null) {
                gmsPlace.photoMetadatas?.firstOrNull()?.also {
                    // Create a FetchPhotoRequest.
                    val photoRequest = FetchPhotoRequest.builder(it)
//                        .setMaxWidth(500) // Optional.
//                        .setMaxHeight(300) // Optional.
                        .build()
                    placesClient?.fetchPhoto(photoRequest)?.addOnSuccessListener { fetchPhotoResponse: FetchPhotoResponse ->
                        complete?.also { it(fetchPhotoResponse.bitmap, null) }
                    }?.addOnFailureListener { exception: Exception ->
                        complete?.also { it(null, exception.message) }
                    }
                } ?: run {
                    complete?.also { it(null, "No photo") }
                }
            }else {
                complete?.also { it(null, message) }
            }
        }
    }

    fun getAddressFrom(
        lat: Double?,
        long: Double?,
        context: Context? = null,
        complete: ((address: Address?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        var address : Address? = null
        var message: String? = null

        val latitude = lat ?: run {
            complete?.also { it(address, "Invalid params") }
            return
        }
        val longitude = long ?: run {
            complete?.also { it(address, "Invalid params") }
            return
        }

        val ctx = context ?: PLANS_APP
        val geocoder = Geocoder(ctx, Locale.getDefault())

        GlobalScope.launch(Dispatchers.IO){
            try {
                val listAddress = geocoder.getFromLocation(latitude, longitude, 1)
                address = listAddress.firstOrNull()
            }catch (e : Exception) {
                e.printStackTrace()
                message = e.message
            }
            withContext(Dispatchers.Main){
                complete?.also { it(address, message) }
            }
        }
    }

    fun fetchPlacesNear(
        lat: Double?,
        long: Double?,
        radius: Double = 5000.0,
        types: ArrayList<String>? = null,
        keyword: String? = null,
        complete: ((places: ArrayList<PlaceModel>?, message: String?) -> Unit)? = null
    ){
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        if (indexType == 0) {
            places.clear()
        }

        if (!types.isNullOrEmpty() && indexType < types.size ) {
            fetchPlacesNearWithType(lat, long, radius, types[indexType], keyword) { places, message ->
                if (!message.isNullOrEmpty()) {
                    this.indexType = 0
                    this.places.clear()
                    complete?.also { it(null, message) }
                }else {
                    places?.forEach { new ->
                        if (!this.places.any{it.place_id == new.place_id}) {
                            this.places.add(new)
                        }
                    }
                    this.indexType += 1
                    if (this.indexType < types.size) {
                        fetchPlacesNear(lat, long, radius, types, keyword, complete)
                    }else {
                        complete?.also { it(this.places, null) }
                        this.indexType = 0
                        this.places.clear()
                    }
                }
            }
        }else {
            fetchPlacesNearWithType(lat, long, radius, keyword = keyword, complete = complete)
        }
    }

    fun fetchPlacesNearWithType(
        lat: Double? = null,
        long: Double? = null,
        radius: Double? = null,
        type: String? = null,
        keyword: String? = null,
        nextPage: String? = null,
        complete: ((places: ArrayList<PlaceModel>?, message: String?) -> Unit)? = null
    ) {
        if(!isNetworkConnected()) {
            complete?.also { it(null, ConstantTexts.NO_INTERNET) }
            return
        }

        val options = mutableMapOf<String, String>()

        options["key"] = Constants.GOOGLE_API_KEY
        options["rankby"] = "prominence"

        if (lat != null && long != null) {
            options["location"] = "$lat,$long"
        }
        if (radius != null) {
            options["radius"] = "$radius"
        }
        if (!type.isNullOrEmpty()) {
            options["type"] = type
        }
        if (!keyword.isNullOrEmpty()){
            options["keyword"] = keyword
        }
        if (!nextPage.isNullOrEmpty()){
            options["pagetoken"] = nextPage
        } else {
            placesTemp.clear()
        }

        webservice.getPlacesNearBy(options)?.enqueue(object : Callback<PlaceResponseModel> {
            override fun onResponse(
                call: Call<PlaceResponseModel>,
                response: Response<PlaceResponseModel>
            ) {
                if (response.isSuccessful){
                    val result = response.body()
                    result?.results?.forEach { new ->
                        new.prepareLocation()
                        if (!placesTemp.any{ it.place_id == new.place_id}) {
                            placesTemp.add(new)
                        }
                    }

                    if (result?.nextPageToken.isNullOrEmpty()) {
                        complete?.also{ it(placesTemp, null) }
                        placesTemp.clear()
                    }else {
                        GlobalScope.launch (Dispatchers.IO){
                            delay(2000)
                            fetchPlacesNearWithType(lat, long, radius, type, nextPage = result?.nextPageToken, complete = complete)
                        }
                    }
                }else {
                    complete?.also{ it(null, "Failed to find the places") }
                }

            }

            override fun onFailure(
                call: Call<PlaceResponseModel>,
                t: Throwable
            ) {
                complete?.also { it(null, "Failed to find the places") }
            }
        })
    }
}