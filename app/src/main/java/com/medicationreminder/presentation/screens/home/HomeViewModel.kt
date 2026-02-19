package com.medicationreminder.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.DoseLogRepository
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.domain.usecase.GetMedicationsUseCase
import com.medicationreminder.domain.usecase.GetTodayDosesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val medications: List<Medication> = emptyList(),
    val todayDoses: List<DoseLog> = emptyList(),
    val takenCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val greeting: String = "Good morning"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val getTodayDosesUseCase: GetTodayDosesUseCase,
    private val doseLogRepository: DoseLogRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        updateGreeting()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine medications and today's doses
            combine(
                getMedicationsUseCase(),
                getTodayDosesUseCase()
            ) { medications, doses ->
                val takenCount = doses.count { it.status == DoseStatus.TAKEN }
                _uiState.update { state ->
                    state.copy(
                        medications = medications,
                        todayDoses = doses,
                        takenCount = takenCount,
                        totalCount = doses.size,
                        isLoading = false
                    )
                }
            }.collect()
        }

        // Auto-create dose logs for today if they don't exist
        viewModelScope.launch {
            getMedicationsUseCase().first().forEach { medication ->
                ensureTodayDoseLogs(medication)
            }
        }
    }

    private suspend fun ensureTodayDoseLogs(medication: Medication) {
        val schedules = medicationRepository.getSchedulesForMedicationSync(medication.id)
        val calendar = Calendar.getInstance()
        schedules.filter { it.isEnabled }.forEach { schedule ->
            calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
            calendar.set(Calendar.MINUTE, schedule.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val scheduledTime = calendar.timeInMillis

            // Only create if doesn't exist yet
            doseLogRepository.saveDoseLog(
                DoseLog(
                    medicationId = medication.id,
                    scheduleId = schedule.id,
                    scheduledTime = scheduledTime,
                    status = DoseStatus.PENDING
                )
            )
        }
    }

    fun markDoseTaken(doseLog: DoseLog) {
        viewModelScope.launch {
            doseLogRepository.updateDoseStatus(
                id = doseLog.id,
                status = DoseStatus.TAKEN,
                takenAt = System.currentTimeMillis()
            )
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour in 5..11 -> "Good morning"
            hour in 12..17 -> "Good afternoon"
            hour in 18..21 -> "Good evening"
            else -> "Good night"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }
}
