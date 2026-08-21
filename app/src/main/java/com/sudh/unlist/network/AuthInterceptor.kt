package com.sudh.unlist.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches "Authorization: Bearer <jwt>" to every request when a token is available.
 * Public endpoints (analyse/compare/share/report/auth-google) simply ignore the header
 * server-side, so it's safe to send it unconditionally rather than special-casing routes.
 */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider()
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
