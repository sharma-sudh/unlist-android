package com.sudh.unlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.sudh.unlist.util.redFlagColor
import com.sudh.unlist.util.redFlagLevel

/** Large, prominent, color-coded red flag score — the centerpiece of the Report Dialog. */
@Composable
fun RedFlagScoreLarge(score: Int, modifier: Modifier = Modifier) {
    val color = redFlagColor(score)
    val level = redFlagLevel(score)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = color
                )
            }
            Text(
                text = level.label,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}

/** Compact chip used on cards (Saved list, Compare results). */
@Composable
fun RedFlagChip(score: Int, modifier: Modifier = Modifier) {
    val color = redFlagColor(score)
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Flag,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}

/** Small platform badge shown on report cards ("Cars24" / "Spinny"). */
@Composable
fun PlatformBadge(platform: String?, modifier: Modifier = Modifier) {
    val label = when (platform?.lowercase()) {
        "cars24" -> "Cars24"
        "spinny" -> "Spinny"
        else -> platform ?: "Unknown"
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
