package com.medicationreminder.domain.usecase

import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.DoseLogRepository
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.worker.AlarmScheduler
import java.util.Calendar
import javax.inject.Inject

class SaveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val alarmScheduler: AlarmScheduler
) {
    /**
     * Saves a medication with its schedules. Returns the medication ID.
     */
    suspend operator fun invoke(medication: Medication, schedules: List<Schedule>): Long {
        val medicationId = if (medication.id != 0L) {
            repository.updateMedication(medication)
            medication.id
        } else {
            repository.saveMedication(medication)
        }

        // Get existing schedules to preserve isEnabled status and alarmRequestCode
        val existingSchedules = if (medication.id != 0L) {
            repository.getSchedulesForMedicationSync(medication.id)
        } else {
            emptyList()
        }

        // Cancel existing alarms before deleting schedules
        existingSchedules.forEach { alarmScheduler.cancelAlarm(it) }

        // Map incoming schedules, matching with existing ones where possible
        val schedulesToSave = schedules.mapIndexed { index, incoming ->
            val match = existingSchedules.find { 
                it.hour == incoming.hour && it.minute == incoming.minute 
            }
            
            if (match != null) {
                // Preserve existing state for matching time
                match.copy(label = incoming.label, medicationId = medicationId)
            } else {
                // New schedule time
                val requestCode = repository.getNextAlarmRequestCode() + index
                incoming.copy(
                    medicationId = medicationId,
                    alarmRequestCode = requestCode,
                    isEnabled = true
                )
            }
        }

        // Delete old schedules and save new mapped ones
        if (medication.id != 0L) {
            repository.deleteSchedulesForMedication(medication.id)
        }
        repository.saveSchedules(schedulesToSave)

        // Reschedule alarms for the updated medication
        val savedMedication = medication.copy(id = medicationId, schedules = schedulesToSave)
        
        // Fetch saved schedules to get their IDs and ensure dose logs for today
        val savedSchedules = repository.getSchedulesForMedicationSync(medicationId)
        val calendar = Calendar.getInstance()
        
        savedSchedules.forEach { schedule ->
            if (schedule.isEnabled) {
                alarmScheduler.scheduleAlarm(savedMedication, schedule)
                
                // Ensure dose log for today
                calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
                calendar.set(Calendar.MINUTE, schedule.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val scheduledTime = calendar.timeInMillis
                
                doseLogRepository.saveDoseLog(
                    DoseLog(
                        medicationId = medicationId,
                        scheduleId = schedule.id,
                        scheduledTime = scheduledTime,
                        status = DoseStatus.PENDING,
                        medicationName = savedMedication.name,
                        scheduleLabel = schedule.label
                    )
                )
            }
        }

        return medicationId
    }
}
