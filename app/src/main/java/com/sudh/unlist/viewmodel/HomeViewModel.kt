package com.sudh.unlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudh.unlist.UnlistApplication
import com.sudh.unlist.dto.CompareResponse
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

/** What's currently shown in the Report Dialog overlay on Home. */
sealed class ReportDialogState {
    data object Hidden : ReportDialogState()
    data class Shown(
        val report: Report,
        val navKey: String,
        val sourceUrl: String?,
        val isSaved: Boolean = false,
        val saving: Boolean = false,
        val sharing: Boolean = false
    ) : ReportDialogState()
}

data class HomeUiState(
    val analyseUrl: String = "",
    val analyseLoading: Boolean = false,
    val compareUrls: List<String> = listOf("", ""),
    val compareLoading: Boolean = false,
    val compareResult: CompareResponse? = null,
    val compareResultUrls: List<String> = emptyList(),
    val dialog: ReportDialogState = ReportDialogState.Hidden,
    val fetchingCarIndex: Int? = null
) {
    val canCompare: Boolean
        get() = compareUrls.count { it.isNotBlank() } >= Constants.COMPARE_MIN_URLS
    val canAddCompareField: Boolean
        get() = compareUrls.size < Constants.COMPARE_MAX_URLS
}

sealed class HomeEvent {
    data class ShowMessage(val message: String) : HomeEvent()
    data object RequireSignIn : HomeEvent()
    /** Ask the screen (which has a Context) to open the native Android share sheet. */
    data class LaunchShareSheet(val text: String) : HomeEvent()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = getApplication<UnlistApplication>()
    private val reportRepository = app.reportRepository
    private val savedRepository = app.savedRepository
    private val tokenManager = app.tokenManager

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    // ── Analyse ──────────────────────────────────────────────────────────────

    fun onAnalyseUrlChange(value: String) {
        _uiState.update { it.copy(analyseUrl = value) }
    }

    fun analyse() {
        val url = _uiState.value.analyseUrl.trim()
        if (url.isBlank()) return
        runAnalyse(url)
    }

    /** Prefills and immediately analyses a URL shared into the app from Cars24/Spinny. */
    fun consumeSharedUrl(url: String) {
        _uiState.update { it.copy(analyseUrl = url) }
        runAnalyse(url)
    }

    private fun runAnalyse(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(analyseLoading = true) }
            reportRepository.analyse(url)
                .onSuccess { report -> openDialog(report, sourceUrl = url) }
                .onFailure { e -> _events.emit(HomeEvent.ShowMessage(e.message ?: "Couldn't analyse this listing.")) }
            _uiState.update { it.copy(analyseLoading = false) }
        }
    }

    // ── Compare ──────────────────────────────────────────────────────────────

    fun onCompareUrlChange(index: Int, value: String) {
        _uiState.update { state ->
            val updated = state.compareUrls.toMutableList().also { it[index] = value }
            state.copy(compareUrls = updated)
        }
    }

    fun addCompareField() {
        _uiState.update { state ->
            if (!state.canAddCompareField) return@update state
            state.copy(compareUrls = state.compareUrls + "")
        }
    }

    fun removeCompareField(index: Int) {
        _uiState.update { state ->
            if (state.compareUrls.size <= Constants.COMPARE_MIN_URLS) return@update state
            state.copy(compareUrls = state.compareUrls.filterIndexed { i, _ -> i != index })
        }
    }

    fun compare() {
        val state = _uiState.value
        val urls = state.compareUrls.map { it.trim() }.filter { it.isNotBlank() }
        if (urls.size < Constants.COMPARE_MIN_URLS) return

        viewModelScope.launch {
            _uiState.update { it.copy(compareLoading = true) }
            reportRepository.compare(urls)
                .onSuccess { response ->
                    _uiState.update { it.copy(compareResult = response, compareResultUrls = urls) }
                }
                .onFailure { e -> _events.emit(HomeEvent.ShowMessage(e.message ?: "Couldn't compare these listings.")) }
            _uiState.update { it.copy(compareLoading = false) }
        }
    }

    fun clearCompareResult() {
        _uiState.update { it.copy(compareResult = null, compareResultUrls = emptyList()) }
    }

    /**
     * The /compare response only carries a normalised fault list, not the full report
     * (no summary, no descriptions) — so viewing full detail on a compared car re-runs
     * a single /analyse call for its URL (which is Redis-cached server-side, so this is cheap).
     */
    fun viewCompareCarDetail(index: Int) {
        val url = _uiState.value.compareResultUrls.getOrNull(index) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(fetchingCarIndex = index) }
            reportRepository.analyse(url)
                .onSuccess { report -> openDialog(report, sourceUrl = url) }
                .onFailure { e -> _events.emit(HomeEvent.ShowMessage(e.message ?: "Couldn't load that report.")) }
            _uiState.update { it.copy(fetchingCarIndex = null) }
        }
    }

    // ── Report Dialog ────────────────────────────────────────────────────────

    private fun openDialog(report: Report, sourceUrl: String?, navKey: String? = null) {
        val key = navKey?.also { ReportNavCache.put(it, report) } ?: ReportNavCache.put(report)
        _uiState.update { it.copy(dialog = ReportDialogState.Shown(report = report, navKey = key, sourceUrl = sourceUrl)) }
    }

    /** Used by the deep-link flow (a shared report link opened from outside the app). */
    fun openSharedReport(shareId: String) {
        viewModelScope.launch {
            val cached = ReportNavCache.get(ReportNavCache.sharedKey(shareId))
            if (cached != null) {
                openDialog(cached, sourceUrl = null, navKey = ReportNavCache.sharedKey(shareId))
                return@launch
            }
            reportRepository.getSharedReport(shareId)
                .onSuccess { report -> openDialog(report, sourceUrl = null, navKey = ReportNavCache.sharedKey(shareId)) }
                .onFailure { e -> _events.emit(HomeEvent.ShowMessage(e.message ?: "This shared report has expired.")) }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = ReportDialogState.Hidden) }
    }

    fun saveCurrentReport() {
        val shown = _uiState.value.dialog as? ReportDialogState.Shown ?: return
        if (tokenManager.getToken() == null) {
            viewModelScope.launch { _events.emit(HomeEvent.RequireSignIn) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(dialog = shown.copy(saving = true)) }
            savedRepository.save(shown.report, title = shown.report.car.title)
                .onSuccess {
                    _uiState.update { it.copy(dialog = shown.copy(saving = false, isSaved = true)) }
                    _events.emit(HomeEvent.ShowMessage("Saved to your reports"))
                }
                .onFailure { e ->
                    _uiState.update { it.copy(dialog = shown.copy(saving = false)) }
                    _events.emit(HomeEvent.ShowMessage(e.message ?: "Couldn't save this report."))
                }
        }
    }

    fun shareCurrentReport() {
        val shown = _uiState.value.dialog as? ReportDialogState.Shown ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(dialog = shown.copy(sharing = true)) }
            reportRepository.share(shown.report)
                .onSuccess { response ->
                    // The unlist:// link only opens for people who already have the app installed —
                    // hand it off through the normal Android share sheet (with context) rather than
                    // silently dropping it on the clipboard with no indication of what to do next.
                    val text = "Check out this car's inspection report on Unlist: " +
                        "${Constants.deepLinkFor(response.id)}\n(Opens in the Unlist app)"
                    _events.emit(HomeEvent.LaunchShareSheet(text))
                }
                .onFailure { e -> _events.emit(HomeEvent.ShowMessage(e.message ?: "Couldn't create a share link.")) }
            _uiState.update { it.copy(dialog = shown.copy(sharing = false)) }
        }
    }

    /** Report Dialog's "Compare" action: adds this car's URL into the Compare section on Home. */
    fun addCurrentReportToCompare() {
        val shown = _uiState.value.dialog as? ReportDialogState.Shown ?: return
        val url = shown.sourceUrl ?: return
        _uiState.update { state ->
            val blankIndex = state.compareUrls.indexOfFirst { it.isBlank() }
            val updatedUrls = when {
                blankIndex >= 0 -> state.compareUrls.toMutableList().also { it[blankIndex] = url }
                state.canAddCompareField -> state.compareUrls + url
                else -> state.compareUrls
            }
            state.copy(compareUrls = updatedUrls, dialog = ReportDialogState.Hidden)
        }
    }

}
