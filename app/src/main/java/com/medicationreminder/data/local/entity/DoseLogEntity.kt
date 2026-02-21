package com.medicationreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("medicationId"),
        Index(value = ["medicationId", "scheduleId", "scheduledTime"], unique = true)
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledTime: Long,        // Epoch millis of the scheduled dose
    val takenAt: Long? = null,      // Epoch millis when actually taken (null = missed)
    val status: String = DoseStatus.PENDING.name,  // PENDING, TAKEN, MISSED, SKIPPED
    val medicationName: String = "",
    val scheduleLabel: String = ""
)

enum class DoseStatus { PENDING, TAKEN, MISSED, SKIPPED }
