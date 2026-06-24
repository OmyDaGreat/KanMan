package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("issue")
sealed class Issue : Error() {
    abstract override val message: String

    @Serializable
    @SerialName("auth")
    sealed class Auth : Issue() {
        @Serializable
        @SerialName("invalid_credentials")
        data class InvalidCredentials(
            override val message: String = "Invalid username or password",
        ) : Auth()

        @Serializable
        @SerialName("invalid_token")
        data class InvalidToken(
            override val message: String = "Invalid or expired token",
        ) : Auth()

        @Serializable
        @SerialName("account_locked")
        data class AccountLocked(
            val unlockAt: Long,
        ) : Auth() {
            override val message = "Account locked until $unlockAt"
        }

        @Serializable
        @SerialName("missing_token")
        data class MissingToken(
            override val message: String = "Missing authentication token",
        ) : Auth()
    }

    @Serializable
    @SerialName("user")
    sealed class User : Issue() {
        @Serializable
        @SerialName("already_exists")
        data class AlreadyExists(
            override val message: String = "User already exists",
        ) : User()

        @Serializable
        @SerialName("not_found")
        data class NotFound(
            override val message: String = "User not found",
        ) : User()
    }

    @Serializable
    @SerialName("access")
    sealed class Access : Issue() {
        @Serializable
        @SerialName("forbidden")
        data class Forbidden(
            override val message: String = "Access forbidden",
        ) : Access()
    }

    @Serializable
    @SerialName("board")
    sealed class Board : Issue() {
        @Serializable
        @SerialName("not_found")
        data class NotFound(
            override val message: String = "Board not found",
        ) : Board()

        @Serializable
        @SerialName("access_denied")
        data class AccessDenied(
            override val message: String = "Access denied to board",
        ) : Board()

        @Serializable
        @SerialName("invalid_id")
        data class InvalidId(
            override val message: String = "Invalid board ID",
        ) : Board()
    }

    @Serializable
    @SerialName("validation")
    sealed class Validation : Issue() {
        @Serializable
        @SerialName("bad_request")
        data class BadRequest(
            override val message: String,
        ) : Validation()

        @Serializable
        @SerialName("bad_response")
        data class BadResponse(
            override val message: String,
        ) : Validation()
    }

    @Serializable
    @SerialName("server")
    sealed class Server : Issue() {
        @Serializable
        @SerialName("internal")
        data class Internal(
            override val message: String = "Internal server error",
        ) : Server()

        @Serializable
        @SerialName("conflict")
        data class Conflict(
            override val message: String,
        ) : Server()

        @Serializable
        @SerialName("rate_limited")
        data class RateLimited(
            val retryAfterMs: Long? = null,
        ) : Server() {
            override val message = "Rate limited" + (retryAfterMs?.let { " (retry after ${it}ms)" } ?: "")
        }
    }

    @Serializable
    @SerialName("client")
    sealed class Client : Issue() {
        @Serializable
        @SerialName("network")
        data class Network(
            override val message: String,
        ) : Client()
    }
}
