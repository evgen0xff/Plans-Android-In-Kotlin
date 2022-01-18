package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.R
import kotlinx.parcelize.Parcelize

@Parcelize
open class CategoryModel(
        @Expose
        @SerializedName("name")
        var name: String? = null,

        @Expose
        @SerializedName("iconImage")
        var iconImage: Int? = null,

        @Expose
        @SerializedName("defaultImage")
        var defaultImage: Int? = null,

        @Expose
        @SerializedName("types")
        var types: ArrayList<String>? = null,

        @Expose
        @SerializedName("typesRestricted")
        var typesRestricted: ArrayList<String>? = null,

) : Parcelable {

        companion object {
                var plansCategories = ArrayList<CategoryModel>().apply {
                        val food = CategoryModel(
                                "Food & Drink",
                                R.drawable.ic_food_green,
                                R.drawable.ic_food_white,
                                arrayListOf("bakery", "restaurant", "cafe")
                        )
                        add(food)
                        val outDoors = CategoryModel(
                                "Outdoors",
                                R.drawable.ic_outdoor_green,
                                R.drawable.ic_outdoor_white,
                                arrayListOf("amusement_park", "park", "zoo")
                        )
                        add(outDoors)
                        val nightLife = CategoryModel(
                                "Nightlife",
                                R.drawable.ic_wine_green,
                                R.drawable.ic_wine_white,
                                arrayListOf("bar", "night_club")
                        )
                        add(nightLife)
                        val fitness = CategoryModel(
                                "Fitness",
                                R.drawable.ic_fitness_green,
                                R.drawable.ic_fitness_white,
                                arrayListOf("gym")
                        )
                        add(fitness)
                        val entertainment = CategoryModel(
                                "Entertainment",
                                R.drawable.ic_theater_green,
                                R.drawable.ic_theater_white,
                                arrayListOf("art_gallery", "bowling_alley", "movie_theater", "museum", "spa", "stadium", "aquarium", "casino")
                        )
                        add(entertainment)
                        val shopping = CategoryModel(
                                "Shopping",
                                R.drawable.ic_shopping_green,
                                R.drawable.ic_shopping_white,
                                arrayListOf("shopping_mall", "store"),
                                arrayListOf("pharmacy", "hospital")
                        )
                        add(shopping)
                }
        }
}
