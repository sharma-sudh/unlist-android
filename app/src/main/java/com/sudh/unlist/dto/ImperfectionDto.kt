package com.sudh.unlist.dto

/**
 * Flexible, nullable shape covering every imperfection payload the backend can send.
 *
 * The two platforms disagree on field names for what is conceptually the same thing:
 *  - Spinny                 : { subcategory, part, status, severity }
 *  - Cars24 (critical)      : { category, issues, description, severity }
 *  - Cars24 (non-critical)  : { category, part, status, description, severity }
 *
 * Rather than normalising on the backend (explicitly rejected — see dev log §10),
 * this DTO accepts every possible field and [displayPart]/[displayStatus] resolve
 * the fallback chain for display. See util/ImperfectionMapper.kt.
 */
data class Imperfection(
    val category: String? = null,
    val issues: Int? = null,
    val description: String? = null,
    val subcategory: String? = null,
    val part: String? = null,
    val status: String? = null,
    val severity: Int? = null
)

/** Cars24-only replaced/repainted-adjacent entries never go through enrich_faults, so no severity. */
data class ReplacedPart(
    val subcategory: String? = null,
    val part: String? = null,
    val status: String? = null
)

data class TyreCheckpoint(
    val label: String? = null,
    val value: String? = null,
    val status: String? = null
)
