package com.medicationreminder.data.local.dao

import androidx.room.*
import com.medicationreminder.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {

    @Query("""
        SELECT * FROM dose_logs
        WHERE scheduledTime >= :startOfDay AND scheduledTime < :endOfDay
        ORDER BY scheduledTime ASC
    """)
    fun getDoseLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseLogEntity>>

    @Query("""
        SELECT * FROM dose_logs
        WHERE medicationId = :medicationId
        ORDER BY scheduledTime DESC
        LIMIT :limit
    """)
    fun getRecentLogsForMedication(medicationId: Long, limit: Int = 30): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getDoseLogById(id: Long): DoseLogEntity?

    @Query("""
        SELECT * FROM dose_logs
        WHERE medicationId = :medicationId AND scheduleId = :scheduleId
        AND scheduledTime = :scheduledTime
        LIMIT 1
    """)
    suspend fun getDoseLogByScheduledTime(
        medicationId: Long,
        scheduleId: Long,
        scheduledTime: Long
    ): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity): Long

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)

    @Query("UPDATE dose_logs SET status = :status, takenAt = :takenAt WHERE id = :id")
    suspend fun updateDoseStatus(id: Long, status: String, takenAt: Long?)

    @Query("""
        SELECT COUNT(*) FROM dose_logs
        WHERE status = 'TAKEN' AND scheduledTime >= :startDate
    """)
    fun getTakenDoseCount(startDate: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM dose_logs
        WHERE scheduledTime >= :startDate
    """)
    fun getTotalDoseCount(startDate: Long): Flow<Int>
}
