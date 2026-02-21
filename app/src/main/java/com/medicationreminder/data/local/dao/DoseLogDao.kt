package com.medicationreminder.data.local.dao

import androidx.room.*
import com.medicationreminder.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

/** Flat projection used by SELECT queries that JOIN medications to fetch the label color. */
data class DoseLogWithColor(
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: String,
    val medicationName: String = "",
    val scheduleLabel: String = "",
    val color: Int = 0
)

@Dao
interface DoseLogDao {

    @Query("""
        SELECT d.id, d.medicationId, d.scheduleId, d.scheduledTime, d.takenAt,
               d.status, d.medicationName, d.scheduleLabel,
               COALESCE(m.color, 0) AS color
        FROM dose_logs d
        LEFT JOIN medications m ON d.medicationId = m.id
        WHERE d.scheduledTime >= :startOfDay AND d.scheduledTime < :endOfDay
        ORDER BY d.scheduledTime ASC
    """)
    fun getDoseLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseLogWithColor>>

    @Query("""
        SELECT d.id, d.medicationId, d.scheduleId, d.scheduledTime, d.takenAt,
               d.status, d.medicationName, d.scheduleLabel,
               COALESCE(m.color, 0) AS color
        FROM dose_logs d
        LEFT JOIN medications m ON d.medicationId = m.id
        WHERE d.scheduledTime >= :startDate AND d.scheduledTime < :endDate
        ORDER BY d.scheduledTime ASC
    """)
    fun getDoseLogsForRange(startDate: Long, endDate: Long): Flow<List<DoseLogWithColor>>

    @Query("""
        SELECT d.id, d.medicationId, d.scheduleId, d.scheduledTime, d.takenAt,
               d.status, d.medicationName, d.scheduleLabel,
               COALESCE(m.color, 0) AS color
        FROM dose_logs d
        LEFT JOIN medications m ON d.medicationId = m.id
        WHERE d.medicationId = :medicationId
        ORDER BY d.scheduledTime DESC
        LIMIT :limit
    """)
    fun getRecentLogsForMedication(medicationId: Long, limit: Int = 30): Flow<List<DoseLogWithColor>>

    @Query("""
        SELECT d.id, d.medicationId, d.scheduleId, d.scheduledTime, d.takenAt,
               d.status, d.medicationName, d.scheduleLabel,
               COALESCE(m.color, 0) AS color
        FROM dose_logs d
        LEFT JOIN medications m ON d.medicationId = m.id
        WHERE d.id = :id
    """)
    suspend fun getDoseLogById(id: Long): DoseLogWithColor?

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
