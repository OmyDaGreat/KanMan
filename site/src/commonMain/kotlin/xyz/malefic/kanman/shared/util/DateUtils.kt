package xyz.malefic.kanman.shared.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

fun Instant.toPrettyDate(): String = toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

fun String.toInstant(): Instant = LocalDate.parse(this).atStartOfDayIn(TimeZone.currentSystemDefault())

fun Instant.isOverdue(): Boolean = this < Clock.System.now()
