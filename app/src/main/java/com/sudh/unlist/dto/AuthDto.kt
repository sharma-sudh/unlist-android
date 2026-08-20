package com.sudh.unlist.dto

/** Request body for POST /auth/google */
data class GoogleSignInRequest(
    val idToken: String
)

/** Response from POST /auth/google */
data class AuthResponse(
    val token: String,
    val user: AuthUser
)

data class AuthUser(
    val name: String,
    val email: String,
    val picture: String? = null
)

/** Response from GET /auth/me */
data class MeResponse(
    val userId: String,
    val email: String
)