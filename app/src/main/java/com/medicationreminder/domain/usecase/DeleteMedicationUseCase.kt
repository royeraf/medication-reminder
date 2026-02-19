package com.medicationreminder.domain.usecase

import com.medicationreminder.domain.model.Medication
import com.medicationreminder.domain.repository.MedicationRepository
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) {
        repository.deleteMedication(medication)
    }
}
