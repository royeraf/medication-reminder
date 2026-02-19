package com.medicationreminder.domain.usecase

import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> =
        repository.getAllActiveMedications()
}
