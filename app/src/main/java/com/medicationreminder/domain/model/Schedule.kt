package com.medicationreminder.domain.model

data class Schedule(
    val id: Long = 0,
    val medicationId: Long,
    val label: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),  // 1=Mon..7=Sun
    val alarmRequestCode: Int = 0
) {
    val timeDisplay: String
        get() {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val m = minute.toString().padStart(2, '0')
            val amPm = if (hour < 12) "AM" else "PM"
            return "$h:$m $amPm"
        }

    val periodLabel: String
        get() = when {
            hour in 5..11 -> "Morning"
            hour in 12..16 -> "Afternoon"
            hour in 17..20 -> "Evening"
            else -> "Night"
        }
}
