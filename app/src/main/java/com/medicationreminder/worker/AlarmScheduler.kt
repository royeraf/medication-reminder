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

        val triggerTime = getNextTriggerTime(schedule.hour, schedule.minute)

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
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_LABEL, schedule.label)
            putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, schedule.alarmRequestCode)
        }

    private fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
