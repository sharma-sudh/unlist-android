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
import com.sudh.unlist.screens.LoginScreen
import com.sudh.unlist.screens.SavedScreen
import com.sudh.unlist.screens.SettingsScreen
import com.sudh.unlist.screens.ReportFullScreen

/**
 * Root navigation graph for the app.
 *
 * Hosts Login, the three bottom-nav destinations (Home, Saved, Settings), and the
 * ReportFullScreen drill-in destination. The floating bottom nav bar is shown only on the
 * three primary destinations and hidden on Login and ReportFullScreen.
 *
 * @param startDestination Home if a session already exists (checked once, synchronously, in
 *   MainActivity via TokenManager), Login otherwise — avoids a login-screen flash on every launch.
 * @param sharedUrl a URL shared into the app from Cars24/Spinny via the Android share sheet.
 * @param sharedReportId the id from a shared-report deep link (unlist://report/{id}).
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    sharedUrl: String? = null,
    sharedReportId: String? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    sharedUrl = sharedUrl,
                    sharedReportId = sharedReportId,
                    onExpandReport = { navKey ->
                        navController.navigate(Screen.ReportFullScreen.createRoute(navKey))
                    },
                    onRequireSignIn = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Saved.route) {
                SavedScreen(
                    onExpandReport = { navKey ->
                        navController.navigate(Screen.ReportFullScreen.createRoute(navKey))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSignedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ReportFullScreen.route) {
                ReportFullScreen(
                    onBack = { navController.popBackStack() },
                    onRequireSignIn = { navController.navigate(Screen.Login.route) }
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
