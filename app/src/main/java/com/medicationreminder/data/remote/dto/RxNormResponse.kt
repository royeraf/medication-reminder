package com.medicationreminder.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── /drugs.json?name={name} ──────────────────────────────────────────────────
data class RxNormDrugsResponse(
    @SerializedName("drugGroup") val drugGroup: DrugGroup?
)

data class DrugGroup(
    @SerializedName("name") val name: String?,
    @SerializedName("conceptGroup") val conceptGroup: List<ConceptGroup>?
)

data class ConceptGroup(
    @SerializedName("tty") val tty: String?,          // Term type: SCD, SBD, GPCK, BPCK…
    @SerializedName("conceptProperties") val conceptProperties: List<ConceptProperty>?
)

data class ConceptProperty(
    @SerializedName("rxcui") val rxcui: String,
    @SerializedName("name") val name: String,
    @SerializedName("synonym") val synonym: String?,
    @SerializedName("tty") val tty: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("suppress") val suppress: String?,
    @SerializedName("umlscui") val umlscui: String?
)

// ── /rxcui/{rxcui}/properties.json ──────────────────────────────────────────
data class RxNormPropertiesResponse(
    @SerializedName("properties") val properties: RxConceptProperties?
)

data class RxConceptProperties(
    @SerializedName("rxcui") val rxcui: String,
    @SerializedName("name") val name: String,
    @SerializedName("synonym") val synonym: String?,
    @SerializedName("tty") val tty: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("suppress") val suppress: String?,
    @SerializedName("umlscui") val umlscui: String?
)

// ── /spellingsuggestions.json?name={name} ───────────────────────────────────
data class RxNormSpellingSuggestionsResponse(
    @SerializedName("suggestionGroup") val suggestionGroup: SuggestionGroup?
)

data class SuggestionGroup(
    @SerializedName("name") val name: String?,
    @SerializedName("suggestionList") val suggestionList: SuggestionList?
)

data class SuggestionList(
    @SerializedName("suggestion") val suggestion: List<String>?
)
