package com.medicationreminder.domain.model

import java.util.Calendar

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

    val isEveryDay: Boolean
        get() = daysOfWeek.sorted() == listOf(1, 2, 3, 4, 5, 6, 7)

    val isWeekdaysOnly: Boolean
        get() = daysOfWeek.sorted() == listOf(1, 2, 3, 4, 5)

    val isWeekendsOnly: Boolean
        get() = daysOfWeek.sorted() == listOf(6, 7)

    /**
     * Checks whether the schedule is active for a given [Calendar.DAY_OF_WEEK] value.
     * Converts from Calendar constants (Sun=1 .. Sat=7) to ISO (Mon=1 .. Sun=7).
     */
    fun isScheduledForDay(calendarDayOfWeek: Int): Boolean {
        val iso = calendarToIsoDayOfWeek(calendarDayOfWeek)
        return iso in daysOfWeek
    }

    companion object {
        /** Day abbreviation resource-id keys in ISO order (Mon=1 .. Sun=7). */
        val DAY_ABBR_KEYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        /**
         * Converts [Calendar.DAY_OF_WEEK] (Sunday=1 .. Saturday=7)
         * to ISO 8601 day-of-week (Monday=1 .. Sunday=7).
         */
        fun calendarToIsoDayOfWeek(calendarDay: Int): Int = when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> calendarDay
        }
    }
}
