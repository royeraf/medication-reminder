package com.medicationreminder.domain.model

data class DrugSearchResult(
    val rxcui: String,
    val name: String,
    val synonym: String = "",
    val tty: String = "",           // Term type (e.g. SCD = Clinical Drug, SBD = Brand)
    val genericName: String = "",
    val dosageForm: String = "",
    val strength: String = ""
) {
    val displayName: String
        get() = if (synonym.isNotBlank()) "$name ($synonym)" else name

    val isBrand: Boolean
        get() = tty == "SBD" || tty == "BPCK"
}
