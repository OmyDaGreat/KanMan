package xyz.malefic.kanman.shared.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.toPrettyDate(): String = toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

fun Instant.toPrettyDateTime(): String {
    val dt = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.date} ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
}

fun Instant.toPrettyTime(): String {
    val dt = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
}

fun combineToInstant(
    date: String,
    time: String,
): Instant? {
    if (date.isBlank()) return null
    return try {
        val localDate = LocalDate.parse(date)
        val localTime =
            if (time.isNotBlank()) {
                LocalTime.parse(time)
            } else {
                LocalTime(0, 0)
            }
        LocalDateTime(localDate, localTime).toInstant(TimeZone.currentSystemDefault())
    } catch (_: Exception) {
        null
    }
}

fun Instant.isOverdue(): Boolean = this < Clock.System.now()
