package com.medicationreminder.presentation.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.domain.usecase.GetMedicationsUseCase
import com.medicationreminder.worker.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemindersUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            getMedicationsUseCase()
                .flatMapLatest { medications ->
                    if (medications.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val scheduleFlows = medications.map { med ->
                            medicationRepository.getSchedulesForMedication(med.id)
                                .map { schedules -> med.copy(schedules = schedules) }
                        }
                        combine(scheduleFlows) { it.toList() }
                    }
                }
                .collect { medicationsWithSchedules ->
                    _uiState.update {
                        it.copy(medications = medicationsWithSchedules, isLoading = false)
                    }
                }
        }
    }

    fun toggleSchedule(schedule: Schedule, enabled: Boolean) {
        viewModelScope.launch {
            medicationRepository.setScheduleEnabled(schedule.id, enabled)
            if (enabled) {
                val medication = medicationRepository.getMedicationById(schedule.medicationId)
                if (medication != null) {
                    alarmScheduler.scheduleAlarm(medication, schedule.copy(isEnabled = true))
                }
            } else {
                alarmScheduler.cancelAlarm(schedule)
            }
        }
    }
}
