package com.medicationreminder.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
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
