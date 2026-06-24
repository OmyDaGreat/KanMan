package xyz.malefic.kanman.api.util

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensureNotNull
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.RefreshRequestModel.Companion.refresh
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.json

object AuthSession {
    private const val TOKENS_KEY = "kanman_tokens"

    var tokens: TokenResponseModel?
        get() = localStorage[TOKENS_KEY]?.let { json.decodeFromString<TokenResponseModel>(it) }
        set(value) {
            if (value == null) {
                localStorage.removeItem(TOKENS_KEY)
            } else {
                localStorage[TOKENS_KEY] = json.encodeToString(value)
            }
        }

    val accessToken: String? get() = tokens?.accessToken
    val refreshToken: String? get() = tokens?.refreshToken
    val isLoggedIn: Boolean get() = accessToken != null

    fun logout() {
        tokens = null
    }
}

suspend fun <T> apiAuth(
    url: String,
    method: String = "GET",
    body: String? = null,
    block: suspend (Response) -> T,
): Either<Issue, T> =
    either {
        val headers = {
            Headers().apply {
                AuthSession.accessToken?.let { set("Authorization", "Bearer $it") }
            }
        }

        val result = api(url, method, body, headers(), block)
        if (result is Either.Left && result.value is Issue.Auth && AuthSession.refreshToken != null) {
            tryRefresh()
            api(url, method, body, headers(), block).bind()
        } else {
            result.bind()
        }
    }

context(_: Raise<Issue>)
private suspend fun tryRefresh() {
    val rt = ensureNotNull(AuthSession.refreshToken) { BadRequest("No refresh token available") }
    AuthSession.tokens = post<_, TokenResponseModel>("token/refresh", rt.refresh).onLeft { AuthSession.logout() }.bind()
}
