package com.sudh.unlist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sudh.unlist.navigation.BottomNavItem
import com.sudh.unlist.navigation.Screen

/**
 * Custom floating pill-style bottom navigation bar.
 *
 * Unlike the standard M3 [NavigationBar] which spans the full width and sits flush
 * against the screen edge, this variant floats above the content with rounded
 * corners on all sides and a soft shadow — inspired by the Google Photos
 * "Albums" bottom bar.
 *
 * @param currentRoute the currently active destination's route, used to highlight
 *                      the selected item
 * @param onItemSelected called when the user taps a navigation item
 * @param visible whether the bar should be shown. Set to false on drill-in screens
 *                (e.g. [Screen.ReportFullScreen]) to hide the bar entirely.
 */
@Composable
fun FloatingBottomNavBar(
    currentRoute: String?,
    onItemSelected: (Screen) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val items = listOf(Screen.Home, Screen.Saved, Screen.Settings)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                    .height(64.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { screen ->
                        val item = screen as BottomNavItem
                        val selected = currentRoute == screen.route

                        NavPillItem(
                            label = item.label,
                            icon = if (selected) item.selectedIcon else item.unselectedIcon,
                            selected = selected,
                            onClick = { onItemSelected(screen) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        onClick = onClick,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor
            )
            AnimatedVisibility(visible = selected) {
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}