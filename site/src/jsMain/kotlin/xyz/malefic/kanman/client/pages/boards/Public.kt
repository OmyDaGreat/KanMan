package xyz.malefic.kanman.client.pages.boards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import kotlinx.coroutines.launch
import xyz.malefic.kanman.client.api.acceptInvitation
import xyz.malefic.kanman.client.api.getPublicBoards
import xyz.malefic.kanman.client.api.getUser
import xyz.malefic.kanman.client.api.invite
import xyz.malefic.kanman.client.components.PaginatedBoards
import xyz.malefic.kanman.client.components.handle
import xyz.malefic.kanman.shared.data.model.Role.GUEST

@Page
@Composable
fun Public(ctx: PageContext) =
    with(ctx) {
        val scope = rememberCoroutineScope()

        PaginatedBoards("Public Boards", { page, limit -> getPublicBoards(page, limit) }) { board ->
            scope.launch {
                val result =
                    either {
                        val user = getUser().bind()
                        val invitation = invite(board.id, user.id, GUEST).bind()
                        val _ = acceptInvitation(invitation.id).bind()
                    }
                handle(result) { router.navigateTo("/boards/${board.id}") }
            }
        }
    }
