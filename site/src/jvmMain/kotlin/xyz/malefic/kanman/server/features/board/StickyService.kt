package xyz.malefic.kanman.server.features.board

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import xyz.malefic.kanman.server.data.AssignedUserEntity
import xyz.malefic.kanman.server.data.StickyNoteEntity
import xyz.malefic.kanman.server.data.UserEntity
import xyz.malefic.kanman.server.data.data
import xyz.malefic.kanman.shared.data.model.BoardAction.EDIT_STICKY
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import xyz.malefic.kanman.shared.data.model.WsAction
import kotlin.uuid.Uuid

context(_: Raise<Issue>)
fun UserResponseModel.createSticky(
    event: WsAction.StickyCreate,
    boardId: Uuid,
) = data {
    val board = getAccessibleBoard(boardId, EDIT_STICKY)

    val sticky =
        StickyNoteEntity
            .new {
                title = event.title
                content = event.content ?: ""
                column = event.column
                this.board = board
            }

    event.assignedUsers.forEach { assignedUserModel ->
        val user = UserEntity.findById(assignedUserModel.userId) ?: return@forEach
        if (board.memberships.any { it.user == user }) {
            AssignedUserEntity.new(sticky, user, assignedUserModel.due)
        }
    }

    sticky.toModel()
}

context(_: Raise<Issue>)
fun UserResponseModel.deleteSticky(
    event: WsAction.StickyDelete,
    boardId: Uuid,
) = data {
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId, EDIT_STICKY) }?.delete())
    { Issue.Board.NotFound() }
}

context(_: Raise<Issue>)
fun UserResponseModel.moveSticky(
    event: WsAction.StickyMove,
    boardId: Uuid,
) = data {
    ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == getAccessibleBoard(boardId, EDIT_STICKY) })
        { Issue.Board.NotFound() }.column = event.newColumn
}

context(_: Raise<Issue>)
fun UserResponseModel.updateSticky(
    event: WsAction.StickyUpdate,
    boardId: Uuid,
) = data {
    val board = getAccessibleBoard(boardId, EDIT_STICKY)
    val sticky =
        ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) {
            Issue.Board.NotFound()
        }.apply {
            title = event.title
            content = event.content ?: ""
        }

    val currentAssignments = sticky.assignedUsers.toList()
    val newAssignments = event.assignedUsers

    currentAssignments.forEach { old ->
        if (newAssignments.none { it.userId == old.user.id.value }) {
            old.delete()
        }
    }

    newAssignments.forEach { new ->
        val user = UserEntity.findById(new.userId) ?: return@forEach
        if (board.memberships.any { it.user == user }) {
            val existing = AssignedUserEntity.findById(sticky.id.value, user.id.value)
            if (existing != null) {
                existing.due = new.due
            } else {
                AssignedUserEntity.new(sticky, user, new.due)
            }
        }
    }

    sticky.toModel()
}

context(_: Raise<Issue>)
fun UserResponseModel.assignUser(
    event: WsAction.AssignUser,
    boardId: Uuid,
): Unit =
    data {
        val board = getAccessibleBoard(boardId, EDIT_STICKY)
        val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
        val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
        val old = AssignedUserEntity.findById(sticky.id.value, user.id.value)
        if (old != null) old.due = event.due else AssignedUserEntity.new(sticky, user, event.due)
    }

context(_: Raise<Issue>)
fun UserResponseModel.unassignUser(
    event: WsAction.UnassignUser,
    boardId: Uuid,
) = data {
    val board = getAccessibleBoard(boardId, EDIT_STICKY)
    val sticky = ensureNotNull(StickyNoteEntity.findById(event.stickyId)?.takeIf { it.board == board }) { Issue.Board.NotFound() }
    val user = ensureNotNull(UserEntity.findById(event.userId)) { Issue.User.NotFound() }
    AssignedUserEntity.findById(sticky.id.value, user.id.value)?.delete() ?: raise(Issue.User.NotFound())
}
