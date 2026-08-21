package com.sudh.unlist.util

import com.sudh.unlist.BuildConfig

object Constants {
    /** Backend base URL — see app/build.gradle.kts to change deployments. */
    const val BASE_URL: String = BuildConfig.BASE_URL

    /** Google OAuth Web Client ID used by Credential Manager to mint an ID token. */
    const val GOOGLE_WEB_CLIENT_ID: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /** Scheme for shareable report deep links produced by the Share action. */
    const val DEEP_LINK_SCHEME = "unlist"
    const val DEEP_LINK_HOST = "report"

    fun deepLinkFor(reportId: String) = "$DEEP_LINK_SCHEME://$DEEP_LINK_HOST/$reportId"

    const val COMPARE_MIN_URLS = 2
    const val COMPARE_MAX_URLS = 4
}
