package xyz.malefic.kanman.data.model

import kotlinx.serialization.json.Json

val json =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
