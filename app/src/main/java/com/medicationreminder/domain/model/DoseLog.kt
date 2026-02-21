package com.medicationreminder.domain.model

import com.medicationreminder.data.local.entity.DoseStatus

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledTime: Long,
    val takenAt: Long? = null,
    val status: DoseStatus = DoseStatus.PENDING,
    val medicationName: String = "",
    val scheduleLabel: String = "",
    val color: Int = 0
)
