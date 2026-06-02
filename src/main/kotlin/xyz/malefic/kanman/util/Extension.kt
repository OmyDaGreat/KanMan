package xyz.malefic.kanman.util

import xyz.malefic.kanman.data.Visibility

fun nowMs(): Long = System.currentTimeMillis()

val String.toVisibility
    get() =
        try {
            Visibility.valueOf(this.uppercase())
        } catch (_: Exception) {
            null
        }
