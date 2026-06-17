package xyz.malefic.kanman.util

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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

fun serveStaticFile(req: Request): Response {
    val requestPath = req.uri.path.removePrefix("/")

    val ext = requestPath.substringAfterLast('.', "")
    val target = if (requestPath.isBlank() || ext.isBlank()) "index.html" else requestPath
    val contentType = mimeTypes.getOrDefault(ext.lowercase(), "text/html; charset=utf-8")

    for (root in staticRoots) {
        val file = root.resolve(target).normalize()
        if (!file.startsWith(root)) return Response(NOT_FOUND)
        if (Files.isRegularFile(file)) {
            val bytes = Files.readAllBytes(file)
            return Response(OK)
                .header("Content-Type", contentType)
                .body(bytes.inputStream(), bytes.size.toLong())
        }
    }

    return Response(NOT_FOUND)
}
