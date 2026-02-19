package com.medicationreminder.domain.repository

import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import kotlinx.coroutines.flow.Flow

interface DoseLogRepository {

    fun getDoseLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>>

    fun getRecentLogsForMedication(medicationId: Long, limit: Int = 30): Flow<List<DoseLog>>

    suspend fun getDoseLogById(id: Long): DoseLog?

    suspend fun saveDoseLog(doseLog: DoseLog): Long

    suspend fun updateDoseStatus(id: Long, status: DoseStatus, takenAt: Long? = null)

    fun getTakenDoseCount(startDate: Long): Flow<Int>

    fun getTotalDoseCount(startDate: Long): Flow<Int>
}
