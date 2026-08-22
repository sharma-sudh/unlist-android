package com.sudh.unlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.unlist.UnlistApplication
import com.sudh.unlist.dto.SavedReportListItem
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

data class SavedUiState(
    val loading: Boolean = false,
    val reports: List<SavedReportListItem> = emptyList(),
    val error: String? = null,
    /** Item pending permanent deletion while its undo snackbar is showing. */
    val pendingDelete: SavedReportListItem? = null,
    /** id of the saved report currently generating a share link, if any. */
    val sharingReportId: Int? = null
)

sealed class SavedEvent {
    data class ShowUndoSnackbar(val title: String) : SavedEvent()
    data class ShowMessage(val message: String) : SavedEvent()
    /** Ask the screen (which has a Context) to open the native Android share sheet. */
    data class LaunchShareSheet(val text: String) : SavedEvent()
}

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<UnlistApplication>()
    private val savedRepository = app.savedRepository
    private val reportRepository = app.reportRepository

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SavedEvent>()
    val events: SharedFlow<SavedEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            savedRepository.list()
                .onSuccess { reports -> _uiState.update { it.copy(reports = reports.sortedByDescending { r -> r.savedAt }) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Couldn't load your saved reports.") } }
            _uiState.update { it.copy(loading = false) }
        }
    }

    /** Returns a nav key the caller can pass straight to Screen.ReportFullScreen / the Report Dialog. */
    fun prepareForViewing(item: SavedReportListItem): String {
        val key = ReportNavCache.savedKey(item.id)
        ReportNavCache.put(key, item.report)
        return key
    }

    fun shareReport(item: SavedReportListItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(sharingReportId = item.id) }
            reportRepository.share(item.report)
                .onSuccess { response ->
                    val text = "Check out this car's inspection report on Unlist: " +
                        "${Constants.deepLinkFor(response.id)}\n(Opens in the Unlist app)"
                    _events.emit(SavedEvent.LaunchShareSheet(text))
                }
                .onFailure { e -> _events.emit(SavedEvent.ShowMessage(e.message ?: "Couldn't create a share link.")) }
            _uiState.update { it.copy(sharingReportId = null) }
        }
    }

    /** Optimistically removes the card and shows an undo snackbar; actually deletes after [commitDelete]. */
    fun requestDelete(item: SavedReportListItem) {
        _uiState.update { state ->
            state.copy(
                reports = state.reports.filterNot { it.id == item.id },
                pendingDelete = item
            )
        }
        viewModelScope.launch {
            _events.emit(SavedEvent.ShowUndoSnackbar(item.title ?: item.report.car.title))
        }
    }

    fun undoDelete() {
        _uiState.update { state ->
            val pending = state.pendingDelete ?: return@update state
            state.copy(
                reports = (state.reports + pending).sortedByDescending { it.savedAt },
                pendingDelete = null
            )
        }
    }

    /** Call when the undo snackbar times out / is dismissed without the user tapping Undo. */
    fun commitDelete() {
        val pending = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            savedRepository.delete(pending.id)
                .onFailure {
                    // Deletion failed server-side — put the card back so state stays consistent.
                    _uiState.update { state -> state.copy(reports = (state.reports + pending).sortedByDescending { r -> r.savedAt }) }
                }
        }
    }
}
