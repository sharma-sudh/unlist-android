package com.sudh.unlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sudh.unlist.dto.CompareResponse
import com.sudh.unlist.util.asRupees

/** Inline results shown on Home under the Compare section once POST /compare succeeds. */
@Composable
fun CompareResultsSection(
    result: CompareResponse,
    fetchingCarIndex: Int?,
    onCarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        result.verdict?.let { verdict ->
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        val recommended = verdict.recommendedIndex
                        Text(
                            text = if (recommended != null) "Car ${recommended + 1} looks like the better buy" else "Too close to call",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        verdict.reason?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        result.cars.forEachIndexed { index, car ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp),
                onClick = { if (!car.isError) onCarClick(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (car.isError) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Car ${index + 1}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                car.error ?: "Couldn't analyse this listing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            car.car?.let {
                                Text(it.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    it.price.asRupees(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            PlatformBadge(platform = car.platform)
                        }
                        Spacer(Modifier.width(12.dp))
                        if (fetchingCarIndex == index) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        } else {
                            RedFlagChip(score = car.redFlagScore ?: 0)
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "View report")
                        }
                    }
                }
            }
        }
    }
}
