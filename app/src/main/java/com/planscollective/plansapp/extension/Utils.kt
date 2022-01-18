package com.planscollective.plansapp.extension

import android.graphics.Bitmap
import android.location.Location
import android.text.Editable
import android.widget.TextView
import com.google.android.libraries.places.api.model.Period
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TimeOfWeek
import com.google.common.reflect.TypeToken
import com.planscollective.plansapp.models.dataModels.NotificationActivityModel
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs


/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

fun HashMap<String, *>.urlQueryString() : String {
    return this.entries.stream().map { it.key + "=" + it.value.toString()}.collect(Collectors.joining("&"))
}

// Convert a Map to an object
inline fun <reified T> Map<String, Any>.toObject(java: Class<NotificationActivityModel>): T {
    return convert()
}

// Convert an object to a Map
fun <T> T.toMap(): MutableMap<String, String?>? {
    return convert()
}

// Convert an object of type T to type R
inline fun <T, reified R> T.convert(): R {
    val gson = com.google.gson.Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<R>() {}.type)
}

fun Location.getDistance(lat: Double?, long: Double?, isMiles: Boolean = true ) : Double? {
    val latitude = lat ?: return  null
    val longitude = long ?: return null

    val destination = Location("")
    destination.latitude = latitude
    destination.longitude = longitude

    val distance = distanceTo(destination).toDouble()

    return if (isMiles) distance * 0.00062137119 else distance
}

fun Place.getOpenNowString() : String? {
    val utcOffSetMinutes = utcOffsetMinutes ?: return null
    val weekdayText = openingHours?.weekdayText ?: return null

    val curDate = ZonedDateTime.now()
    val curUTC = curDate.convertToUTC(utcOffSetMinutes)
    val dayText = curUTC.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val result = if (isOpen == true) {
        weekdayText.firstOrNull{ it.contains(dayText)}?.let{
            val periodText = it.replace(dayText, "").replace("Open ", "")
            "Open now$periodText"
        } ?: "Open now"
    }else {
        "Closed now: " + firstNextOpenTimeString(curUTC) ?: ""
    }
    return result
}

fun Place.firstNextOpenTimeString(datetime: ZonedDateTime) : String? {
    val utcOffSetMinutes = utcOffsetMinutes ?: return null
    val periods = openingHours?.periods ?: return null
    val weekday = datetime.dayOfWeek
    val hour = datetime.hour
    val minutes = datetime.minute
    val currentTime = hour * 60 + minutes

    val period = periods.firstOrNull{
        val start = (it.open?.time?.hours ?: 0) * 60 + (it.open?.time?.minutes ?: 0)
        if (it.close != null) {
            it.open?.day?.name == weekday.name && start > currentTime
        }else true
    }

    var result = ""
    if (period != null) {
        result = "Opens " + period.getString(utcOffSetMinutes, true) + " Today"
    }else {
        for (i in 1 .. 6) {
            val next = (weekday.ordinal + i) % 7
            val nextWeekDay = org.threeten.bp.DayOfWeek.values()[next]
            val nextPeriod = periods.firstOrNull{
                it.open?.day?.name == nextWeekDay.name
            }
            if (nextPeriod != null) {
                result = "Opens " + nextPeriod.getString(utcOffSetMinutes, true)
                if (abs(nextWeekDay.ordinal - weekday.ordinal) > 1) {
                    result += " " + nextWeekDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                }else {
                    result += " Tomorrow"
                }
                break
            }
        }
    }

    return result
}

fun Period.getString(utcOffsetMinutes: Int? = null, isStartOnly: Boolean = false) : String {
    var result = ""
    if (close != null) {
        result = open?.getString(utcOffsetMinutes) ?: ""
        if (!isStartOnly) {
            result += " - " + close!!.getString(utcOffsetMinutes)
        }
    }else {
        result = "24 hours"
    }
    return result
}

fun TimeOfWeek.getString(utcOffsetMinutes: Int? = null) : String {
    var result = ""
    val curCalendar = Calendar.getInstance()
    curCalendar[Calendar.HOUR] = time.hours
    curCalendar[Calendar.MINUTE] = time.minutes
    result = curCalendar.time.toFormatString("h:mm a")
    return result
}



fun TextView.getEditableTextWidth(editable: Editable? = null) : Int {
    return paint.measureText((editable ?: this.text).toString()).toInt()
}

fun TextView.getTextWidth(text: String? = null) : Int {
    return paint.measureText((text ?: this.text).toString()).toInt()
}

fun Bitmap.toByteArray() : ByteArray {
    ByteArrayOutputStream().apply {
        compress(Bitmap.CompressFormat.JPEG, 100 , this)
        return toByteArray()
    }
}
