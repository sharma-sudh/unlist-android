package com.sudh.unlist.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Small, unencrypted preference store for non-sensitive UI settings (currently just the
 * dark mode toggle). Kept separate from [com.sudh.unlist.auth.TokenManager] — no reason
 * to pay the EncryptedSharedPreferences cost for a boolean nobody needs to protect.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var darkModeEnabled: Boolean?
        get() = if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_DARK_MODE).apply()
            } else {
                prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            }
        }

    companion object {
        private const val FILE_NAME = "unlist_settings"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
