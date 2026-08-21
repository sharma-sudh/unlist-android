package com.sudh.unlist.dto

/** Request body for POST /share. The backend accepts an arbitrary report JSON blob. */
data class ShareRequest(val report: Report)

/** Response from POST /share. [id] is a 10-char hex id, valid for 7 days. */
data class ShareResponse(val id: String)
