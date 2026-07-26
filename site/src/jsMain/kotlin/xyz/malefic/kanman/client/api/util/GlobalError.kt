package xyz.malefic.kanman.client.api.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import xyz.malefic.kanman.shared.data.model.Issue

object GlobalErrorState {
    var currentIssue by mutableStateOf<Issue?>(null)
        private set

    fun show(issue: Issue) {
        currentIssue = issue
    }

    fun clear() {
        currentIssue = null
    }
}
