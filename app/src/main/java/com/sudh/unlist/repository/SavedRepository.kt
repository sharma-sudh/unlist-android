package com.sudh.unlist.repository

import com.sudh.unlist.dto.DeleteResponse
import com.sudh.unlist.dto.Report
import com.sudh.unlist.dto.SaveRequest
import com.sudh.unlist.dto.SavedReportListItem
import com.sudh.unlist.dto.SavedReportSummary
import com.sudh.unlist.network.RetrofitClient
import com.sudh.unlist.util.safeApiCall

/** Handles the account-tied saved-reports flows. Every call here requires a signed-in user. */
class SavedRepository {

    private val api = RetrofitClient.api

    suspend fun save(report: Report, title: String? = null): Result<SavedReportSummary> = safeApiCall {
        api.saveReport(SaveRequest(report, title))
    }

    suspend fun list(): Result<List<SavedReportListItem>> = safeApiCall {
        api.listSaved()
    }

    suspend fun delete(reportId: Int): Result<DeleteResponse> = safeApiCall {
        api.deleteSaved(reportId)
    }
}
