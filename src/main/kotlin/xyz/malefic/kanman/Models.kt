@file:OptIn(ExperimentalUuidApi::class)

package xyz.malefic.kanman

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Data class containing a sticky note.
 *
 * @param uuid Unique identifier for the sticky note.
 * @param title Title representing the sticky note.
 * @param content Content of the sticky note.
 */
data class Sticky(
    val uuid: Uuid,
    val title: String,
    val content: String,
)

/**
 * Enum representing the columns in a Kanban board.
 */
enum class Column {
    BACKLOG,
    PLANNING,
    IN_PROGRESS,
    DONE,
}

/**
 * Enum representing the visibility of a Kanban board.
 */
enum class Visibility {
    PUBLIC,
    PRIVATE,
}

typealias Stickies = MutableMap<Column, MutableList<Sticky>>

/**
 * Data class representing a Kanban board.
 *
 * @param uuid Unique identifier for the board.
 * @param title Title of the board.
 * @param visibility Visibility of the board.
 * @param columns Columns containing the Sticky's of the board.
 * @param users Users involved with the board.
 */
data class Board(
    val uuid: Uuid,
    val title: String,
    val visibility: Visibility,
    val columns: Stickies,
    val users: MutableList<User>,
)

/**
 * Data class representing a user.
 *
 * @param uuid Unique identifier for the user.
 * @param username Username of the user.
 * @param password Password for user authentication.
 * @param boards Boards the user is involved with.
 */
data class User(
    val uuid: Uuid,
    val username: String,
    val password: String,
    val boards: MutableList<Board>,
)
