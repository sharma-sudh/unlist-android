package com.sudh.unlist.dto

/** Request body for POST /analyse */
data class AnalyseRequest(val url: String)

/**
 * Full inspection report — the response shape of POST /analyse, GET /report/{id},
 * and the "report" field embedded in a saved-report row.
 *
 * Fields that only exist for one platform ([restoredImperfections], [replacedParts],
 * [tyreLife], [repainted]) are nullable; the UI hides the corresponding section when null/empty.
 */
data class Report(
    val car: Car,
    val platform: String,
    val redFlagScore: Int = 0,
    val summary: String? = null,
    val criticalImperfections: List<Imperfection> = emptyList(),
    val nonCriticalImperfections: List<Imperfection> = emptyList(),
    val restoredImperfections: List<Imperfection>? = null,
    val replacedParts: List<ReplacedPart>? = null,
    val tyreLife: Map<String, TyreCheckpoint>? = null,
    val repainted: Boolean? = null,
    val checklist: List<String> = emptyList()
) {
    val isCars24: Boolean get() = platform.equals("cars24", ignoreCase = true)
    val isSpinny: Boolean get() = platform.equals("spinny", ignoreCase = true)
}
