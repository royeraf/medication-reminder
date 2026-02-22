package com.medicationreminder.presentation.screens.medications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.domain.model.DrugSearchResult
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.domain.usecase.DeleteMedicationUseCase
import com.medicationreminder.domain.usecase.SaveMedicationUseCase
import com.medicationreminder.domain.usecase.SearchDrugsUseCase
import com.medicationreminder.util.Resource
import com.medicationreminder.worker.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedicationUiState(
    val searchQuery: String = "",
    val searchResults: List<DrugSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedDrug: DrugSearchResult? = null,

    // Medication form
    val medicationId: Long = 0L,
    val medicationName: String = "",
    val genericName: String = "",
    val dosageForm: String = "",
    val strength: String = "",
    val instructions: String = "",
    val selectedColorIndex: Int = 0,

    // Schedules
    val schedules: List<Schedule> = emptyList(),

    // UI states
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val searchError: String? = null
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val searchDrugsUseCase: SearchDrugsUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val medicationId: Long? = savedStateHandle["medicationId"]
        if (medicationId != null && medicationId != -1L) {
            loadMedicationForEdit(medicationId)
        }
    }

    private fun loadMedicationForEdit(id: Long) {
        viewModelScope.launch {
            val medication = medicationRepository.getMedicationById(id)
            if (medication != null) {
                val schedules = medicationRepository.getSchedulesForMedicationSync(id)
                _uiState.update {
                    it.copy(
                        medicationId = medication.id,
                        medicationName = medication.name,
                        genericName = medication.genericName,
                        dosageForm = medication.dosageForm,
                        strength = medication.strength,
                        instructions = medication.instructions,
                        selectedColorIndex = medication.color,
                        schedules = schedules,
                        searchQuery = medication.name
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchError = null) }
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            _uiState.update { it.copy(isSearching = true) }
            when (val result = searchDrugsUseCase(query)) {
                is Resource.Success -> _uiState.update {
                    it.copy(searchResults = result.data, isSearching = false)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        searchError = result.message,
                        isSearching = false,
                        searchResults = emptyList()
                    )
                }
                else -> {}
            }
        }
    }

    fun onDrugSelected(drug: DrugSearchResult) {
        _uiState.update {
            it.copy(
                selectedDrug = drug,
                medicationName = drug.name,
                genericName = drug.genericName,
                dosageForm = drug.dosageForm,
                strength = drug.strength,
                searchQuery = drug.name,
                searchResults = emptyList()
            )
        }
    }

    fun onMedicationNameChange(name: String) =
        _uiState.update { it.copy(medicationName = name) }

    fun onGenericNameChange(name: String) =
        _uiState.update { it.copy(genericName = name) }

    fun onDosageFormChange(form: String) =
        _uiState.update { it.copy(dosageForm = form) }

    fun onStrengthChange(strength: String) =
        _uiState.update { it.copy(strength = strength) }

    fun onInstructionsChange(instructions: String) =
        _uiState.update { it.copy(instructions = instructions) }

    fun onColorSelected(index: Int) =
        _uiState.update { it.copy(selectedColorIndex = index) }

    fun addSchedule(hour: Int, minute: Int, label: String, daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)) {
        val schedule = Schedule(
            medicationId = _uiState.value.medicationId,
            label = label,
            hour = hour,
            minute = minute,
            daysOfWeek = daysOfWeek
        )
        _uiState.update { it.copy(schedules = it.schedules + schedule) }
    }

    fun removeSchedule(index: Int) {
        _uiState.update { state ->
            state.copy(schedules = state.schedules.toMutableList().also { it.removeAt(index) })
        }
    }

    fun updateSchedule(index: Int, hour: Int, minute: Int, label: String, daysOfWeek: List<Int>) {
        _uiState.update { state ->
            val updated = state.schedules.toMutableList()
            updated[index] = updated[index].copy(
                hour = hour,
                minute = minute,
                label = label,
                daysOfWeek = daysOfWeek
            )
            state.copy(schedules = updated)
        }
    }

    fun updateScheduleDays(index: Int, daysOfWeek: List<Int>) {
        _uiState.update { state ->
            val updated = state.schedules.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(daysOfWeek = daysOfWeek)
            }
            state.copy(schedules = updated)
        }
    }

    fun saveMedication() {
        val state = _uiState.value
        if (state.medicationName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a medication name") }
            return
        }
        if (state.schedules.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add at least one reminder time") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val medication = Medication(
                    id = state.medicationId,
                    rxcui = state.selectedDrug?.rxcui ?: "",
                    name = state.medicationName.trim(),
                    genericName = state.genericName.trim(),
                    dosageForm = state.dosageForm.trim(),
                    strength = state.strength.trim(),
                    instructions = state.instructions.trim(),
                    color = state.selectedColorIndex,
                    schedules = state.schedules
                )
                val medicationId = saveMedicationUseCase(medication, state.schedules)

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}")
                }
            }
        }
    }

    fun deleteMedication() {
        val state = _uiState.value
        if (state.medicationId == 0L) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                // Cancel every alarm before removing from DB
                state.schedules.forEach { alarmScheduler.cancelAlarm(it) }
                val medication = medicationRepository.getMedicationById(state.medicationId)
                if (medication != null) {
                    deleteMedicationUseCase(medication)
                }
                // Reuse saveSuccess to trigger back navigation
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Failed to delete: ${e.message}")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
