package com.sudh.unlist.dto

/** Request body for POST /saved */
data class SaveRequest(val report: Report, val title: String? = null)

/** Response from POST /saved — the row that was just inserted (no embedded report). */
data class SavedReportSummary(
    val id: Int,
    val userId: Int? = null,
    val title: String? = null,
    val savedAt: String? = null
)

/** One entry in the response of GET /saved — includes the full embedded report. */
data class SavedReportListItem(
    val id: Int,
    val title: String? = null,
    val savedAt: String? = null,
    val report: Report
)

/** Response from DELETE /saved/{id} */
data class DeleteResponse(val deleted: Boolean = false)
