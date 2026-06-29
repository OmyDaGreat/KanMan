package xyz.malefic.kanman.api.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import xyz.malefic.kanman.api.register
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Validation.BadRequest
import xyz.malefic.kanman.data.model.RefreshRequestModel.Companion.refresh
import xyz.malefic.kanman.data.model.TokenResponseModel
import xyz.malefic.kanman.data.model.UserRequestModel
import xyz.malefic.kanman.api.login as apilogin

object AuthSession {
    private const val TOKENS_KEY = "kanman_tokens"

    var tokens: TokenResponseModel? by mutableStateOf(
        localStorage[TOKENS_KEY]?.let {
            runCatching { json.decodeFromString<TokenResponseModel>(it) }.getOrNull()
        },
    )
        private set

    val accessToken: String? get() = tokens?.accessToken
    val refreshToken: String? get() = tokens?.refreshToken

    private fun updateTokens(value: TokenResponseModel?) {
        tokens = value
        if (value == null) {
            localStorage.removeItem(TOKENS_KEY)
        } else {
            localStorage[TOKENS_KEY] = json.encodeToString(value)
        }
    }

    suspend fun login(
        username: String,
        password: String,
    ) = either {
        updateTokens(apilogin(UserRequestModel(username, password)).bind())
    }

    suspend fun signup(
        username: String,
        password: String,
    ) = either {
        updateTokens(register(UserRequestModel(username, password)).bind())
    }

    context(_: Raise<Issue>)
    suspend fun tryRefresh() {
        val rt = ensureNotNull(refreshToken) { BadRequest("No refresh token available") }
        updateTokens(post<_, TokenResponseModel>("token/refresh", rt.refresh).onLeft { logout() }.bind())
    }

    suspend fun logout() =
        either {
            xyz.malefic.kanman.api
                .logout()
                ?.bind() ?: Unit
            updateTokens(null)
        }
}

suspend fun <T> apiAuth(
    url: String,
    method: String = "GET",
    body: String? = null,
    block: suspend (Response) -> T,
) = either {
    val headers = { Headers().apply { AuthSession.accessToken?.let { set("Authorization", "Bearer $it") } } }

    val result = api(url, method, body, headers(), block)
    if (result is Either.Left && result.value is Issue.Auth && AuthSession.refreshToken != null) {
        AuthSession.tryRefresh()
        api(url, method, body, headers(), block).bind()
    } else {
        result.bind()
    }
}
