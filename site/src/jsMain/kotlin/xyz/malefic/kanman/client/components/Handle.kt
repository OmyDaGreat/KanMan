package xyz.malefic.kanman.client.components

import arrow.core.Either
import xyz.malefic.kanman.client.api.util.GlobalErrorState
import xyz.malefic.kanman.shared.data.model.Issue

inline fun <T> handle(
    result: Either<Issue, T>,
    onSuccess: (T) -> Unit = {},
) {
    result.fold(
        { GlobalErrorState.show(it) },
        { onSuccess(it) },
    )
}
