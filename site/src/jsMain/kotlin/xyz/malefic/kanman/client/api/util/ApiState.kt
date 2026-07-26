package xyz.malefic.kanman.client.api.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import arrow.core.Either
import xyz.malefic.kanman.client.components.Spinner
import xyz.malefic.kanman.shared.data.model.Issue

sealed interface ApiState<out T> {
    data object Loading : ApiState<Nothing>

    data class Success<T>(
        val data: T,
    ) : ApiState<T>

    data class Error(
        val issue: Issue,
    ) : ApiState<Nothing>
}

@Composable
fun <T> produceApiState(
    vararg keys: Any?,
    block: suspend () -> Either<Issue, T>,
) = produceState<ApiState<T>>(ApiState.Loading, *keys) {
    value = ApiState.Loading
    block().fold(
        { value = ApiState.Error(it) },
        { value = ApiState.Success(it) },
    )
}

@Composable
fun <T> Request(
    vararg keys: Any?,
    request: suspend () -> Either<Issue, T>,
    content: @Composable (T) -> Unit,
) {
    val state by produceApiState(*keys) { request() }
    when (state) {
        is ApiState.Loading -> {
            Spinner()
        }

        is ApiState.Error -> {
            GlobalErrorState.show((state as ApiState.Error).issue)
        }

        is ApiState.Success -> {
            content((state as ApiState.Success<T>).data)
        }
    }
}
