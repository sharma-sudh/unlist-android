package com.sudh.unlist.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.unlist.components.EmptyState
import com.sudh.unlist.components.PlatformBadge
import com.sudh.unlist.components.RedFlagChip
import com.sudh.unlist.components.ReportDialog
import com.sudh.unlist.dto.SavedReportListItem
import com.sudh.unlist.util.ReportNavCache
import com.sudh.unlist.viewmodel.SavedEvent
import com.sudh.unlist.viewmodel.SavedViewModel

/**
 * Saved Reports: explicit saves only, no automatic history (spec §4). Deletion is an icon-button
 * action backed by an optimistic-remove + undo-snackbar flow, matching the "swipe to delete with
 * undo" intent from the spec without pulling in a separate swipe-gesture dependency.
 */
@Composable
fun SavedScreen(
    onExpandReport: (String) -> Unit,
    viewModel: SavedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var openedReportKey by remember { mutableStateOf<String?>(null) }
    var openedReportId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SavedEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Removed \"${event.title}\"",
                        actionLabel = "Undo"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    } else {
                        viewModel.commitDelete()
                    }
                }
                is SavedEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is SavedEvent.LaunchShareSheet -> {
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
        topBar = { TopAppBar(title = { Text("Saved", style = MaterialTheme.typography.titleLarge) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.loading && uiState.reports.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Checked before the plain "empty" branch below: a failed load (auth hiccup,
                // network blip, backend cold-start/restart) must never look identical to
                // "you genuinely have zero saved reports" — that's what was masking the real
                // cause here. Only reachable when reports is empty; a failed refresh with
                // existing reports on screen just keeps showing the stale list.
                uiState.error != null && uiState.reports.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Couldn't load your saved reports.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = viewModel::refresh) {
                                Text("Retry")
                            }
                        }
                    }
                }
                uiState.reports.isEmpty() -> {
                    EmptyState(message = "No saved reports yet. Paste a listing link on Home to get started.")
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 88.dp)
                    ) {
                        items(uiState.reports, key = { it.id }) { item ->
                            SavedReportCard(
                                item = item,
                                onClick = {
                                    val key = viewModel.prepareForViewing(item)
                                    openedReportKey = key
                                    openedReportId = item.id
                                },
                                onDelete = { viewModel.requestDelete(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    val key = openedReportKey
    val openedId = openedReportId
    if (key != null && openedId != null) {
        val report = ReportNavCache.get(key)
        if (report != null) {
            ReportDialog(
                report = report,
                isSaved = true,
                saving = false,
                sharing = uiState.sharingReportId == openedId,
                onDismiss = { openedReportKey = null; openedReportId = null },
                onSave = { /* already saved */ },
                onShare = {
                    uiState.reports.find { it.id == openedId }?.let { viewModel.shareReport(it) }
                },
                onAddToCompare = { openedReportKey = null; openedReportId = null },
                onExpand = {
                    openedReportKey = null
                    openedReportId = null
                    onExpandReport(key)
                }
            )
        }
    }
}

@Composable
private fun SavedReportCard(
    item: SavedReportListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: item.report.car.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                PlatformBadge(platform = item.report.platform)
                item.savedAt?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Saved ${it.take(10)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            RedFlagChip(score = item.report.redFlagScore)
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove")
            }
        }
    }
}
