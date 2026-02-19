package com.medicationreminder.presentation.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.usecase.DeleteMedicationUseCase
import com.medicationreminder.domain.usecase.GetMedicationsUseCase
import com.medicationreminder.worker.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationsUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val deletedMedication: Medication? = null  // for undo snackbar
)

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState(isLoading = true))
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    val filteredMedications: StateFlow<List<Medication>> = _uiState
        .map { state ->
            if (state.searchQuery.isBlank()) state.medications
            else state.medications.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                    it.genericName.contains(state.searchQuery, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            getMedicationsUseCase()
                .collect { medications ->
                    _uiState.update {
                        it.copy(medications = medications, isLoading = false)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            // Cancel all alarms for this medication
            medication.schedules.forEach { schedule ->
                alarmScheduler.cancelAlarm(schedule)
            }
            deleteMedicationUseCase(medication)
            _uiState.update { it.copy(deletedMedication = medication) }
        }
    }

    fun clearDeletedMedication() {
        _uiState.update { it.copy(deletedMedication = null) }
    }
}
