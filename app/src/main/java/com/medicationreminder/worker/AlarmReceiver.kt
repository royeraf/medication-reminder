package com.medicationreminder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medicationreminder.MainActivity
import com.medicationreminder.R
import com.medicationreminder.data.LanguageSetting
import com.medicationreminder.presentation.components.MedicationColors
import com.medicationreminder.data.SettingsDataStore
import com.medicationreminder.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        when (intent.action) {
            ACTION_MEDICATION_ALARM -> handleMedicationAlarm(context, intent, result)
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> rescheduleAllAlarms(context, result)
            else -> result.finish()
        }
    }

    private fun handleMedicationAlarm(context: Context, intent: Intent, result: PendingResult) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationColorIndex = intent.getIntExtra(EXTRA_MEDICATION_COLOR, 0)
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build a locale-aware context so notification strings respect the saved language
                val languageSetting = settingsDataStore.languageSetting.first()
                val localizedContext = localizedContext(context, languageSetting)

                showNotification(localizedContext, medicationId, medicationName, scheduleLabel, medicationColorIndex)

                // Re-schedule for the next day
                val medication = medicationRepository.getMedicationById(medicationId)
                if (medication != null) {
                    val schedules = medicationRepository.getSchedulesForMedicationSync(medicationId)
                    val schedule = schedules.find { it.id == scheduleId }
                    if (schedule != null && schedule.isEnabled) {
                        alarmScheduler.scheduleAlarm(medication, schedule)
                    }
                }
            } finally {
                result.finish()
            }
        }
    }

    private fun localizedContext(context: Context, setting: LanguageSetting): Context {
        val locale = when (setting) {
            LanguageSetting.SYSTEM -> {
                // Use the real device locale, not Locale.getDefault() which can be
                // overridden by the in-app language switcher.
                val systemConfig = Resources.getSystem().configuration
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    systemConfig.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    systemConfig.locale
                }
            }
            else -> Locale(setting.code)
        }
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun rescheduleAllAlarms(context: Context, result: PendingResult) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedules = medicationRepository.getAllEnabledSchedules()
                schedules.forEach { schedule ->
                    val medication = medicationRepository.getMedicationById(schedule.medicationId)
                    if (medication != null && medication.isActive) {
                        alarmScheduler.scheduleAlarm(medication, schedule)
                    }
                }
            } finally {
                result.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        medicationId: Long,
        medicationName: String,
        scheduleLabel: String,
        colorIndex: Int = 0
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

        val title = context.getString(R.string.notification_title, medicationName)
        val body = if (scheduleLabel.isNotBlank()) {
            context.getString(R.string.notification_body_with_label, scheduleLabel)
        } else {
            context.getString(R.string.notification_body_default)
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(MedicationColors.getColorArgb(colorIndex))
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
                    .setSummaryText(medicationName)
            )
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
        const val EXTRA_MEDICATION_COLOR = "medication_color"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_SCHEDULE_LABEL = "schedule_label"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val CHANNEL_ID = "medication_reminders"
    }
}
