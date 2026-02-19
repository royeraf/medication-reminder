package com.medicationreminder.data.repository

import com.medicationreminder.data.remote.api.RxNormApi
import com.medicationreminder.domain.model.DrugSearchResult
import com.medicationreminder.domain.repository.RxNormRepository
import com.medicationreminder.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RxNormRepositoryImpl @Inject constructor(
    private val rxNormApi: RxNormApi
) : RxNormRepository {

    override suspend fun searchDrugs(query: String): Resource<List<DrugSearchResult>> {
        return try {
            val response = rxNormApi.searchDrugs(query)
            val results = mutableListOf<DrugSearchResult>()

            response.drugGroup?.conceptGroup?.forEach { group ->
                group.conceptProperties?.forEach { prop ->
                    // Filter to most useful term types for patients
                    if (group.tty in setOf("SCD", "SBD", "GPCK", "BPCK", "IN", "MIN", "BN")) {
                        results.add(
                            DrugSearchResult(
                                rxcui = prop.rxcui,
                                name = prop.name,
                                synonym = prop.synonym ?: "",
                                tty = group.tty ?: ""
                            )
                        )
                    }
                }
            }

            // Deduplicate by name (case-insensitive)
            val deduplicated = results
                .distinctBy { it.name.lowercase() }
                .sortedWith(
                    compareBy(
                        { it.isBrand }, // generic first
                        { it.name }
                    )
                )

            Resource.Success(deduplicated)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error searching medications", e)
        }
    }

    override suspend fun getSpellingSuggestions(query: String): Resource<List<String>> {
        return try {
            val response = rxNormApi.getSpellingSuggestions(query)
            val suggestions = response.suggestionGroup?.suggestionList?.suggestion ?: emptyList()
            Resource.Success(suggestions)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error getting suggestions", e)
        }
    }
}
