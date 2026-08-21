package com.sudh.unlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sudh.unlist.dto.Report
import com.sudh.unlist.util.asKm
import com.sudh.unlist.util.asRupees

/**
 * The Report Dialog: a centered Material 3 basic dialog with a scrim, rounded corners, and
 * scrollable content. Used identically whether reached from Analyse, a Compare result, a
 * tapped Saved Reports card, or a shared-link deep link (spec §2 / §"Report Dialog").
 */
@Composable
fun ReportDialog(
    report: Report,
    isSaved: Boolean,
    saving: Boolean,
    sharing: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onAddToCompare: () -> Unit,
    onExpand: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.wrapContentHeight()
        ) {
            Column {
                // Header: platform badge + close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 16.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = report.platform)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Text(text = report.car.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(6.dp))

                    val details = listOfNotNull(
                        report.car.city,
                        report.car.fuel,
                        report.car.transmission,
                        report.car.ownerNumber?.let { "Owner $it" }
                    ).joinToString(" · ")
                    if (details.isNotBlank()) {
                        Text(text = details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${report.car.price.asRupees()} · ${report.car.km.asKm()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))
                    RedFlagScoreLarge(score = report.redFlagScore)
                    Spacer(Modifier.height(16.dp))

                    report.summary?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                    }

                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    QuickStatsRow(report = report)
                    Spacer(Modifier.height(20.dp))
                }

                HorizontalDivider()

                // Bottom action bar — always visible, not part of the scroll area.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogActionButton(
                        icon = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        label = if (isSaved) "Saved" else "Save",
                        loading = saving,
                        enabled = !isSaved,
                        onClick = onSave
                    )
                    DialogActionButton(
                        icon = Icons.Outlined.Share,
                        label = "Share",
                        loading = sharing,
                        onClick = onShare
                    )
                    DialogActionButton(
                        icon = Icons.Outlined.CompareArrows,
                        label = "Compare",
                        onClick = onAddToCompare
                    )
                    DialogActionButton(
                        icon = Icons.Outlined.OpenInFull,
                        label = "Expand",
                        onClick = onExpand
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(report: Report) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickStat(label = "Critical", value = report.criticalImperfections.size)
        QuickStat(label = "Non-critical", value = report.nonCriticalImperfections.size)
        QuickStat(label = "Replaced", value = report.replacedParts?.size ?: 0)
    }
}

@Composable
private fun QuickStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DialogActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true
) {
    TextButton(onClick = onClick, enabled = enabled && !loading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(20.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(icon, contentDescription = label, modifier = Modifier.width(20.dp))
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
