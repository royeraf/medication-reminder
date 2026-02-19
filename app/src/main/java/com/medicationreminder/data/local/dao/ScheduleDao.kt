package com.medicationreminder.data.local.dao

import androidx.room.*
import com.medicationreminder.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId ORDER BY hour ASC, minute ASC")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId ORDER BY hour ASC, minute ASC")
    suspend fun getSchedulesForMedicationSync(medicationId: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getAllEnabledSchedules(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    @Query("UPDATE schedules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setScheduleEnabled(id: Long, enabled: Boolean)

    @Query("SELECT MAX(alarmRequestCode) FROM schedules")
    suspend fun getMaxAlarmRequestCode(): Int?
}
