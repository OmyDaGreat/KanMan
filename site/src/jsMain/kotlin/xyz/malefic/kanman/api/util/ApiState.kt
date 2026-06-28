package xyz.malefic.kanman.api.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import arrow.core.Either
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.PageContext
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.kanman.components.Spinner
import xyz.malefic.kanman.data.model.Issue

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
fun <T> PageContext.Request(
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
            val error = (state as ApiState.Error).issue
            if (error is Issue.Auth) {
                router.navigateTo("/login")
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error: ${error.message}")
                }
            }
        }

        is ApiState.Success -> {
            content((state as ApiState.Success<T>).data)
        }
    }
}
