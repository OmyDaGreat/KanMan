package xyz.malefic.kanman.api.util

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import xyz.malefic.kanman.api.util.ApiError.HttpError.Companion.error

val json = Json { ignoreUnknownKeys = true }

context(_: Raise<ApiError>)
suspend fun fetch(
    url: String,
    init: RequestInit,
) = Either
    .catchOrThrow<Exception, _> { window.fetch(url, init).await() }
    .mapLeft { ApiError.NetworkError(it.message ?: "Network failure", it) }
    .bind()

suspend fun <T> api(
    url: String,
    method: String = "GET",
    body: String? = null,
    block: suspend (Response) -> T,
) = either {
    val request = { RequestInit(method, Headers().also { it.set("Content-Type", "application/json") }, body) }
    val response = fetch(url, request())

    ensure(response.ok) { response.error() }
    block(response)
}

suspend inline fun <reified T> get(url: String) =
    api(url) { response ->
        response
            .text()
            .await()
            .let { json.decodeFromString<T>(it) }
    }

suspend inline fun <reified T> getAuth(url: String) =
    apiAuth(url) { response ->
        response
            .text()
            .await()
            .let { json.decodeFromString<T>(it) }
    }

suspend inline fun <reified T, reified R> post(
    url: String,
    body: T,
) = api(url, method = "POST", body = json.encodeToString(body)) { response ->
    response
        .text()
        .await()
        .let { json.decodeFromString<R>(it) }
}

suspend inline fun <reified T, reified R> postAuth(
    url: String,
    body: T,
) = apiAuth(url, method = "POST", body = json.encodeToString(body)) { response ->
    response
        .text()
        .await()
        .let { json.decodeFromString<R>(it) }
}

suspend inline fun <reified T> post(
    url: String,
    body: T,
) = api(url, method = "POST", body = json.encodeToString(body)) { }

suspend inline fun <reified T> postAuth(
    url: String,
    body: T,
) = apiAuth(url, method = "POST", body = json.encodeToString(body)) { }

suspend fun delete(url: String) = api(url, method = "DELETE") { }

suspend fun deleteAuth(url: String) = apiAuth(url, method = "DELETE") { }
