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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.sudh.unlist.components.EmptyState
import com.sudh.unlist.components.ImperfectionRow
import com.sudh.unlist.components.PlatformBadge
import com.sudh.unlist.components.RedFlagScoreLarge
import com.sudh.unlist.components.ReplacedPartRow
import com.sudh.unlist.components.SectionCard
import com.sudh.unlist.dto.Report
import com.sudh.unlist.dto.TyreCheckpoint
import com.sudh.unlist.util.asKm
import com.sudh.unlist.util.asRupees
import com.sudh.unlist.viewmodel.ReportFullViewModel

/**
 * Report Full Screen: reached via the Expand action on the Report Dialog. Full NavGraph
 * destination, bottom nav hidden here (handled in NavGraph), back returns to wherever the
 * dialog was opened from (spec §3).
 */
@Composable
fun ReportFullScreen(
    onBack: () -> Unit,
    onRequireSignIn: () -> Unit,
    viewModel: ReportFullViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            when {
                message == "sign_in_required" -> onRequireSignIn()
                message.startsWith("share:") -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message.removePrefix("share:"))
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share report"))
                }
                else -> snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.report != null) {
                        IconButton(onClick = viewModel::saveReport, enabled = !uiState.isSaved && !uiState.saving) {
                            if (uiState.saving) {
                                CircularProgressIndicator()
                            } else {
                                Icon(
                                    if (uiState.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save"
                                )
                            }
                        }
                        IconButton(onClick = viewModel::shareReport, enabled = !uiState.sharing) {
                            if (uiState.sharing) {
                                CircularProgressIndicator()
                            } else {
                                Icon(Icons.Outlined.Share, contentDescription = "Share")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding: PaddingValues ->
        val report = uiState.report
        if (report == null) {
            EmptyState(
                message = "This report is no longer available.",
                modifier = Modifier.padding(padding)
            )
        } else {
            ReportFullContent(report = report, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun ReportFullContent(report: Report, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Car details header
        PlatformBadge(platform = report.platform)
        Spacer(Modifier.height(8.dp))
        Text(report.car.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        val details = listOfNotNull(
            report.car.city,
            report.car.fuel,
            report.car.transmission,
            report.car.ownerNumber?.let { "Owner $it" }
        ).joinToString(" · ")
        if (details.isNotBlank()) {
            Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "${report.car.price.asRupees()} · ${report.car.km.asKm()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        RedFlagScoreLarge(score = report.redFlagScore)
        report.summary?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(24.dp))

        SectionCard(title = "Critical imperfections", items = report.criticalImperfections) {
            ImperfectionRow(it)
        }
        if (report.criticalImperfections.isNotEmpty()) Spacer(Modifier.height(12.dp))

        SectionCard(title = "Non-critical imperfections", items = report.nonCriticalImperfections) {
            ImperfectionRow(it)
        }
        if (report.nonCriticalImperfections.isNotEmpty()) Spacer(Modifier.height(12.dp))

        report.restoredImperfections?.let { restored ->
            SectionCard(title = "Restored", items = restored) { ImperfectionRow(it) }
            if (restored.isNotEmpty()) Spacer(Modifier.height(12.dp))
        }

        report.replacedParts?.let { replaced ->
            SectionCard(title = "Replaced parts", items = replaced) { ReplacedPartRow(it) }
            if (replaced.isNotEmpty()) Spacer(Modifier.height(12.dp))
        }

        report.tyreLife?.takeIf { it.isNotEmpty() }?.let { tyres ->
            TyreLifeSection(tyres)
            Spacer(Modifier.height(12.dp))
        }

        report.repainted?.let { repainted ->
            RepaintedBanner(repainted)
            Spacer(Modifier.height(12.dp))
        }

        if (report.checklist.isNotEmpty()) {
            ChecklistSection(report.checklist)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TyreLifeSection(tyres: Map<String, TyreCheckpoint>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tyre life", style = MaterialTheme.typography.titleMedium)
            tyres.values.forEachIndexed { index, tyre ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tyre.label ?: "Tyre", style = MaterialTheme.typography.bodyLarge)
                    Column(horizontalAlignment = Alignment.End) {
                        tyre.value?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        tyre.status?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (index != tyres.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun RepaintedBanner(repainted: Boolean) {
    val color = if (repainted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onColor = if (repainted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    Surface(color = color, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (repainted) "This car has repainted panels" else "No repainted panels detected",
            style = MaterialTheme.typography.titleSmall,
            color = onColor,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ChecklistSection(checklist: List<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Checklist", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            checklist.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
