package xyz.malefic.kanman.api

import kotlinx.coroutines.await
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

suspend inline fun <reified T> getApi(url: String) =
    withAuth(url) { response ->
        response
            .text()
            .await()
            .let { json.decodeFromString<T>(it) }
    }

suspend inline fun <reified T, reified R> postApi(
    url: String,
    body: T,
) = withAuth(url, method = "POST", body = json.encodeToString(body)) { response ->
    response
        .text()
        .await()
        .let { json.decodeFromString<R>(it) }
}

suspend inline fun <reified T> postApi(
    url: String,
    body: T,
) = withAuth(url, method = "POST", body = json.encodeToString(body)) { }

suspend fun deleteApi(url: String) = withAuth(url, method = "DELETE") { }
