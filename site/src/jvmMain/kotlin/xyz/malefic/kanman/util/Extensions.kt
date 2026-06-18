package xyz.malefic.kanman.util

import xyz.malefic.kanman.data.model.Visibility

fun nowMs(): Long = System.currentTimeMillis()

val String.toVisibility
    get() =
        try {
            Visibility.valueOf(this.uppercase().trim())
        } catch (_: Exception) {
            null
        }
