package com.medicationreminder.data.remote.api

import com.medicationreminder.data.remote.dto.RxNormDrugsResponse
import com.medicationreminder.data.remote.dto.RxNormPropertiesResponse
import com.medicationreminder.data.remote.dto.RxNormSpellingSuggestionsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RxNormApi {

    /**
     * Search drugs by name.
     * GET https://rxnav.nlm.nih.gov/REST/drugs.json?name={name}
     */
    @GET("drugs.json")
    suspend fun searchDrugs(
        @Query("name") name: String
    ): RxNormDrugsResponse

    /**
     * Get properties for a specific RxNorm concept.
     * GET https://rxnav.nlm.nih.gov/REST/rxcui/{rxcui}/properties.json
     */
    @GET("rxcui/{rxcui}/properties.json")
    suspend fun getProperties(
        @Path("rxcui") rxcui: String
    ): RxNormPropertiesResponse

    /**
     * Get spelling suggestions when no results found.
     * GET https://rxnav.nlm.nih.gov/REST/spellingsuggestions.json?name={name}
     */
    @GET("spellingsuggestions.json")
    suspend fun getSpellingSuggestions(
        @Query("name") name: String
    ): RxNormSpellingSuggestionsResponse

    companion object {
        const val BASE_URL = "https://rxnav.nlm.nih.gov/REST/"
    }
}
