package xyz.malefic.kanman.shared.api.util

import kotlinx.serialization.json.Json

val json =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
