package com.sudh.unlist.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sudh.unlist.components.FloatingBottomNavBar
import com.sudh.unlist.screens.HomeScreen
import com.sudh.unlist.screens.SavedScreen
import com.sudh.unlist.screens.SettingsScreen
import com.sudh.unlist.screens.ReportFullScreen

/**
 * Root navigation graph for the app.
 *
 * Hosts the three bottom-nav destinations (Home, Saved, Settings) plus the
 * ReportFullScreen drill-in destination. The floating bottom nav bar is shown
 * only on the three primary destinations and hidden on ReportFullScreen.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onExpandReport = { reportId ->
                        navController.navigate(Screen.ReportFullScreen.createRoute(reportId))
                    }
                )
            }

            composable(Screen.Saved.route) {
                SavedScreen(
                    onExpandReport = { reportId ->
                        navController.navigate(Screen.ReportFullScreen.createRoute(reportId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.ReportFullScreen.route) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId").orEmpty()
                ReportFullScreen(
                    reportId = reportId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        FloatingBottomNavBar(
            currentRoute = currentRoute,
            visible = currentRoute in Screen.bottomNavRoutes,
            onItemSelected = { screen ->
                navController.navigate(screen.route) {
                    // Avoid building up a large stack of bottom nav destinations
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}