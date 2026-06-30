package xyz.malefic.kanman.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Column {
    BACKLOG,
    PLANNING,
    IN_PROGRESS,
    DONE,
}

@Serializable
enum class Visibility {
    PUBLIC,
    PRIVATE,
    ;

    companion object {
        val String.toVisibility
            get() =
                try {
                    Visibility.valueOf(this.uppercase().trim())
                } catch (_: Exception) {
                    null
                }
    }
}

@Serializable
enum class BoardAction {
    DELETE_BOARD,
    INVITE_USER,
    EDIT_STICKY,
    VIEW_BOARD,
}

@Serializable
enum class Role(
    val permission: (BoardAction) -> Boolean,
) {
    OWNER({ true }),
    ADMIN({
        when (it) {
            BoardAction.DELETE_BOARD -> false
            BoardAction.INVITE_USER -> true
            BoardAction.EDIT_STICKY -> true
            BoardAction.VIEW_BOARD -> true
        }
    }),
    MEMBER({
        when (it) {
            BoardAction.DELETE_BOARD -> false
            BoardAction.INVITE_USER -> false
            BoardAction.EDIT_STICKY -> true
            BoardAction.VIEW_BOARD -> true
        }
    }),
    GUEST({
        when (it) {
            BoardAction.VIEW_BOARD -> true
            else -> false
        }
    }),
}
