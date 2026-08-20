package com.sudh.unlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.unlist.UnlistApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// ── Navigation Events ─────────────────────────────────────────────────────────

sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app            = getApplication<UnlistApplication>()
    private val authRepository = app.authRepository
    private val tokenManager   = app.tokenManager

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

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