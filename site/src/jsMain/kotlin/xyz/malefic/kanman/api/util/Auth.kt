package xyz.malefic.kanman.api.util

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import xyz.malefic.kanman.api.util.ApiError.HttpError.Companion.error
import xyz.malefic.kanman.data.model.TokenResponseModel

object AuthSession {
    private const val TOKENS_KEY = "kanman_tokens"

    var tokens: TokenResponseModel?
        get() = localStorage[TOKENS_KEY]?.let { json.decodeFromString<TokenResponseModel>(it) }
        set(value) {
            if (value == null) {
                localStorage.removeItem(TOKENS_KEY)
            } else {
                localStorage[TOKENS_KEY] = json.encodeToString(TokenResponseModel.serializer(), value)
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
) = either {
    val request = {
        RequestInit(
            method,
            Headers().also {
                it.set("Content-Type", "application/json")
                AuthSession.accessToken?.let { token -> it.set("Authorization", "Bearer $token") }
            },
            body,
        )
    }
    val initial = fetch(url, request())
    val final =
        if (initial.status == 401.toShort() && AuthSession.refreshToken != null) {
            tryRefresh().fold(
                {
                    AuthSession.logout()
                    raise(initial.error())
                },
                { fetch(url, request()) },
            )
        } else {
            initial
        }

    ensure(final.ok) { final.error() }
    block(final)
}

private suspend fun tryRefresh() =
    either {
        val rt = AuthSession.refreshToken ?: raise(ApiError.AuthError("No refresh token available"))
        val response =
            fetch(
                "/api/token/refresh",
                RequestInit(
                    "POST",
                    Headers().also { it.set("Content-Type", "application/json") },
                    json.encodeToString(rt),
                ),
            )
        ensure(response.ok) { response.error() }
        Either
            .catchOrThrow<Exception, _> {
                val result =
                    response
                        .text()
                        .await()
                        .let { json.decodeFromString<TokenResponseModel>(it) }
                AuthSession.tokens = result
            }.mapLeft { ApiError.AuthError("Failed to parse refresh response", it) }
            .bind()
    }
