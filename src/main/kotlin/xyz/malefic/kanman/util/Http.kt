package xyz.malefic.kanman.util

import co.touchlab.kermit.Logger
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import xyz.malefic.kanman.data.UserResponseModel
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.errorModel
import xyz.malefic.kanman.data.transaction.currentUser

fun catch(
    message: String,
    func: HttpHandler,
) = { request: Request ->
    try {
        func(request)
    } catch (e: Exception) {
        Logger.e(e, "HTTP") { message }
        Response(INTERNAL_SERVER_ERROR).with(message.error)
    }
}

fun catchPlus(
    message: String,
    func: () -> HttpHandler,
) = catch(message, func())

inline fun <reified A : Any> model(crossinline handler: (Request, A) -> Response): HttpHandler =
    REQUEST@{ request ->
        val a =
            try {
                lens<A>()(request)
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

inline fun <reified T : Any> auth(crossinline next: (UserResponseModel, T) -> Response) =
    auth.then(
        model<T> REQUEST@{ request, lensRequest ->
            val user = currentUser(request) ?: return@REQUEST Response(UNAUTHORIZED).with("Authenticated user not found".error)
            next(user, lensRequest)
        },
    )

val String.error: (Response) -> Response
    get() = errorLens.of(this.errorModel)

inline fun <reified T : Any> lens() = Body.auto<T>().toLens()
