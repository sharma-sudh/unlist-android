package com.sudh.unlist.repository

import com.sudh.unlist.dto.AuthResponse
import com.sudh.unlist.dto.GoogleSignInRequest
import com.sudh.unlist.dto.MeResponse
import com.sudh.unlist.network.RetrofitClient
import com.sudh.unlist.util.safeApiCall

class AuthRepository {

    private val api = RetrofitClient.api

    suspend fun googleSignIn(idToken: String): Result<AuthResponse> = safeApiCall {
        api.googleSignIn(GoogleSignInRequest(idToken))
    }

    suspend fun me(): Result<MeResponse> = safeApiCall {
        api.me()
    }
}
