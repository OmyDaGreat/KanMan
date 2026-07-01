package xyz.malefic.kanman.server.features.invitation

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import org.jetbrains.exposed.v1.core.eq
import xyz.malefic.kanman.server.data.BoardUserEntity
import xyz.malefic.kanman.server.data.InvitationEntity
import xyz.malefic.kanman.server.data.Invitations
import xyz.malefic.kanman.server.data.UserEntity
import xyz.malefic.kanman.server.data.data
import xyz.malefic.kanman.server.data.entity
import xyz.malefic.kanman.server.features.board.getAccessibleBoard
import xyz.malefic.kanman.shared.data.model.BoardAction.INVITE_USER
import xyz.malefic.kanman.shared.data.model.InviteRequest
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Board.AccessDenied
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.UserResponseModel
import kotlin.uuid.Uuid

context(_: Raise<Issue>)
fun UserResponseModel.getInvites() = data { InvitationEntity.find { Invitations.receiver eq id }.map { it.toModel() } }

context(_: Raise<Issue>)
fun UserResponseModel.invite(
    boardId: Uuid,
    inviteRequest: InviteRequest,
) = data {
    ensure(inviteRequest.role != Role.OWNER) { AccessDenied("A board owner cannot be invited") }

    val board = getAccessibleBoard(boardId, INVITE_USER)
    val addUser = ensureNotNull(UserEntity.findById(inviteRequest.userId)) { Issue.User.NotFound() }

    InvitationEntity.new {
        this.board = board
        sender = entity
        receiver = addUser
        role = inviteRequest.role
    }
}

context(_: Raise<Issue>)
fun UserResponseModel.acceptInvite(inviteId: Uuid) =
    data {
        val invite = ensureNotNull(InvitationEntity.findById(inviteId)) { Issue.Board.NotFound() }

        ensure(invite.receiver.id.value == id) { AccessDenied("You are not invited to this board") }

        BoardUserEntity.new(invite)
        val result = (invite.board.memberships.map { it.user } + invite.receiver).map { it.toSummaryModel() }.distinct()
        invite.delete()
        result
    }

context(_: Raise<Issue>)
fun UserResponseModel.declineInvite(inviteId: Uuid) =
    data {
        val invite = ensureNotNull(InvitationEntity.findById(inviteId)) { Issue.Board.NotFound() }
        ensure(invite.receiver.id.value == id) { AccessDenied("You are not invited to this board") }
        invite.delete()
    }
