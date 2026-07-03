package xyz.malefic.kanman.client.api

import xyz.malefic.kanman.client.api.util.deleteAuth
import xyz.malefic.kanman.client.api.util.getAuth
import xyz.malefic.kanman.client.api.util.postAuth
import xyz.malefic.kanman.shared.data.model.Invitation
import xyz.malefic.kanman.shared.data.model.InviteRequest
import xyz.malefic.kanman.shared.data.model.Role
import xyz.malefic.kanman.shared.data.model.UserSummaryModel
import kotlin.uuid.Uuid

suspend fun getInvitations() = getAuth<List<Invitation>>("invitations")

suspend fun invite(
    boardId: Uuid,
    userId: Uuid,
    role: Role,
) = postAuth<InviteRequest, Invitation>("invitations", InviteRequest(boardId, userId, role))

suspend fun acceptInvitation(id: Uuid) = postAuth<List<UserSummaryModel>>("invitations/$id/accept")

suspend fun declineInvitation(id: Uuid) = deleteAuth("invitations/$id")
