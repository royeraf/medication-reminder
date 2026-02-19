package com.medicationreminder.data.repository

import com.medicationreminder.data.local.dao.MedicationDao
import com.medicationreminder.data.local.dao.ScheduleDao
import com.medicationreminder.data.local.entity.MedicationEntity
import com.medicationreminder.data.local.entity.ScheduleEntity
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val scheduleDao: ScheduleDao
) : MedicationRepository {

    // ─── Medications ─────────────────────────────────────────────────────────

    override fun getAllActiveMedications(): Flow<List<Medication>> =
        medicationDao.getAllActiveMedications().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAllMedications(): Flow<List<Medication>> =
        medicationDao.getAllMedications().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getMedicationById(id)?.toDomain()

    override suspend fun saveMedication(medication: Medication): Long =
        medicationDao.insertMedication(medication.toEntity())

    override suspend fun updateMedication(medication: Medication) =
        medicationDao.updateMedication(medication.toEntity())

    override suspend fun deleteMedication(medication: Medication) =
        medicationDao.deleteMedication(medication.toEntity())

    override suspend fun deactivateMedication(id: Long) =
        medicationDao.deactivateMedication(id)

    override fun getActiveMedicationCount(): Flow<Int> =
        medicationDao.getActiveMedicationCount()

    // ─── Schedules ────────────────────────────────────────────────────────────

    override fun getSchedulesForMedication(medicationId: Long): Flow<List<Schedule>> =
        scheduleDao.getSchedulesForMedication(medicationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSchedulesForMedicationSync(medicationId: Long): List<Schedule> =
        scheduleDao.getSchedulesForMedicationSync(medicationId).map { it.toDomain() }

    override suspend fun getAllEnabledSchedules(): List<Schedule> =
        scheduleDao.getAllEnabledSchedules().map { it.toDomain() }

    override suspend fun saveSchedule(schedule: Schedule): Long =
        scheduleDao.insertSchedule(schedule.toEntity())

    override suspend fun saveSchedules(schedules: List<Schedule>) =
        scheduleDao.insertSchedules(schedules.map { it.toEntity() })

    override suspend fun updateSchedule(schedule: Schedule) =
        scheduleDao.updateSchedule(schedule.toEntity())

    override suspend fun deleteSchedule(schedule: Schedule) =
        scheduleDao.deleteSchedule(schedule.toEntity())

    override suspend fun deleteSchedulesForMedication(medicationId: Long) =
        scheduleDao.deleteSchedulesForMedication(medicationId)

    override suspend fun setScheduleEnabled(id: Long, enabled: Boolean) =
        scheduleDao.setScheduleEnabled(id, enabled)

    override suspend fun getNextAlarmRequestCode(): Int =
        (scheduleDao.getMaxAlarmRequestCode() ?: 0) + 1

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private fun MedicationEntity.toDomain() = Medication(
        id = id,
        rxcui = rxcui,
        name = name,
        genericName = genericName,
        dosageForm = dosageForm,
        strength = strength,
        instructions = instructions,
        color = color,
        iconIndex = iconIndex,
        isActive = isActive,
        createdAt = createdAt
    )

    private fun Medication.toEntity() = MedicationEntity(
        id = id,
        rxcui = rxcui,
        name = name,
        genericName = genericName,
        dosageForm = dosageForm,
        strength = strength,
        instructions = instructions,
        color = color,
        iconIndex = iconIndex,
        isActive = isActive,
        createdAt = createdAt
    )

    private fun ScheduleEntity.toDomain() = Schedule(
        id = id,
        medicationId = medicationId,
        label = label,
        hour = hour,
        minute = minute,
        isEnabled = isEnabled,
        daysOfWeek = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() },
        alarmRequestCode = alarmRequestCode
    )

    private fun Schedule.toEntity() = ScheduleEntity(
        id = id,
        medicationId = medicationId,
        label = label,
        hour = hour,
        minute = minute,
        isEnabled = isEnabled,
        daysOfWeek = daysOfWeek.joinToString(","),
        alarmRequestCode = alarmRequestCode
    )
}
