package com.sudh.unlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sudh.unlist.UnlistApplication
import com.sudh.unlist.dto.Report
import com.sudh.unlist.util.Constants
import com.sudh.unlist.util.ReportNavCache
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportFullUiState(
    val report: Report? = null,
    val notFound: Boolean = false,
    val isSaved: Boolean = false,
    val saving: Boolean = false,
    val sharing: Boolean = false
)

class ReportFullViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = getApplication<UnlistApplication>()
    private val savedRepository = app.savedRepository
    private val tokenManager = app.tokenManager

    private val _uiState = MutableStateFlow(ReportFullUiState())
    val uiState: StateFlow<ReportFullUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        val navKey = savedStateHandle.get<String>("reportId").orEmpty()
        val report = ReportNavCache.get(navKey)
        _uiState.update { it.copy(report = report, notFound = report == null, isSaved = navKey.startsWith("saved-")) }
    }

    fun saveReport() {
        val report = _uiState.value.report ?: return
        if (tokenManager.getToken() == null) {
            viewModelScope.launch { _events.emit("sign_in_required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            savedRepository.save(report, title = report.car.title)
                .onSuccess {
                    _uiState.update { it.copy(saving = false, isSaved = true) }
                    _events.emit("Saved to your reports")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false) }
                    _events.emit(e.message ?: "Couldn't save this report.")
                }
        }
    }

    fun shareReport() {
        val report = _uiState.value.report ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(sharing = true) }
            app.reportRepository.share(report)
                .onSuccess { response ->
                    // Same "share:" sentinel convention as "sign_in_required" below — ReportFullScreen
                    // has the Context needed to open the native share sheet, this ViewModel doesn't.
                    val text = "Check out this car's inspection report on Unlist: " +
                        "${Constants.deepLinkFor(response.id)}\n(Opens in the Unlist app)"
                    _events.emit("share:$text")
                }
                .onFailure { e -> _events.emit(e.message ?: "Couldn't create a share link.") }
            _uiState.update { it.copy(sharing = false) }
        }
    }
}
