package xyz.malefic.kanman.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
)
