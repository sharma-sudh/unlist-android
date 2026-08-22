package com.sudh.unlist.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Small, unencrypted preference store for non-sensitive UI settings (currently just the
 * dark mode toggle). Kept separate from [com.sudh.unlist.auth.TokenManager] — no reason
 * to pay the EncryptedSharedPreferences cost for a boolean nobody needs to protect.
 *
 * Backed by a [StateFlow] (not just SharedPreferences) so the app-wide theme — read once at
 * the Activity root in MainActivity — recomposes immediately when this changes from Settings,
 * instead of only taking effect the next time the app is launched.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val _darkModeEnabled = MutableStateFlow(readDarkModePref())

    /** null = follow device setting (the app's default, both on first install and if reset). */
    val darkModeEnabledFlow: StateFlow<Boolean?> = _darkModeEnabled

    var darkModeEnabled: Boolean?
        get() = _darkModeEnabled.value
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_DARK_MODE).apply()
            } else {
                prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            }
            _darkModeEnabled.value = value
        }

    private fun readDarkModePref(): Boolean? =
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null

    companion object {
        private const val FILE_NAME = "unlist_settings"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
