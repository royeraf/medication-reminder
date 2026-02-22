package com.medicationreminder.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Cache formatters. DateTimeFormatter is thread-safe and faster than SimpleDateFormat.
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault())
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()).withZone(ZoneId.systemDefault())
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault())
private val headerDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()).withZone(ZoneId.systemDefault())
private val accordionDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault()).withZone(ZoneId.systemDefault())

fun Long.toFormattedHeaderDate(): String {
    return headerDateFormatter.format(Instant.ofEpochMilli(this)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun Long.toFormattedAccordionDate(): String {
    return accordionDateFormatter.format(Instant.ofEpochMilli(this)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun Long.toFormattedTime(): String {
    return timeFormatter.format(Instant.ofEpochMilli(this))
}

fun Long.toFormattedDate(): String {
    return dateFormatter.format(Instant.ofEpochMilli(this))
}

fun Long.toFormattedDateTime(): String {
    return dateTimeFormatter.format(Instant.ofEpochMilli(this))
}

fun Int.toTimeString(): String {
    val h = if (this == 0) 12 else if (this > 12) this - 12 else this
    val amPm = if (this < 12) "AM" else "PM"
    return "$h $amPm"
}

fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val m = minute.toString().padStart(2, '0')
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h:$m $amPm"
}
