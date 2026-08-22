package com.sudh.unlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
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
            // Observed as State (not a one-shot read) so toggling dark mode in Settings
            // re-themes the whole app immediately, not just on next launch.
            val darkOverride by settingsStore.darkModeEnabledFlow.collectAsState()
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
                if (data != null && isReportLink(data)) {
                    data.lastPathSegment?.let { sharedReportIdState = it }
                }
            }
        }
    }

    /**
     * True for either shape a shared report link can arrive in:
     *  - unlist://report/<id> — the legacy custom-scheme link, and the one the backend's own
     *    web fallback page still opens via its "Open in Unlist app" button.
     *  - https://<APP_LINK_HOST>/report/<id> — the verified App Link that Constants.deepLinkFor
     *    now generates for Share. android:pathPrefix="/report/" in the manifest is what makes
     *    Android route this to the app at all; this check just mirrors that same shape so both
     *    forms extract the report id identically via lastPathSegment below.
     */
    private fun isReportLink(data: Uri): Boolean {
        val isCustomScheme = data.scheme == Constants.DEEP_LINK_SCHEME && data.host == Constants.DEEP_LINK_HOST
        val isAppLink = data.scheme == "https" &&
            data.host == Constants.APP_LINK_HOST &&
            data.path?.startsWith("/report/") == true
        return isCustomScheme || isAppLink
    }

    /** Share text from Cars24/Spinny often includes a sentence around the link — pull just the URL. */
    private fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = Pattern.compile("https?://\\S+").matcher(text)
        return if (matcher.find()) matcher.group() else text.trim()
    }
}
