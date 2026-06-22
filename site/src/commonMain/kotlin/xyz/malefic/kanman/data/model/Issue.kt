package xyz.malefic.kanman.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Issue : Error() {
    abstract override val message: String

    @Serializable
    sealed class Server : Issue() {
        @Serializable
        data class Unauthorized(
            override val message: String,
        ) : Server()

        @Serializable
        data class NotFound(
            override val message: String,
        ) : Server()

        @Serializable
        data class Internal(
            override val message: String = "Internal server error",
        ) : Server()

        @Serializable
        data class RateLimited(
            override val message: String,
        ) : Server()

        @Serializable
        data class BadRequest(
            override val message: String,
        ) : Server()

        @Serializable
        data class Conflict(
            override val message: String,
        ) : Server()

        @Serializable
        data class Forbidden(
            override val message: String,
        ) : Server()
    }

    @Serializable
    sealed class Client : Issue() {
        @Serializable
        data class Network(
            override val message: String,
        ) : Client()

        @Serializable
        data class Auth(
            override val message: String,
        ) : Client()
    }
}
