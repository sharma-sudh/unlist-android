package com.sudh.unlist.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings

/**
 * Sealed class representing all navigable destinations in the app.
 *
 * Destinations with [BottomNavItem] data are shown in the bottom navigation bar.
 * Other destinations (e.g. ReportFullScreen) are drill-in screens reached via
 * navigation actions and do not appear in the bottom nav.
 */
sealed class Screen(val route: String) {

    data object Home : Screen("home"), BottomNavItem {
        override val label = "Home"
        override val selectedIcon: ImageVector = Icons.Filled.Home
        override val unselectedIcon: ImageVector = Icons.Outlined.Home
    }

    data object Saved : Screen("saved"), BottomNavItem {
        override val label = "Saved"
        override val selectedIcon: ImageVector = Icons.Filled.Bookmark
        override val unselectedIcon: ImageVector = Icons.Outlined.BookmarkBorder
    }

    data object Settings : Screen("settings"), BottomNavItem {
        override val label = "Settings"
        override val selectedIcon: ImageVector = Icons.Filled.Settings
        override val unselectedIcon: ImageVector = Icons.Outlined.Settings
    }

    /**
     * Drill-in screen showing the complete inspection report.
     * Reached via the expand action on the Report Dialog.
     * Bottom nav is hidden on this screen.
     *
     * Expects a "reportId" argument identifying which saved/cached report to display.
     */
    data object ReportFullScreen : Screen("report_full/{reportId}") {
        fun createRoute(reportId: String) = "report_full/$reportId"
    }

    companion object {
        /** All routes that should display the bottom navigation bar. */
        val bottomNavRoutes = setOf(Home.route, Saved.route, Settings.route)
    }
}

/**
 * Contract for screens that appear as items in the bottom navigation bar.
 */
interface BottomNavItem {
    val label: String
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
}