package xyz.malefic.kanman.client.api.util

import androidx.compose.runtime.mutableStateOf
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import co.touchlab.kermit.Logger
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set
import xyz.malefic.kanman.client.api.logout
import xyz.malefic.kanman.client.api.refresh
import xyz.malefic.kanman.client.api.register
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.UserCreateModel
import xyz.malefic.kanman.shared.data.model.UserLoginModel
import xyz.malefic.kanman.client.api.login as apilogin

object AuthSession {
    private const val TOKEN_KEY = "kanman_token"

    private val _accessToken = mutableStateOf(localStorage[TOKEN_KEY])

    init {
        ApiConfig.accessToken = { accessToken }
        ApiConfig.onAuthFailure = { either { tryRefresh() } }
    }

    var accessToken: String?
        get() = _accessToken.value
        private set(value) {
            _accessToken.value = value
            if (value == null) {
                localStorage.removeItem(TOKEN_KEY)
            } else {
                localStorage[TOKEN_KEY] = value
            }
        }

    suspend fun login(
        username: String,
        password: String,
    ) = either { accessToken = apilogin(UserLoginModel(username, password)).bind().accessToken }

    suspend fun signup(
        username: String,
        email: String,
        password: String,
    ) = either { accessToken = register(UserCreateModel(username, email, password)).bind().accessToken }

    context(_: Raise<Issue>)
    suspend fun tryRefresh() {
        accessToken = refresh().onLeft { if (it is Issue.Auth) signout() }.bind().accessToken
    }

    suspend fun signout() {
        logout().onLeft { Logger.e(it) { "Failed to log out" } }
        accessToken = null
    }
}
