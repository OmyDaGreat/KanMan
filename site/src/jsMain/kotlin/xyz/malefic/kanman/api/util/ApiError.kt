package xyz.malefic.kanman.api.util

import kotlinx.coroutines.await
import org.w3c.fetch.Response

sealed class ApiError(
    override val message: String,
) : Error(message) {
    class HttpError(
        val status: Short,
        override val message: String,
    ) : ApiError(message) {
        companion object {
            suspend fun Response.error() = HttpError(status, text().await())
        }
    }

    class NetworkError(
        override val message: String,
        override val cause: Exception? = null,
    ) : ApiError(message)

    class AuthError(
        override val message: String,
        override val cause: Exception? = null,
    ) : ApiError(message)
}
