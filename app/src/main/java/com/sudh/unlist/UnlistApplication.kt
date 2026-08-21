package com.sudh.unlist

import android.app.Application
import com.sudh.unlist.auth.TokenManager
import com.sudh.unlist.network.RetrofitClient
import com.sudh.unlist.repository.AuthRepository
import com.sudh.unlist.repository.ReportRepository
import com.sudh.unlist.repository.SavedRepository
import com.sudh.unlist.util.SettingsStore

class UnlistApplication : Application() {

    val tokenManager: TokenManager by lazy { TokenManager(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val reportRepository: ReportRepository by lazy { ReportRepository() }
    val savedRepository: SavedRepository by lazy { SavedRepository() }

    override fun onCreate() {
        super.onCreate()
        // Every outgoing request reads the current token lazily through this lambda,
        // so sign-in/sign-out take effect immediately without rebuilding the client.
        RetrofitClient.init { tokenManager.getToken() }
    }
}
