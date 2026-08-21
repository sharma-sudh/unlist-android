package com.sudh.unlist.util

import com.sudh.unlist.dto.Report
import java.util.UUID

/**
 * Transient, in-memory handoff for whichever report is currently open in a dialog or the
 * full-screen drill-in.
 *
 * The app deliberately keeps no local database (dev log §10 — "no Room, everything
 * server-backed"), but the Report Dialog and Report Full Screen aren't NavGraph siblings
 * with a shared ViewModel, so navigating between them still needs *some* way to hand off
 * the already-fetched report object without a wasted round trip. This is that handoff,
 * not a cache in the persistence sense — entries are short-lived and process-scoped.
 */
object ReportNavCache {
    private val store = LinkedHashMap<String, Report>()
    private const val MAX_ENTRIES = 20

    fun put(report: Report): String {
        val key = "r-${UUID.randomUUID().toString().take(8)}"
        put(key, report)
        return key
    }

    fun put(key: String, report: Report) {
        store[key] = report
        while (store.size > MAX_ENTRIES) {
            val oldestKey = store.keys.first()
            store.remove(oldestKey)
        }
    }

    fun get(key: String): Report? = store[key]

    fun savedKey(savedReportId: Int) = "saved-$savedReportId"
    fun sharedKey(shareId: String) = "shared-$shareId"
}
