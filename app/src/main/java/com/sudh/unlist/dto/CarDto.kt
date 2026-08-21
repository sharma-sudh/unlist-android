package com.sudh.unlist.dto

/**
 * Core listing info, common to both platforms.
 *
 * [transmission] and [ownerNumber] are Cars24-only — Spinny's scraper never
 * populates them, so both are nullable here and hidden in the UI when null.
 */
data class Car(
    val title: String,
    val km: Long,
    val fuel: String? = null,
    val city: String? = null,
    val price: Long,
    val transmission: String? = null,
    val ownerNumber: String? = null
)
