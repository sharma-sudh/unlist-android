package com.sudh.unlist.util

import com.sudh.unlist.dto.Imperfection
import com.sudh.unlist.dto.ReplacedPart

/**
 * Resolves the display fallback chain noted in dev log §6.2:
 * `part ?: category`, `status ?: description`.
 */
val Imperfection.displayPart: String
    get() = part ?: category ?: "Unspecified part"

val Imperfection.displayStatus: String
    get() = status ?: description ?: ""

val Imperfection.displaySubcategory: String?
    get() = subcategory ?: category

val ReplacedPart.displayPart: String
    get() = part ?: "Unspecified part"
