package com.medicationreminder.domain.model

data class Medication(
    val id: Long = 0,
    val rxcui: String = "",
    val name: String,
    val genericName: String = "",
    val dosageForm: String = "",
    val strength: String = "",
    val instructions: String = "",
    val color: Int = 0,
    val iconIndex: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val schedules: List<Schedule> = emptyList()
)
