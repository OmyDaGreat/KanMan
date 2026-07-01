package xyz.malefic.kanman.server

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.filter.AllowAllOriginPolicy
import org.http4k.filter.CorsPolicy
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.days

val corsPolicy =
    CorsPolicy(
        AllowAllOriginPolicy,
        listOf("Content-Type", "Authorization"),
        listOf(GET, POST, DELETE, PATCH),
        true,
        maxAge = 1.days.inWholeSeconds.toInt(),
    )

val mimeTypes =
    mapOf(
        "html" to "text/html; charset=utf-8",
        "js" to "application/javascript",
        "css" to "text/css",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "svg" to "image/svg+xml",
        "ico" to "image/x-icon",
        "webp" to "image/webp",
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "json" to "application/json",
    )

val staticRoots: List<Path> by lazy {
    listOf(
        Paths.get("build", "dist", "js", "productionExecutable"),
        Paths.get("build", "dist", "js", "productionExecutable", "public"),
        Paths.get("/app", "site", "build", "dist", "js", "productionExecutable"),
        Paths.get("/app", "site", "build", "dist", "js", "productionExecutable", "public"),
    ).filter { Files.isDirectory(it) }
}
