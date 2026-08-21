package com.sudh.unlist.repository

import com.sudh.unlist.dto.AnalyseRequest
import com.sudh.unlist.dto.CompareRequest
import com.sudh.unlist.dto.CompareResponse
import com.sudh.unlist.dto.Report
import com.sudh.unlist.dto.ShareRequest
import com.sudh.unlist.dto.ShareResponse
import com.sudh.unlist.network.RetrofitClient
import com.sudh.unlist.util.safeApiCall

/** Handles the core, unauthenticated report flows: analyse, compare, share, and shared-link lookup. */
class ReportRepository {

    private val api = RetrofitClient.api

    suspend fun analyse(url: String): Result<Report> = safeApiCall {
        api.analyse(AnalyseRequest(url.trim()))
    }

    suspend fun compare(urls: List<String>): Result<CompareResponse> = safeApiCall {
        api.compare(CompareRequest(urls.map { it.trim() }))
    }

    suspend fun share(report: Report): Result<ShareResponse> = safeApiCall {
        api.share(ShareRequest(report))
    }

    suspend fun getSharedReport(reportId: String): Result<Report> = safeApiCall {
        api.getSharedReport(reportId)
    }
}
