package com.planscollective.plansapp.extension

import android.content.res.Resources
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.*

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.toSeconds(): Long = this / 1000

fun Long.toMilliseconds(): Long = this * 1000

fun Number.toSeconds(): Long = this.toLong() / 1000

fun Number.toMilliseconds(): Long = this.toLong() * 1000

fun Number.toDate() : Date = Date(this.toMilliseconds())

fun Number.toCalendar() : Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = toMilliseconds()
    return calendar
}

fun Number.toLocalDateTime() : LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(this.toLong()), ZoneId.systemDefault())
}

fun Number.toLocalDate() : LocalDate {
    return this.toLocalDateTime().toLocalDate()
}