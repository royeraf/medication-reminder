package com.medicationreminder.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.repository.DoseLogRepository
import com.medicationreminder.domain.repository.MedicationRepository
import com.medicationreminder.domain.usecase.GetMedicationsUseCase
import com.medicationreminder.domain.usecase.GetTodayDosesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class DayHistory(
    val dayStartMillis: Long,   // midnight of that day
    val doses: List<DoseLog>,   // sorted ASC by scheduledTime
    val isToday: Boolean
)

data class HomeUiState(
    val medications: List<Medication> = emptyList(),
    val todayDoses: List<DoseLog> = emptyList(),
    val pastDoses: List<DoseLog> = emptyList(),
    val upcomingDoses: List<DoseLog> = emptyList(),
    val historyByDay: List<DayHistory> = emptyList(), // one entry per day, today first
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

    fun refresh() {
        loadData()
        updateGreeting()
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        val todayStart = startOfDay(System.currentTimeMillis())
        val sevenDaysAgoStart = todayStart - 6L * 24 * 60 * 60 * 1000
        val endOfToday = todayStart + 24L * 60 * 60 * 1000

        val gracePeriodMs = 30 * 60 * 1000L // 30-minute window to mark a dose as taken

        // Auto-mark any PENDING dose as MISSED only after the grace period has elapsed
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            doseLogRepository.getDoseLogsForRange(sevenDaysAgoStart, now)
                .first()
                .filter { it.status == DoseStatus.PENDING && it.scheduledTime < now - gracePeriodMs }
                .forEach { dose ->
                    doseLogRepository.updateDoseStatus(dose.id, DoseStatus.MISSED, null)
                }
        }

        viewModelScope.launch {
            combine(
                getMedicationsUseCase(),
                doseLogRepository.getDoseLogsForRange(sevenDaysAgoStart, endOfToday)
            ) { medications, allDoses ->
                val now = System.currentTimeMillis()

                val todayDoses = allDoses.filter { it.scheduledTime >= todayStart }
                val takenCount = todayDoses.count { it.status == DoseStatus.TAKEN }

                // Show as upcoming if still within the 30-minute grace window
                val upcoming = todayDoses.filter {
                    it.status == DoseStatus.PENDING && it.scheduledTime >= now - gracePeriodMs
                }

                val historyDoses = allDoses.filter {
                    it.status == DoseStatus.TAKEN ||
                    it.status == DoseStatus.MISSED ||
                    it.status == DoseStatus.SKIPPED ||
                    (it.status == DoseStatus.PENDING && it.scheduledTime < now - gracePeriodMs)
                }
                val historyByDay = buildHistoryByDay(historyDoses, todayStart)

                _uiState.update { state ->
                    state.copy(
                        medications = medications,
                        todayDoses = todayDoses,
                        pastDoses = historyDoses.filter { it.scheduledTime >= todayStart },
                        upcomingDoses = upcoming,
                        historyByDay = historyByDay,
                        takenCount = takenCount,
                        totalCount = todayDoses.size,
                        isLoading = false
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            getMedicationsUseCase().collectLatest { medications ->
                medications.forEach { medication ->
                    ensureTodayDoseLogs(medication)
                }
            }
        }
    }

    private fun startOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun buildHistoryByDay(doses: List<DoseLog>, todayStart: Long): List<DayHistory> =
        doses
            .groupBy { startOfDay(it.scheduledTime) }
            .map { (dayStart, dayDoses) ->
                DayHistory(
                    dayStartMillis = dayStart,
                    doses = dayDoses.sortedBy { it.scheduledTime },
                    isToday = dayStart == todayStart
                )
            }
            .sortedByDescending { it.dayStartMillis } // today first, then descending

    private suspend fun ensureTodayDoseLogs(medication: Medication) {
        val schedules = medicationRepository.getSchedulesForMedicationSync(medication.id)
        val calendar = Calendar.getInstance()
        schedules.filter { it.isEnabled }.forEach { schedule ->
            calendar.set(Calendar.HOUR_OF_DAY, schedule.hour)
            calendar.set(Calendar.MINUTE, schedule.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val scheduledTime = calendar.timeInMillis

            // Repository now handles checking for existence and the DB has a unique constraint
            doseLogRepository.saveDoseLog(
                DoseLog(
                    medicationId = medication.id,
                    scheduleId = schedule.id,
                    scheduledTime = scheduledTime,
                    status = DoseStatus.PENDING,
                    medicationName = medication.name,
                    scheduleLabel = schedule.label
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
