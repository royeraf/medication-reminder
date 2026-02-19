package com.medicationreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rxcui: String = "",          // RxNorm concept unique identifier
    val name: String,
    val genericName: String = "",
    val dosageForm: String = "",     // e.g. "Tablet", "Capsule", "Syrup"
    val strength: String = "",       // e.g. "500 mg"
    val instructions: String = "",   // Patient-specific notes
    val color: Int = 0,              // Pill color index for visual identification
    val iconIndex: Int = 0,          // Icon selection
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
