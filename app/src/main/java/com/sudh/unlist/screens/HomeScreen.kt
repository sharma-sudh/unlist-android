package com.sudh.unlist.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.unlist.components.CompareResultsSection
import com.sudh.unlist.components.ReportDialog
import com.sudh.unlist.viewmodel.HomeEvent
import com.sudh.unlist.viewmodel.HomeViewModel
import com.sudh.unlist.viewmodel.ReportDialogState

/**
 * Home: the Analyse section and Compare section, always both visible (spec §1 / §"Empty States"
 * — Home has no empty state, the inputs are always present and functional). The Report Dialog
 * overlays this screen for both the Analyse result and any Compare card the user taps into.
 *
 * [sharedUrl] carries a URL shared into the app from Cars24/Spinny (see MainActivity); [sharedReportId]
 * carries the id from a shared-report deep link. Both are one-shot — consumed via LaunchedEffect(key).
 */
@Composable
fun HomeScreen(
    sharedUrl: String?,
    sharedReportId: String?,
    onExpandReport: (String) -> Unit,
    onRequireSignIn: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(sharedUrl) {
        sharedUrl?.let { viewModel.consumeSharedUrl(it) }
    }
    LaunchedEffect(sharedReportId) {
        sharedReportId?.let { viewModel.openSharedReport(it) }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is HomeEvent.RequireSignIn -> onRequireSignIn()
                is HomeEvent.LaunchShareSheet -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share report"))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Unlist", style = MaterialTheme.typography.titleLarge) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ── Analyse section ──────────────────────────────────────────────
            Text("Analyse a listing", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.analyseUrl,
                onValueChange = viewModel::onAnalyseUrlChange,
                placeholder = { Text("Paste a Cars24 or Spinny link") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = viewModel::analyse,
                enabled = uiState.analyseUrl.isNotBlank() && !uiState.analyseLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.analyseLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Analyse")
                }
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // ── Compare section ──────────────────────────────────────────────
            Text("Compare listings", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            uiState.compareUrls.forEachIndexed { index, url ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { viewModel.onCompareUrlChange(index, it) },
                        placeholder = { Text("Listing ${index + 1} URL") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.compareUrls.size > 2) {
                        IconButton(onClick = { viewModel.removeCompareField(index) }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Remove")
                        }
                    }
                }
            }

            if (uiState.canAddCompareField) {
                OutlinedButton(
                    onClick = viewModel::addCompareField,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add URL")
                }
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick = viewModel::compare,
                enabled = uiState.canCompare && !uiState.compareLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.compareLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Compare")
                }
            }

            uiState.compareResult?.let { result ->
                Spacer(Modifier.height(20.dp))
                CompareResultsSection(
                    result = result,
                    fetchingCarIndex = uiState.fetchingCarIndex,
                    onCarClick = viewModel::viewCompareCarDetail
                )
            }

            // Room for the floating bottom nav bar to not cover the last field.
            Spacer(Modifier.height(96.dp))
        }
    }

    val dialogState = uiState.dialog
    if (dialogState is ReportDialogState.Shown) {
        ReportDialog(
            report = dialogState.report,
            isSaved = dialogState.isSaved,
            saving = dialogState.saving,
            sharing = dialogState.sharing,
            onDismiss = viewModel::dismissDialog,
            onSave = viewModel::saveCurrentReport,
            onShare = viewModel::shareCurrentReport,
            onAddToCompare = viewModel::addCurrentReportToCompare,
            onExpand = {
                // Dismiss first: otherwise dialog.state stays Shown, and popping back from
                // ReportFullScreen to Home re-shows this same dialog immediately, which reads
                // as "back doesn't work" / looping straight back into the expanded flow.
                viewModel.dismissDialog()
                onExpandReport(dialogState.navKey)
            }
        )
    }
}
