package com.sudh.unlist.dto

/** Shape of a FastAPI HTTPException body: {"detail": "..."} */
data class ErrorResponse(val detail: String? = null)
