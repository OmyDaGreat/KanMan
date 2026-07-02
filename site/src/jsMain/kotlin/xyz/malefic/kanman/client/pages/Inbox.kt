package xyz.malefic.kanman.client.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import xyz.malefic.kanman.client.api.getInvitations
import xyz.malefic.kanman.client.api.util.Request

@Page
@Composable
fun Inbox(ctx: PageContext) =
    ctx.Request(request = { getInvitations() }) {
    }
