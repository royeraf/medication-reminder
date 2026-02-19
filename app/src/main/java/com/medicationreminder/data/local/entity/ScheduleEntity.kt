package com.medicationreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val label: String,          // e.g. "Morning", "Afternoon", "Evening"
    val hour: Int,              // 0-23
    val minute: Int,            // 0-59
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "1,2,3,4,5,6,7",  // CSV of day numbers (1=Mon..7=Sun)
    val alarmRequestCode: Int = 0   // Unique code for AlarmManager
)
