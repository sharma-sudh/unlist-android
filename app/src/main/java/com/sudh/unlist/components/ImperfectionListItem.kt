package com.sudh.unlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sudh.unlist.dto.Imperfection
import com.sudh.unlist.dto.ReplacedPart
import com.sudh.unlist.util.displayPart
import com.sudh.unlist.util.displaySubcategory
import com.sudh.unlist.util.displayStatus
import com.sudh.unlist.util.severityColor

/** One row inside a critical/non-critical/restored imperfections section on the Full Screen report. */
@Composable
fun ImperfectionRow(item: Imperfection, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            item.displaySubcategory?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = item.displayPart, style = MaterialTheme.typography.bodyLarge)
            if (item.displayStatus.isNotBlank()) {
                Text(
                    text = item.displayStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SeverityDot(severity = item.severity)
    }
}

@Composable
fun ReplacedPartRow(item: ReplacedPart, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            item.subcategory?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = item.displayPart, style = MaterialTheme.typography.bodyLarge)
        }
        item.status?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SeverityDot(severity: Int?, modifier: Modifier = Modifier) {
    val color = severityColor(severity)
    Surface(
        color = color,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = (severity ?: "-").toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** A titled card wrapping a list of rows with dividers between them — used throughout Full Screen. */
@Composable
fun <T> SectionCard(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    row: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "$title (${items.size})", style = MaterialTheme.typography.titleMedium)
            items.forEachIndexed { index, item ->
                row(item)
                if (index != items.lastIndex) HorizontalDivider()
            }
        }
    }
}
