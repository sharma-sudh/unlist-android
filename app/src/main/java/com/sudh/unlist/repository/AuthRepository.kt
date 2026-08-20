package com.sudh.unlist.repository

import com.sudh.unlist.dto.AuthResponse
import com.sudh.unlist.dto.GoogleSignInRequest
import com.sudh.unlist.network.RetrofitClient

class AuthRepository {

    private val api = RetrofitClient.api

    suspend fun googleSignIn(idToken: String): Result<AuthResponse> = try {
        Result.success(api.googleSignIn(GoogleSignInRequest(idToken)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}