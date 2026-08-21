package com.sudh.unlist.network

import com.sudh.unlist.dto.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit contract for the Unlist FastAPI backend.
 *
 * Every response body uses snake_case JSON keys; [RetrofitClient] configures Gson with
 * [com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES] so the camelCase Kotlin
 * field names here map automatically (e.g. redFlagScore <-> red_flag_score).
 */
interface UnlistApiService {

    // ── Public: analyse / compare / share ───────────────────────────────────

    @POST("analyse")
    suspend fun analyse(@Body request: AnalyseRequest): Report

    @POST("compare")
    suspend fun compare(@Body request: CompareRequest): CompareResponse

    @POST("share")
    suspend fun share(@Body request: ShareRequest): ShareResponse

    @GET("report/{reportId}")
    suspend fun getSharedReport(@Path("reportId") reportId: String): Report

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    // ── Protected: saved reports ────────────────────────────────────────────

    @POST("saved")
    suspend fun saveReport(@Body request: SaveRequest): SavedReportSummary

    @GET("saved")
    suspend fun listSaved(): List<SavedReportListItem>

    @DELETE("saved/{reportId}")
    suspend fun deleteSaved(@Path("reportId") reportId: Int): DeleteResponse
}
