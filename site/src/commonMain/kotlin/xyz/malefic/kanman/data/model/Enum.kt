package xyz.malefic.kanman.data.model

enum class Column {
    BACKLOG,
    PLANNING,
    IN_PROGRESS,
    DONE,
}

enum class Visibility {
    PUBLIC,
    PRIVATE,
    ;

    companion object {
        val String.toVisibility
            get() =
                try {
                    Visibility.valueOf(this.uppercase().trim())
                } catch (_: Exception) {
                    null
                }
    }
}
