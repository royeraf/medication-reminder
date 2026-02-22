package com.medicationreminder.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(medication: Medication, schedule: Schedule) {
        if (!schedule.isEnabled) return

        val intent = buildAlarmIntent(medication, schedule)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(schedule.hour, schedule.minute, schedule.daysOfWeek)
        // No valid day found (empty daysOfWeek) – skip scheduling
        if (triggerTime == null) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm if exact not permitted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(schedule: Schedule) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.alarmRequestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun rescheduleAllAlarms(medications: List<Medication>) {
        medications.forEach { medication ->
            medication.schedules.forEach { schedule ->
                if (schedule.isEnabled) {
                    scheduleAlarm(medication, schedule)
                }
            }
        }
    }

    private fun buildAlarmIntent(medication: Medication, schedule: Schedule): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MEDICATION_ALARM
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_COLOR, medication.color)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_LABEL, schedule.label)
            putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, schedule.alarmRequestCode)
        }

    /**
     * Computes the next trigger time in millis for the given [hour] and [minute],
     * only on days present in [daysOfWeek] (ISO: 1=Mon .. 7=Sun).
     *
     * Returns null if [daysOfWeek] is empty.
     */
    internal fun getNextTriggerTime(hour: Int, minute: Int, daysOfWeek: List<Int>): Long? {
        if (daysOfWeek.isEmpty()) return null

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, start checking from tomorrow
        val startFromTomorrow = calendar.timeInMillis <= System.currentTimeMillis()
        if (startFromTomorrow) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Check up to 7 days to find a matching day-of-week
        for (i in 0 until 7) {
            val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
            val isoDay = Schedule.calendarToIsoDayOfWeek(calendarDay)
            if (isoDay in daysOfWeek) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Defensive fallback – should not happen if daysOfWeek is valid
        return null
    }
}
