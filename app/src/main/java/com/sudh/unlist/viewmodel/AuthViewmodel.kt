package com.sudh.unlist.viewmodel

import android.app.Application
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.unlist.UnlistApplication
import com.sudh.unlist.auth.GoogleAuthHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// ── Navigation Events ─────────────────────────────────────────────────────────

sealed class AuthEvent {
    data object NavigateToHome : AuthEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app            = getApplication<UnlistApplication>()
    private val authRepository = app.authRepository
    private val tokenManager   = app.tokenManager
    private val googleAuthHelper = GoogleAuthHelper(app)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    /** Full flow: Credential Manager -> Google ID token -> POST /auth/google -> store JWT. */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val idToken = googleAuthHelper.requestIdTokenWithFallback()
                signInWithIdToken(idToken)
            } catch (e: GetCredentialCancellationException) {
                _uiState.value = AuthUiState.Idle
            } catch (e: GetCredentialException) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Google Sign-In isn't available on this device."
                )
            }
        }
    }

    private suspend fun signInWithIdToken(idToken: String) {
        authRepository.googleSignIn(idToken)
            .onSuccess { response ->
                tokenManager.saveToken(response.token)
                _uiState.value = AuthUiState.Idle
                _events.emit(AuthEvent.NavigateToHome)
            }
            .onFailure { e ->
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Sign in failed. Please try again."
                )
            }
    }

    fun dismissError() {
        _uiState.value = AuthUiState.Idle
    }

    // If a token exists, skip login. If not, stay on login screen — no event emitted.
    fun checkExistingSession() {
        viewModelScope.launch {
            if (tokenManager.getToken() != null) {
                _events.emit(AuthEvent.NavigateToHome)
            }
        }
    }
}
