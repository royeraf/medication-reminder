package com.medicationreminder.domain.usecase

import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class GetTodayDosesUseCase @Inject constructor(
    private val doseLogRepository: DoseLogRepository
) {
    operator fun invoke(): Flow<List<DoseLog>> {
        val calendar = Calendar.getInstance()

        // Start of today (midnight)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of today (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return doseLogRepository.getDoseLogsForDay(startOfDay, endOfDay)
    }
}
