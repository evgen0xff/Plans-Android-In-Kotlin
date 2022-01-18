package com.planscollective.plansapp.models.dataModels

import android.graphics.Bitmap
import android.location.Address
import android.location.Location
import android.os.Parcelable
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import com.planscollective.plansapp.extension.getOpenNowString
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
open class PlaceModel(
        @Expose
        @SerializedName("lat")
        var latitude: Number? = null,

        @Expose
        @SerializedName("long")
        var longitude: Number? = null,

        @Expose
        @SerializedName("icon")
        var icon: String? = null,

        @Expose
        @SerializedName("name")
        var name: String? = null,

        @Expose
        @SerializedName("place_id")
        var place_id: String? = null,

        @Expose
        @SerializedName("types")
        var types: ArrayList<String>? = null,

        @Expose
        @SerializedName("vicinity")
        var address: String? = null,

        @Expose
        @SerializedName("formatted_address")
        var formattedAddress: String? = null,

        @Expose
        @SerializedName("formatted_phone_number")
        var phoneNumber: String? = null,

        @Expose
        @SerializedName("rating")
        var rating: Number? = null,

        @Expose
        @SerializedName("website")
        var website: String? = null,

        @Expose
        @SerializedName("geometry")
        var geometry : LinkedTreeMap<*, *>? = null

) : Parcelable {


        var photoImage: Bitmap? = null
        var location: Location? = null
        var gmsPlace: Place? = null
        var category: CategoryModel? = null
        var marker: Marker? = null

        constructor(place: Place?) : this() {
                gmsPlace = place
                name = place?.name
                address = place?.address
                phoneNumber = place?.phoneNumber
                latitude = place?.latLng?.latitude
                longitude = place?.latLng?.longitude
                place_id = place?.id
                rating = place?.rating
                website = place?.websiteUri?.path

                prepareLocation()
        }

        constructor(address: Address?) : this() {
                latitude = address?.latitude
                longitude = address?.longitude
                name = address?.featureName
                this.address = address?.maxAddressLineIndex?.takeIf { it >= 0 }?.let { address.getAddressLine(0)}
                prepareLocation()
        }

        constructor(lat: Double?, long: Double?, locationName: String? = null, locationAddress: String? = null) : this() {
                latitude = lat
                longitude = long
                name = locationName
                address = locationAddress
                prepareLocation()
        }

        fun isValidPlace() : Boolean {
                var result = true
                category?.typesRestricted?.takeIf { it.isNotEmpty() }?.also{
                        if (it.any { item -> types?.contains(item) == true }) {
                                result = false
                        }
                }
                return result
        }

        fun prepareLocation() {
                if (latitude == null || longitude == null) {
                        (geometry?.get("location") as? LinkedTreeMap<*, *>)?.also{
                                latitude = it["lat"] as? Double
                                longitude = it["lng"] as? Double
                        }
                }

                if (latitude != null && longitude != null) {
                        location = Location("")
                        location?.latitude = latitude!!.toDouble()
                        location?.longitude = longitude!!.toDouble()
                }
        }

        fun getFormattedTypes() : String {
                val temp = ArrayList<String>()
                types?.forEach { item ->
                        if (!item.contains("_") && item != "food" && item != "establishment") {
                                temp.add(item.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                                Locale.getDefault()
                                        ) else it.toString()
                                })
                        }
                }
                return temp.joinToString (" • ")
        }

        fun getOpenString() : String? {
                return gmsPlace?.getOpenNowString()
        }


}
