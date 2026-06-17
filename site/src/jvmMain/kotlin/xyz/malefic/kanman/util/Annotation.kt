@file:Suppress("ktlint:standard:filename")

package xyz.malefic.kanman.util

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

/**
 * Internal non-functional annotation specifically for [JdbcTransaction] extension functions that specifies database-operating functions require a transaction block
 */
internal annotation class RequiresTransaction
