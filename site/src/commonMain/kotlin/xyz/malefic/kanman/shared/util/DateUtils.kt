package xyz.malefic.kanman.shared.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.toPrettyDate(): String = toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

fun Instant.toPrettyDateTime(): String {
    val dt = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.date} ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
}

// TODO: Set time in deadlines as well
fun String.toInstant(): Instant = LocalDate.parse(this).atStartOfDayIn(TimeZone.currentSystemDefault())

fun Instant.isOverdue(): Boolean = this < Clock.System.now()
