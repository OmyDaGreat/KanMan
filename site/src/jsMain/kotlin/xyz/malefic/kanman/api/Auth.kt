package xyz.malefic.kanman.api

import co.touchlab.kermit.Logger
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import xyz.malefic.kanman.data.model.TokenResponseModel

object AuthSession {
    private const val TOKENS_KEY = "kanman_tokens"
    private val json = Json { ignoreUnknownKeys = true }

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

suspend fun <T> withAuth(
    url: String,
    method: String = "GET",
    body: String? = null,
    block: suspend (Response) -> T,
): T {
    val request = {
        RequestInit(
            method = method,
            headers =
                Headers().also {
                    it.set("Content-Type", "application/json")
                    AuthSession.accessToken?.let { token -> it.set("Authorization", "Bearer $token") }
                },
            body = body,
        )
    }

    var response = window.fetch(url, request()).await()

    if (response.status == 401.toShort() && AuthSession.refreshToken != null) {
        if (tryRefresh()) {
            response = window.fetch(url, request()).await()
        } else {
            AuthSession.logout()
        }
    }

    if (!response.ok) {
        val errorText = response.text().await()
        throw Exception("API call failed with status ${response.status}: $errorText")
    }

    return block(response)
}

private suspend fun tryRefresh(): Boolean {
    val rt = AuthSession.refreshToken ?: return false
    return try {
        val requestInit =
            RequestInit(
                method = "POST",
                headers = Headers().also { it.set("Content-Type", "application/json") },
                body = json.encodeToString(rt),
            )

        val response = window.fetch("/api/token/refresh", requestInit).await()
        if (response.ok) {
            val result =
                response
                    .text()
                    .await()
                    .let { json.decodeFromString(TokenResponseModel.serializer(), it) }
            AuthSession.tokens = result
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Logger.e(e) { "Failed to refresh token" }
        false
    }
}
