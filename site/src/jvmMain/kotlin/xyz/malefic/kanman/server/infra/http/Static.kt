package xyz.malefic.kanman.server.infra.http

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import xyz.malefic.kanman.server.mimeTypes
import xyz.malefic.kanman.server.staticRoots
import java.nio.file.Files

fun serveStaticFile(req: Request): Response {
    val requestPath = req.uri.path.removePrefix("/")

    val ext = requestPath.substringAfterLast('.', "")
    val target = if (requestPath.isBlank() || ext.isBlank()) "index.html" else requestPath
    val contentType = mimeTypes.getOrDefault(ext.lowercase(), "text/html; charset=utf-8")

    for (root in staticRoots) {
        val file = root.resolve(target).normalize()
        if (!file.startsWith(root)) return Response.Companion(Status.NOT_FOUND)
        if (Files.isRegularFile(file)) {
            val bytes = Files.readAllBytes(file)
            return Response
                .Companion(Status.OK)
                .header("Content-Type", contentType)
                .body(bytes.inputStream(), bytes.size.toLong())
        }
    }

    return Response.Companion(Status.NOT_FOUND)
}
