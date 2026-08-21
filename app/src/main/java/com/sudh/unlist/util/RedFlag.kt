package com.sudh.unlist.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The red flag score is just a count of critical imperfections (see backend parsers) —
 * there's no fixed upper bound, so this buckets it into three bands for color/labeling.
 * The score itself is always shown alongside the label; this is a visual aid, not the verdict.
 */
enum class RedFlagLevel(val label: String) {
    LOW("Low risk"),
    MEDIUM("Some concerns"),
    HIGH("High risk")
}

fun redFlagLevel(score: Int): RedFlagLevel = when {
    score <= 0 -> RedFlagLevel.LOW
    score <= 2 -> RedFlagLevel.MEDIUM
    else -> RedFlagLevel.HIGH
}

@Composable
fun redFlagColor(score: Int): Color = when (redFlagLevel(score)) {
    RedFlagLevel.LOW -> Color(0xFF2E7D32)
    RedFlagLevel.MEDIUM -> Color(0xFFF57C00)
    RedFlagLevel.HIGH -> MaterialTheme.colorScheme.error
}

/** Severity (0-N, per-fault, from the ML model) reuses the same three-band treatment. */
@Composable
fun severityColor(severity: Int?): Color = when {
    severity == null -> MaterialTheme.colorScheme.onSurfaceVariant
    severity <= 0 -> Color(0xFF2E7D32)
    severity <= 2 -> Color(0xFFF57C00)
    else -> MaterialTheme.colorScheme.error
}
