package com.sudh.unlist.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudh.unlist.viewmodel.SettingsEvent
import com.sudh.unlist.viewmodel.SettingsViewModel
import androidx.compose.foundation.clickable

/** Settings: email, dark mode toggle, sign out — simple list layout, no nested screens (spec §5). */
@Composable
fun SettingsScreen(
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SignedOut -> onSignedOut()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", style = MaterialTheme.typography.titleLarge) }) }
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                leadingContent = { Icon(Icons.Outlined.Email, contentDescription = null) },
                headlineContent = { Text("Email") },
                supportingContent = {
                    when {
                        uiState.loadingEmail -> CircularProgressIndicator()
                        uiState.email != null -> Text(uiState.email!!)
                        else -> Text("Unavailable")
                    }
                }
            )
            HorizontalDivider()

            ListItem(
                leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                headlineContent = { Text("Dark mode") },
                supportingContent = { Text(if (uiState.darkModeOverride == true) "On" else "Off") },
                trailingContent = {
                    Switch(
                        checked = uiState.darkModeOverride == true,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            )
            HorizontalDivider()

            ListItem(
                modifier = Modifier.clickableSurface { viewModel.requestSignOut() },
                leadingContent = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                headlineContent = { Text("Sign out", color = MaterialTheme.colorScheme.error) }
            )
        }
    }

    if (uiState.signOutDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::cancelSignOut,
            title = { Text("Sign out?") },
            text = { Text("You'll need to sign in again with Google to see your saved reports.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSignOut) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelSignOut) { Text("Cancel") }
            }
        )
    }
}

private fun Modifier.clickableSurface(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)