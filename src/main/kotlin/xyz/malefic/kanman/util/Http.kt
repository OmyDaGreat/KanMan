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
import xyz.malefic.kanman.data.ErrorModel
import xyz.malefic.kanman.data.UserResponseModel
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
    request@{ request ->
        val a =
            try {
                lens<A>()(request)
            } catch (e: Exception) {
                return@request Response(BAD_REQUEST).with("Invalid JSON for request body: $e".error)
            }
        handler(request, a)
    }

fun authRequest(next: Request.(UserResponseModel) -> Response) =
    auth.then { request ->
        request.next(
            currentUser(request) ?: return@then Response(UNAUTHORIZED).with("Authenticated user not found".error),
        )
    }

inline fun <reified T : Any> authModel(crossinline next: (UserResponseModel, T) -> Response) =
    auth.then(
        model<T> model@{ request, lensRequest ->
            val user = currentUser(request) ?: return@model Response(UNAUTHORIZED).with("Authenticated user not found".error)
            next(user, lensRequest)
        },
    )

val String.error: (Response) -> Response
    get() = value(ErrorModel(this))

inline fun <reified T : Any> lens() = Body.auto<T>().toLens()

inline fun <reified T : Any> value(obj: T) = lens<T>().of<Response>(obj)
