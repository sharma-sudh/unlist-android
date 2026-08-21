package com.sudh.unlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.sudh.unlist.navigation.NavGraph
import com.sudh.unlist.navigation.Screen
import com.sudh.unlist.ui.theme.AppTheme
import com.sudh.unlist.util.Constants
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {

    private var sharedUrlState by mutableStateOf<String?>(null)
    private var sharedReportIdState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        val app = application as UnlistApplication
        val startDestination = if (app.tokenManager.getToken() != null) Screen.Home.route else Screen.Login.route

        setContent {
            val settingsStore = app.settingsStore
            val darkOverride = settingsStore.darkModeEnabled
            val useDarkTheme = darkOverride ?: isSystemInDarkTheme()

            AppTheme(darkTheme = useDarkTheme) {
                NavGraph(
                    navController = rememberNavController(),
                    startDestination = startDestination,
                    sharedUrl = sharedUrlState,
                    sharedReportId = sharedReportIdState
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    extractUrl(text)?.let { sharedUrlState = it }
                }
            }
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.scheme == Constants.DEEP_LINK_SCHEME && data.host == Constants.DEEP_LINK_HOST) {
                    data.lastPathSegment?.let { sharedReportIdState = it }
                }
            }
        }
    }

    /** Share text from Cars24/Spinny often includes a sentence around the link — pull just the URL. */
    private fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = Pattern.compile("https?://\\S+").matcher(text)
        return if (matcher.find()) matcher.group() else text.trim()
    }
}
