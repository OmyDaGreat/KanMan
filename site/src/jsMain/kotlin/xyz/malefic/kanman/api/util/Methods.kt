package xyz.malefic.kanman.api.util

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
import xyz.malefic.kanman.data.model.Issue
import xyz.malefic.kanman.data.model.Issue.Client.Network
import xyz.malefic.kanman.data.model.Issue.Server.Internal
import xyz.malefic.kanman.data.model.Issue.Validation.BadResponse

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

suspend inline fun <reified R> post(url: String) =
    api(url, method = "POST") { response ->
        response
            .text()
            .await()
            .let { json.decodeFromString<R>(it) }
    }

suspend inline fun <reified R> postAuth(url: String) =
    apiAuth(url, method = "POST") { response ->
        response
            .text()
            .await()
            .let { json.decodeFromString<R>(it) }
    }

suspend inline fun post(url: String) = api(url, method = "POST") { }

suspend inline fun postAuth(url: String) = apiAuth(url, method = "POST") { }

suspend fun delete(url: String) = api(url, method = "DELETE") { }

suspend fun deleteAuth(url: String) = apiAuth(url, method = "DELETE") { }
