package com.planscollective.plansapp.extension

import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormatString(format: String) : String {
    return SimpleDateFormat(format).format(this)
}


fun Date.getStartedString (isStarted : Boolean = true) : String {
    var result = ""
    val now = Calendar.getInstance()
    val date = Calendar.getInstance()
    date.timeInMillis = this.time

    result = if (now.get(Calendar.DATE) == date.get(Calendar.DATE)){ // Today
        if (now > date) {
            if (isStarted) {
                "Started today at " + toFormatString("h:mm a")
            }else if (now.time.time.toSeconds() > (time.toSeconds() + 60)) {
                "Running Late"
            }else {
                "Today at " + toFormatString("h:mm a")
            }
        }else {
            "Today at " + toFormatString("h:mm a")
        }
    }else if ((now.get(Calendar.DATE) + 1) == date.get(Calendar.DATE)) { // Tomorrow
        "Tomorrow at " + toFormatString("h:mm a")
    }else if (now > date) { // Past
        if (isStarted) {
            val format = if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) "MMM d" else "MMM d, yyyy"
            "Started on " + toFormatString(format)
        }else {
            "Running Late"
        }
    }else { // Future
        val format = if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) "MMM d" else "MMM d, yyyy"
        toFormatString(format)
    }

    return result
}

fun Date.getYearInPlans() : Int {
    val calender = Calendar.getInstance()
    calender.timeInMillis = this.time
    return calender.get(Calendar.YEAR)
}

fun LocalDate.getStartTimeOfMonth() : Long {
    return withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}

fun LocalDate.getEndTimeOfMonth() : Long {
    return withDayOfMonth(lengthOfMonth()).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}

fun LocalDate.toFormatString(format: String) : String {
    return this.format(DateTimeFormatter.ofPattern(format))
}

fun LocalDateTime.toFormatString(format: String) : String {
    return this.format(DateTimeFormatter.ofPattern(format))
}

fun LocalDateTime.timeAgoSince() : String {
    var result = ""

    val now = LocalDateTime.now()
    val difYear = ChronoUnit.YEARS.between(this, now)
    val difMonth = ChronoUnit.MONTHS.between(this, now)
    val difWeeks = ChronoUnit.WEEKS.between(this, now)
    val difDays = ChronoUnit.DAYS.between(this, now)
    val difHours = ChronoUnit.HOURS.between(this, now)
    val difMinutes = ChronoUnit.MINUTES.between(this, now)

    result = if (difYear > 0) {
        toFormatString("MMM d, yyyy")
    }else if (difMonth > 0 || difWeeks > 0) {
        toFormatString("MMM d")
    }else if (difDays > 0) {
        "${difDays}d"
    }else if (difHours > 0) {
        "${difHours}h"
    }else if (difMinutes > 0) {
        "${difMinutes}m"
    }else {
        "Now"
    }

    return result
}

fun LocalDateTime.stringDate() : String {
    var result = ""

    val now = LocalDateTime.now()
    val difYear = ChronoUnit.YEARS.between(this, now)
    val difMonth = ChronoUnit.MONTHS.between(this, now)
    val difDays = ChronoUnit.DAYS.between(this, now)
    val difHours = ChronoUnit.HOURS.between(this, now)
    val difMinutes = ChronoUnit.MINUTES.between(this, now)

    result = if (difYear > 0) {
        toFormatString("MMMM d, yyyy")
    }else if (difMonth > 0) {
        toFormatString("MMMM d")
    }else if (difDays.toInt() == 0) {
        "Today"
    }else if (difDays.toInt() == 1) {
        "Yesterday"
    }else{
        toFormatString("MMMM d")
    }

    return result
}

fun ZonedDateTime.convertToUTC(utcOffsetMinutes: Int? = null): ZonedDateTime {
    val zoneOffset = ZoneOffset.ofTotalSeconds((utcOffsetMinutes ?: 0) * 60)
    val zoneId = ZoneId.ofOffset("UTC", zoneOffset)
    return toInstant().atZone(zoneId)
}


fun Calendar.setCalendarWithTime(calendarTime: Calendar) : Calendar {
    this[Calendar.HOUR_OF_DAY] = calendarTime[Calendar.HOUR_OF_DAY]
    this[Calendar.MINUTE] = calendarTime[Calendar.MINUTE]
    this[Calendar.SECOND] = calendarTime[Calendar.SECOND]
    this[Calendar.MILLISECOND] = calendarTime[Calendar.MILLISECOND]
    return this
}

fun Calendar.setCalendarWithTime(dateTime: Date) : Calendar {
    val calendarTime = Calendar.getInstance()
    calendarTime.time = dateTime
    return setCalendarWithTime(calendarTime)
}

fun Calendar.getCalendarWithTime(time: Long) : Calendar {
    val calendarTime = Calendar.getInstance()
    calendarTime.time = time.toDate()
    return setCalendarWithTime(calendarTime)
}



