package com.medicationreminder.domain.repository

import com.medicationreminder.domain.model.DrugSearchResult
import com.medicationreminder.util.Resource
import kotlinx.coroutines.flow.Flow

interface RxNormRepository {

    suspend fun searchDrugs(query: String): Resource<List<DrugSearchResult>>

    suspend fun getSpellingSuggestions(query: String): Resource<List<String>>
}
