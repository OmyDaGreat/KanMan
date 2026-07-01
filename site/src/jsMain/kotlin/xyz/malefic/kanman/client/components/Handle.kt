package xyz.malefic.kanman.client.components

import arrow.core.Either
import com.varabyte.kobweb.core.PageContext
import js.uri.encodeURIComponent
import kotlinx.browser.window
import xyz.malefic.kanman.shared.data.model.Issue

fun <T> PageContext.handle(
    result: Either<Issue, T>,
    onSuccess: (T) -> Unit = {},
) {
    result.fold(
        { error ->
            when (error) {
                is Issue.Auth -> router.navigateTo("/login?redirect=${encodeURIComponent(route.path)}")
                else -> window.alert("Error: ${error.message}")
            }
        },
        { onSuccess(it) },
    )
}
