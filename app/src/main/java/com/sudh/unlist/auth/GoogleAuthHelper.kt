package com.sudh.unlist.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.sudh.unlist.util.Constants

/**
 * Thin wrapper around Credential Manager for Google Sign-In.
 *
 * Android-side auth is Google OAuth only (see dev log §5.1) — no email/password screen.
 * The flow: Credential Manager -> Google ID token (client-side) -> POST id_token to
 * /auth/google -> JWT. No refresh token; the JWT is valid 30 days server-side and the
 * user simply re-authenticates through this same flow when it expires.
 */
class GoogleAuthHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /** Returns the Google ID token, or throws if the user cancels or no Google account is available. */
    suspend fun requestIdToken(filterByAuthorizedAccounts: Boolean = true): String {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                throw GetCredentialUnknownException("Couldn't parse the Google credential. Please try again.")
            }
        }
        throw GetCredentialUnknownException("Unexpected credential type returned by Google Sign-In.")
    }

    /**
     * First-time sign-in fallback: if the user has no previously-authorized account on this
     * device (filterByAuthorizedAccounts = true throws NoCredentialException), retry allowing
     * any Google account on the device.
     */
    suspend fun requestIdTokenWithFallback(): String {
        return try {
            requestIdToken(filterByAuthorizedAccounts = true)
        } catch (e: GetCredentialException) {
            requestIdToken(filterByAuthorizedAccounts = false)
        }
    }
}
