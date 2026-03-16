package cn.bg7qvu.mtrwebctc.server;

import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.io.*
import java.nio.file.*
import java.util.*

/**
 * 静态资源服务
 */
public class StaticResourceHandler(
    private val resourceMode: String,
    private val configDir: Path
) {
    // 静态资源 MIME 类型映射
    private val mimeTypes = mapOf(
        "html" to ContentType.Text.Html,
        "css" to ContentType.Text.Css,
        "js" to ContentType.Application.JavaScript,
        "json" to ContentType.Application.Json,
        "png" to ContentType.Image.PNG,
        "jpg" to ContentType.Image.JPEG,
        "jpeg" to ContentType.Image.JPEG,
        "gif" to ContentType.Image.GIF,
        "svg" to ContentType.Image.SVG,
        "ico" to ContentType.Image.XIcon,
        "woff" to ContentType("font", "woff"),
        "woff2" to ContentType("font", "woff2"),
        "ttf" to ContentType("font", "ttf"),
        "eot" to ContentType("application", "vnd.ms-fontobject")
    )
    
    public fun register(route: Route) {
        when (resourceMode.lowercase()) {
            "embedded" -> serveEmbedded(route)
            "external" -> serveExternal(route)
            else -> serveDefault(route)
        }
    }
    
    /**
     * 从 JAR 内嵌资源服务
     */
    private fun serveEmbedded(route: Route) {
        route.get("/{...}") {
            val path = call.parameters.getAll("...")?.joinToString("/") ?: "index.html"
            serveResource(path)
        }
    }
    
    /**
     * 从外部目录服务
     */
    private fun serveExternal(route: Route) {
        val webDir = configDir.resolve("web")
        
        route.get("/{...}") {
            val path = call.parameters.getAll("...")?.joinToString("/") ?: "index.html"
            val file = webDir.resolve(path)
            
            if (Files.exists(file) && !Files.isDirectory(file)) {
                val contentType = getContentType(file.fileName.toString())
                call.respondBytes(Files.readAllBytes(file), contentType)
            } else {
                // 尝试 index.html (SPA 路由)
                val indexFile = webDir.resolve("index.html")
                if (Files.exists(indexFile)) {
                    call.respondBytes(Files.readAllBytes(indexFile), ContentType.Text.Html)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
    
    /**
     * 默认页面（无前端时）
     */
    private fun serveDefault(route: Route) {
        route.get("/") {
            call.respondText(defaultHtml, ContentType.Text.Html)
        }
        
        route.get("/{...}") {
            call.respond(HttpStatusCode.NotFound)
        }
    }
    
    private suspend fun PipelineContext<Unit, ApplicationCall>.serveResource(path: String) {
        // 尝试从 classpath 加载
        val resourcePath = "web/$path"
        val stream = javaClass.classLoader.getResourceAsStream(resourcePath)
        
        if (stream != null) {
            val bytes = stream.readBytes()
            stream.close()
            val contentType = getContentType(path)
            call.respondBytes(bytes, contentType)
        } else {
            // 尝试 index.html (SPA 路由)
            val indexStream = javaClass.classLoader.getResourceAsStream("web/index.html")
            if (indexStream != null) {
                val bytes = indexStream.readBytes()
                indexStream.close()
                call.respondBytes(bytes, ContentType.Text.Html)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
    
    private fun getContentType(filename: String): ContentType {
        val ext = filename.substringAfterLast(".").lowercase()
        return mimeTypes[ext] ?: ContentType.Application.OctetStream
    }
    
    private val defaultHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>MTRWebCTC</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                    color: #eee;
                    min-height: 100vh;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                }
                .container {
                    text-align: center;
                    padding: 2rem;
                }
                h1 {
                    font-size: 3rem;
                    margin-bottom: 1rem;
                    background: linear-gradient(90deg, #0f0, #0ff);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                }
                p { color: #888; margin-bottom: 2rem; }
                .api-info {
                    background: rgba(255,255,255,0.1);
                    padding: 1.5rem;
                    border-radius: 8px;
                    text-align: left;
                    max-width: 400px;
                }
                code {
                    background: rgba(0,0,0,0.3);
                    padding: 0.2rem 0.5rem;
                    border-radius: 4px;
                }
                a { color: #0ff; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>MTRWebCTC</h1>
                <p>Web-based CTC System for MTR</p>
                <div class="api-info">
                    <p><strong>API Endpoints:</strong></p>
                    <p><code>GET /api/stations</code></p>
                    <p><code>GET /api/routes</code></p>
                    <p><code>GET /api/depots</code></p>
                    <p><code>GET /api/trains</code></p>
                    <p><code>GET /health</code></p>
                    <p style="margin-top: 1rem; font-size: 0.9rem;">
                        <a href="https://github.com/LarkerO/MTRWebCTCMod">GitHub</a>
                    </p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}
