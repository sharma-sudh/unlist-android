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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val email: String? = null,
    val loadingEmail: Boolean = false,
    /** null = follow system, matching AppTheme's default. */
    val darkModeOverride: Boolean? = null,
    val signOutDialogVisible: Boolean = false
)

sealed class SettingsEvent {
    data object SignedOut : SettingsEvent()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<UnlistApplication>()
    private val authRepository = app.authRepository
    private val tokenManager = app.tokenManager
    private val settingsStore = app.settingsStore

    private val _uiState = MutableStateFlow(SettingsUiState(darkModeOverride = settingsStore.darkModeEnabled))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadEmail()
    }

    private fun loadEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingEmail = true) }
            authRepository.me()
                .onSuccess { me -> _uiState.update { it.copy(email = me.email) } }
            _uiState.update { it.copy(loadingEmail = false) }
        }
    }

    fun setDarkMode(enabled: Boolean?) {
        settingsStore.darkModeEnabled = enabled
        _uiState.update { it.copy(darkModeOverride = enabled) }
    }

    fun requestSignOut() {
        _uiState.update { it.copy(signOutDialogVisible = true) }
    }

    fun cancelSignOut() {
        _uiState.update { it.copy(signOutDialogVisible = false) }
    }

    fun confirmSignOut() {
        tokenManager.clearAll()
        _uiState.update { it.copy(signOutDialogVisible = false, email = null) }
        viewModelScope.launch { _events.emit(SettingsEvent.SignedOut) }
    }
}
