package xyz.malefic.kanman.util

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import xyz.malefic.kanman.data.UserResponseModel
import xyz.malefic.kanman.data.Visibility
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.errorModel

fun <A> model(
    lens: BiDiBodyLens<A>,
    handler: (Request, A) -> Response,
): HttpHandler =
    REQUEST@{ request ->
        val a =
            try {
                lens(request)
            } catch (e: Exception) {
                return@REQUEST Response(BAD_REQUEST).with("Invalid JSON for request body: $e".error)
            }
        handler(request, a)
    }

fun auth(next: (UserResponseModel, Request) -> Response) =
    auth.then { request ->
        next(
            currentUser(request) ?: return@then Response(UNAUTHORIZED).with("Authenticated user not found".error),
            request,
        )
    }

fun <T> auth(
    lens: BiDiBodyLens<T>,
    next: (UserResponseModel, T) -> Response,
) = auth.then(
    model(lens) REQUEST@{ request, lensRequest ->
        val user =
            currentUser(request) ?: return@REQUEST Response(UNAUTHORIZED).with("Authenticated user not found".error)
        next(user, lensRequest)
    },
)

fun nowMs(): Long = System.currentTimeMillis()

val String.toVisibility
    get() =
        try {
            Visibility.valueOf(this.uppercase())
        } catch (_: Exception) {
            null
        }

val String.error: (Response) -> Response
    get() = errorLens.of(this.errorModel)
