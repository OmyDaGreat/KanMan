package xyz.malefic.kanman.client.api.util

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import xyz.malefic.kanman.shared.api.util.json
import xyz.malefic.kanman.shared.data.model.Issue
import xyz.malefic.kanman.shared.data.model.Issue.Client.Network
import xyz.malefic.kanman.shared.data.model.Issue.Server.Internal
import xyz.malefic.kanman.shared.data.model.Issue.Validation.BadResponse

object ApiConfig {
    var accessToken: () -> String? = { null }
    var onAuthFailure: suspend () -> Either<Issue, Unit> = { either { } }
}

suspend inline fun <reified T> Response.decode(): T {
    val text = text().await()
    return if (T::class == Unit::class) {
        Unit as T
    } else {
        json.decodeFromString<T>(text)
    }
}

suspend fun Response.error() =
    try {
        json.decodeFromString<Issue>(text().await())
    } catch (e: Exception) {
        Internal.from(e)
    }

context(_: Raise<Issue>)
suspend fun fetch(
    url: String,
    init: RequestInit,
) = Either
    .catch { window.fetch(url, init).await() }
    .mapLeft { Network(it.message ?: "Network failure") }
    .bind()

suspend fun <T> api(
    url: String,
    method: String = "GET",
    body: String? = null,
    headers: Headers = Headers(),
    block: suspend (Response) -> T,
) = either {
    headers.set("Content-Type", "application/json")
    val response = fetch("/api/$url", RequestInit(method, headers, body))

    ensure(response.ok) { response.error() }
    catch({ block(response) }) { raise(BadResponse("Invalid JSON for response body: ${it.message}")) }
}

suspend fun <T> apiAuth(
    url: String,
    method: String = "GET",
    body: String? = null,
    block: suspend (Response) -> T,
) = either {
    val getHeaders = {
        Headers().apply {
            ApiConfig.accessToken()?.let { set("Authorization", "Bearer $it") }
        }
    }

    val result = api(url, method, body, getHeaders(), block)
    if (result is Either.Left && result.value is Issue.Auth && ApiConfig.accessToken() != null) {
        ApiConfig.onAuthFailure().bind()
        api(url, method, body, getHeaders(), block).bind()
    } else {
        result.bind()
    }
}

// GET
suspend inline fun <reified T> get(url: String) = api(url) { it.decode<T>() }

suspend inline fun <reified T> getAuth(url: String) = apiAuth(url) { it.decode<T>() }

// POST
suspend inline fun <reified T, reified R> post(
    url: String,
    body: T,
) = api(url, "POST", json.encodeToString(body)) { it.decode<R>() }

suspend inline fun <reified T, reified R> postAuth(
    url: String,
    body: T,
) = apiAuth(url, "POST", json.encodeToString(body)) { it.decode<R>() }

suspend inline fun <reified R> post(url: String) = api(url, "POST") { it.decode<R>() }

suspend inline fun <reified R> postAuth(url: String) = apiAuth(url, "POST") { it.decode<R>() }

suspend inline fun <reified T> post(
    url: String,
    body: T,
) = api(url, "POST", json.encodeToString(body)) { }

suspend inline fun <reified T> postAuth(
    url: String,
    body: T,
) = apiAuth(url, "POST", json.encodeToString(body)) { }

suspend fun post(url: String) = api(url, "POST") { }

suspend fun postAuth(url: String) = apiAuth(url, "POST") { }

// PATCH
suspend inline fun <reified T, reified R> patch(
    url: String,
    body: T,
) = api(url, "PATCH", json.encodeToString(body)) { it.decode<R>() }

suspend inline fun <reified T, reified R> patchAuth(
    url: String,
    body: T,
) = apiAuth(url, "PATCH", json.encodeToString(body)) { it.decode<R>() }

suspend inline fun <reified T> patch(
    url: String,
    body: T,
) = api(url, "PATCH", json.encodeToString(body)) { }

suspend inline fun <reified T> patchAuth(
    url: String,
    body: T,
) = apiAuth(url, "PATCH", json.encodeToString(body)) { }

// DELETE
suspend fun delete(url: String) = api(url, "DELETE") { }

suspend fun deleteAuth(url: String) = apiAuth(url, "DELETE") { }
