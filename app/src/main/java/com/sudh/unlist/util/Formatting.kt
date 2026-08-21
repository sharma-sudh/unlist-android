package com.sudh.unlist.util

import java.text.NumberFormat
import java.util.Locale

private val inrFormat: NumberFormat by lazy {
    NumberFormat.getNumberInstance(Locale("en", "IN"))
}

/** ₹ formatted with Indian digit grouping, e.g. 845000 -> "₹8,45,000" */
fun Long.asRupees(): String = "\u20B9${inrFormat.format(this)}"

/** e.g. 42000 -> "42,000 km" */
fun Long.asKm(): String = "${inrFormat.format(this)} km"
