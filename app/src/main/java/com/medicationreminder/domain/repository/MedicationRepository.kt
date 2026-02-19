package com.medicationreminder.domain.repository

import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {

    fun getAllActiveMedications(): Flow<List<Medication>>

    fun getAllMedications(): Flow<List<Medication>>

    suspend fun getMedicationById(id: Long): Medication?

    suspend fun saveMedication(medication: Medication): Long

    suspend fun updateMedication(medication: Medication)

    suspend fun deleteMedication(medication: Medication)

    suspend fun deactivateMedication(id: Long)

    fun getActiveMedicationCount(): Flow<Int>

    // Schedules
    fun getSchedulesForMedication(medicationId: Long): Flow<List<Schedule>>

    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<Schedule>

    suspend fun getAllEnabledSchedules(): List<Schedule>

    suspend fun saveSchedule(schedule: Schedule): Long

    suspend fun saveSchedules(schedules: List<Schedule>)

    suspend fun updateSchedule(schedule: Schedule)

    suspend fun deleteSchedule(schedule: Schedule)

    suspend fun deleteSchedulesForMedication(medicationId: Long)

    suspend fun setScheduleEnabled(id: Long, enabled: Boolean)

    suspend fun getNextAlarmRequestCode(): Int
}
