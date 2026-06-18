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
}

enum class TokenType {
    ACCESS,
    REFRESH,
}
