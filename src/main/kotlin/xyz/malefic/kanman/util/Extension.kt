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
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens

fun <A> model(
    lens: BiDiBodyLens<A>,
    handler: (Request, A) -> Response,
): HttpHandler =
    REQUEST@{ request ->
        val a =
            try {
                lens(request)
            } catch (e: Exception) {
                return@REQUEST Response(BAD_REQUEST).with(errorLens of "Invalid JSON for request body: $e".error)
            }
        handler(request, a)
    }

fun auth(next: (UserResponseModel) -> Response) =
    auth.then { request ->
        next(currentUser(request) ?: return@then Response(UNAUTHORIZED).with(errorLens of "Authenticated user not found".error))
    }

fun <T> auth(
    lens: BiDiBodyLens<T>,
    next: (UserResponseModel, T) -> Response,
) = auth.then(
    model(lens) REQUEST@{ request, lensRequest ->
        val user = currentUser(request) ?: return@REQUEST Response(UNAUTHORIZED).with(errorLens of "Authenticated user not found".error)
        next(user, lensRequest)
    },
)

fun nowMs(): Long = System.currentTimeMillis()
