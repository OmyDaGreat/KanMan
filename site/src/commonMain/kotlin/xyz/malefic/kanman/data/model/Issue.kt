package xyz.malefic.kanman.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed class Issue : Error() {
    abstract override val message: String

    @Serializable
    sealed class Auth : Issue() {
        @Serializable
        data class InvalidCredentials(
            override val message: String = "Invalid username or password",
        ) : Auth()

        @Serializable
        data class InvalidToken(
            override val message: String = "Invalid or expired token",
        ) : Auth()

        @Serializable
        data class AccountLocked(
            val unlockAt: Long,
        ) : Auth() {
            override val message = "Account locked until $unlockAt"
        }

        @Serializable
        data class MissingToken(
            override val message: String = "Missing authentication token",
        ) : Auth()
    }

    @Serializable
    sealed class User : Issue() {
        @Serializable
        data class AlreadyExists(
            override val message: String = "User already exists",
        ) : User()

        @Serializable
        data class NotFound(
            override val message: String = "User not found",
        ) : User()
    }

    @Serializable
    sealed class Access : Issue() {
        @Serializable
        data class Forbidden(
            override val message: String = "Access forbidden",
        ) : Access()
    }

    @Serializable
    sealed class Board : Issue() {
        @Serializable
        data class NotFound(
            override val message: String = "Board not found",
        ) : Board()

        @Serializable
        data class AccessDenied(
            override val message: String = "Access denied to boards",
        ) : Board()

        @Serializable
        data class InvalidId(
            override val message: String = "Invalid boards ID",
        ) : Board()
    }

    @Serializable
    sealed class Validation : Issue() {
        @Serializable
        data class BadRequest(
            override val message: String,
        ) : Validation()

        @Serializable
        data class BadResponse(
            override val message: String,
        ) : Validation()
    }

    @Serializable
    sealed class Server : Issue() {
        @Serializable
        data class Internal(
            override val message: String = "Internal server error",
            @SerialName("cause") val trace: String? = null,
        ) : Server() {
            companion object {
                fun from(
                    e: Throwable,
                    message: String = "Internal server error",
                ) = e as? Issue ?: Internal(e.message ?: message, e.stackTraceToString())
            }
        }

        @Serializable
        data class Conflict(
            override val message: String,
        ) : Server()

        @Serializable
        data class RateLimited(
            @SerialName("retry_after_ms") val retryAfterMs: Duration? = null,
        ) : Server() {
            override val message = "Rate limited" + (retryAfterMs?.let { " (retry after $it)" } ?: "")
        }
    }

    @Serializable
    sealed class Client : Issue() {
        @Serializable
        data class Network(
            override val message: String,
        ) : Client()
    }
}
