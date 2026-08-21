package com.sudh.unlist.util

import com.google.gson.Gson
import com.sudh.unlist.dto.ErrorResponse
import retrofit2.HttpException
import java.io.IOException

/** Thrown for any failed API call, carrying a message that's safe to show the user. */
class ApiException(message: String) : Exception(message)

private val gson = Gson()

/**
 * Runs [call], mapping failures into a human-readable [ApiException]:
 *  - HTTP errors: unwraps FastAPI's {"detail": "..."} body when present.
 *  - Network errors: a generic "check your connection" message.
 *  - Anything else: falls back to the exception's own message.
 *
 * Every repository function goes through this so ViewModels only ever deal with
 * `Result<T>` and a message that's ready to put on screen.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: HttpException) {
        val detail = try {
            e.response()?.errorBody()?.string()?.let { body ->
                gson.fromJson(body, ErrorResponse::class.java)?.detail
            }
        } catch (parseError: Exception) {
            null
        }
        Result.failure(ApiException(detail ?: "Something went wrong (${e.code()}). Please try again."))
    } catch (e: IOException) {
        Result.failure(ApiException("Couldn't reach the server. Check your connection and try again."))
    } catch (e: Exception) {
        Result.failure(ApiException(e.message ?: "Something went wrong. Please try again."))
    }
}
