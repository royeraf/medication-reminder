package com.medicationreminder.domain.usecase

import com.medicationreminder.domain.model.DrugSearchResult
import com.medicationreminder.domain.repository.RxNormRepository
import com.medicationreminder.util.Resource
import javax.inject.Inject

class SearchDrugsUseCase @Inject constructor(
    private val rxNormRepository: RxNormRepository
) {
    suspend operator fun invoke(query: String): Resource<List<DrugSearchResult>> {
        if (query.isBlank()) return Resource.Success(emptyList())
        return rxNormRepository.searchDrugs(query.trim())
    }
}
