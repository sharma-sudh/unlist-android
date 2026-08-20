package com.sudh.unlist

import android.app.Application
import com.sudh.unlist.auth.TokenManager
import com.sudh.unlist.repository.AuthRepository

class UnlistApplication : Application() {

    val tokenManager: TokenManager by lazy { TokenManager(this) }
    val authRepository: AuthRepository by lazy { AuthRepository() }
}