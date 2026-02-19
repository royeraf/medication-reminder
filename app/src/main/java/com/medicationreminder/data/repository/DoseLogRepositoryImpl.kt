package com.medicationreminder.data.repository

import com.medicationreminder.data.local.dao.DoseLogDao
import com.medicationreminder.data.local.entity.DoseLogEntity
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoseLogRepositoryImpl @Inject constructor(
    private val doseLogDao: DoseLogDao
) : DoseLogRepository {

    override fun getDoseLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>> =
        doseLogDao.getDoseLogsForDay(startOfDay, endOfDay).map { list ->
            list.map { it.toDomain() }
        }

    override fun getRecentLogsForMedication(medicationId: Long, limit: Int): Flow<List<DoseLog>> =
        doseLogDao.getRecentLogsForMedication(medicationId, limit).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getDoseLogById(id: Long): DoseLog? =
        doseLogDao.getDoseLogById(id)?.toDomain()

    override suspend fun saveDoseLog(doseLog: DoseLog): Long =
        doseLogDao.insertDoseLog(doseLog.toEntity())

    override suspend fun updateDoseStatus(id: Long, status: DoseStatus, takenAt: Long?) =
        doseLogDao.updateDoseStatus(id, status.name, takenAt)

    override fun getTakenDoseCount(startDate: Long): Flow<Int> =
        doseLogDao.getTakenDoseCount(startDate)

    override fun getTotalDoseCount(startDate: Long): Flow<Int> =
        doseLogDao.getTotalDoseCount(startDate)

    private fun DoseLogEntity.toDomain() = DoseLog(
        id = id,
        medicationId = medicationId,
        scheduleId = scheduleId,
        scheduledTime = scheduledTime,
        takenAt = takenAt,
        status = DoseStatus.valueOf(status)
    )

    private fun DoseLog.toEntity() = DoseLogEntity(
        id = id,
        medicationId = medicationId,
        scheduleId = scheduleId,
        scheduledTime = scheduledTime,
        takenAt = takenAt,
        status = status.name
    )
}
