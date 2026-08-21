package com.sudh.unlist.dto

/** Request body for POST /compare. Backend enforces 2–4 URLs. */
data class CompareRequest(val urls: List<String>)

/** One normalised fault row inside a compare result — see backend compare.py:normalise(). */
data class CompareFault(
    val part: String? = null,
    val status: String? = null,
    val severity: Int = 0,
    val bucket: String? = null // "critical" | "non_critical" | "restored"
)

data class PlatformExtras(
    val tyreLife: Map<String, TyreCheckpoint>? = null,
    val repainted: Boolean? = null
)

/**
 * One entry in the "cars" array of a compare response.
 *
 * The backend returns either a normalised car ([car], [redFlagScore], [faults], ...)
 * or, if that URL failed to analyse, just {"error": ..., "url": ...}. Both shapes
 * land in this one nullable-everything class — check [isError] first.
 */
data class CompareCarResult(
    val car: Car? = null,
    val redFlagScore: Int? = null,
    val faults: List<CompareFault>? = null,
    val platformExtras: PlatformExtras? = null,
    val platform: String? = null,
    val error: String? = null,
    val url: String? = null
) {
    val isError: Boolean get() = error != null
}

data class CompareVerdict(
    val recommendedIndex: Int? = null,
    val reason: String? = null
)

data class CompareResponse(
    val cars: List<CompareCarResult> = emptyList(),
    val verdict: CompareVerdict? = null
)
