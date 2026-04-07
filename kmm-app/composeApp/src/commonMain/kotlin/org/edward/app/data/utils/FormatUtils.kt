package org.edward.app.data.utils

import kotlin.math.roundToLong

fun Double.formatPrice(): String {
    val rounded = ((this * 100).roundToLong() / 100.0)
    val whole = rounded.toLong()
    val frac = ((rounded - whole) * 100).roundToLong()
    return "$whole.${frac.toString().padStart(2, '0')}"
}

fun Double.formatRating(): String {
    val rounded = ((this * 10).roundToLong() / 10.0)
    val whole = rounded.toLong()
    val frac = ((rounded - whole) * 10).roundToLong()
    return "$whole.$frac"
}
