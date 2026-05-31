package xyz.malefic.kanman.util

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import xyz.malefic.kanman.data.LoginModel
import xyz.malefic.kanman.data.error
import xyz.malefic.kanman.data.errorLens
import xyz.malefic.kanman.data.loginLens

fun auth(handler: (Request, LoginModel) -> Response): HttpHandler = model(loginLens, handler)

fun <T> model(
    dataLens: BiDiBodyLens<T>,
    handler: (Request, T) -> Response,
): HttpHandler =
    REQUEST@{ request ->
        val dataOrError =
            try {
                dataLens(request)
            } catch (e: Exception) {
                return@REQUEST Response(BAD_REQUEST)
                    .with(errorLens of "Invalid JSON for request body: $e".error)
            }
        handler(request, dataOrError)
    }
