package com.medicationreminder.domain.usecase

import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.MedicationRepository
import javax.inject.Inject

class SaveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    /**
     * Saves a medication with its schedules. Returns the medication ID.
     */
    suspend operator fun invoke(medication: Medication, schedules: List<Schedule>): Long {
        val medicationId = repository.saveMedication(medication)

        // Delete old schedules if updating
        if (medication.id != 0L) {
            repository.deleteSchedulesForMedication(medication.id)
        }

        // Insert new schedules with the resolved medication ID
        val schedulesToSave = schedules.mapIndexed { index, schedule ->
            val requestCode = repository.getNextAlarmRequestCode() + index
            schedule.copy(
                medicationId = medicationId,
                alarmRequestCode = requestCode
            )
        }
        repository.saveSchedules(schedulesToSave)

        return medicationId
    }
}
