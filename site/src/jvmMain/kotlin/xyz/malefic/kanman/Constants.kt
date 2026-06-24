package xyz.malefic.kanman

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.filter.AllowAllOriginPolicy
import org.http4k.filter.CorsPolicy

val corsPolicy =
    CorsPolicy(
        headers = listOf("Content-Type", "Authorization"),
        methods = listOf(GET, POST, DELETE),
        originPolicy = AllowAllOriginPolicy,
    )
