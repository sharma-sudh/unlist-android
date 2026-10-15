package com.sudh.unlist.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.sudh.unlist.BuildConfig
import com.sudh.unlist.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds and owns the single [UnlistApiService] instance for the app.
 *
 * Must be initialised once, before first use, via [init] — [com.sudh.unlist.UnlistApplication]
 * does this in onCreate() with a lambda that reads the current JWT from [com.sudh.unlist.auth.TokenManager].
 * A lambda (rather than a Context/TokenManager reference) keeps this object decoupled from Android.
 */
object RetrofitClient {

    private var tokenProvider: () -> String? = { null }

    fun init(tokenProvider: () -> String?) {
        this.tokenProvider = tokenProvider
    }

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            // /compare scrapes + runs Gemini for up to 4 listings, then a verdict call — and
            // Render cold starts add more. 30s was cutting it off.
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: UnlistApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(UnlistApiService::class.java)
    }
}