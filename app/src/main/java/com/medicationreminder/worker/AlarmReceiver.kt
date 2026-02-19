package com.medicationreminder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.medicationreminder.MainActivity
import com.medicationreminder.R
import com.medicationreminder.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MEDICATION_ALARM -> handleMedicationAlarm(context, intent)
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> rescheduleAllAlarms(context)
        }
    }

    private fun handleMedicationAlarm(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""

        // Show notification
        showNotification(context, medicationId, medicationName, scheduleLabel)

        // Re-schedule for the next day
        CoroutineScope(Dispatchers.IO).launch {
            val medication = medicationRepository.getMedicationById(medicationId) ?: return@launch
            val schedules = medicationRepository.getSchedulesForMedicationSync(medicationId)
            val schedule = schedules.find { it.id == scheduleId } ?: return@launch

            if (schedule.isEnabled) {
                alarmScheduler.scheduleAlarm(medication, schedule)
            }
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = medicationRepository.getAllEnabledSchedules()
            schedules.forEach { schedule ->
                val medication = medicationRepository.getMedicationById(schedule.medicationId)
                if (medication != null && medication.isActive) {
                    alarmScheduler.scheduleAlarm(medication, schedule)
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        medicationId: Long,
        medicationName: String,
        scheduleLabel: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager, context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("medicationId", medicationId)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (scheduleLabel.isNotBlank()) {
            "$scheduleLabel dose – tap to confirm"
        } else {
            "Time to take your medication"
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💊 $medicationName")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager.notify(medicationId.toInt(), notification)
    }

    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        context: Context
    ) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_MEDICATION_ALARM = "com.medicationreminder.MEDICATION_ALARM"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_SCHEDULE_LABEL = "schedule_label"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val CHANNEL_ID = "medication_reminders"
    }
}
