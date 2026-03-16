package cn.bg7qvu.mtrwebctc.server

import cn.bg7qvu.mtrwebctc.api.*
import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.backup.BackupManager
import cn.bg7qvu.mtrwebctc.config.Config
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker
import cn.bg7qvu.mtrwebctc.util.Logger
import cn.bg7qvu.mtrwebctc.websocket.WebSocketHandler
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

class WebServer(
    private val config: Config,
    private val mtrDataManager: MTRDataManager,
    private val trainTracker: TrainTracker,
    private val backupManager: BackupManager
) {
    private val authManager = AuthManager(config)
    private val webSocketHandler = WebSocketHandler(config, mtrDataManager, trainTracker)
    private var server: ApplicationEngine? = null

    fun start() {
        Logger.info("Starting web server on ${config.server.bind}:${config.server.port}")

        server = embeddedServer(Netty, port = config.server.port, host = config.server.bind) {
            install(ContentNegotiation) {
                gson { }
            }
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (cause.message ?: "Unknown error"))
                    )
                }
            }
            install(WebSockets)

            routing {
                get("/") {
                    call.respondText(
                        "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>MTRWebCTC</title></head>" +
                            "<body><h1>MTRWebCTC API</h1><p>Web interface coming soon...</p>" +
                            "<h2>API Endpoints</h2><ul>" +
                            "<li>POST /api/auth/login - Login</li>" +
                            "<li>GET /api/stations - List stations</li>" +
                            "<li>GET /api/routes - List routes</li>" +
                            "<li>GET /api/depots - List depots</li>" +
                            "<li>GET /api/trains - List trains</li>" +
                            "</ul></body></html>",
                        ContentType.Text.Html
                    )
                }

                get("/health") {
                    call.respond(mapOf("status" to "ok"))
                }

                route("/api") {
                    AuthRouter(authManager).register(this)
                    StationRouter(mtrDataManager, authManager).register(this)
                    RouteRouter(mtrDataManager, authManager).register(this)
                    DepotRouter(mtrDataManager, authManager, backupManager).register(this)
                    TrainRouter(trainTracker).register(this)
                    ConfigRouter(config, authManager).register(this)
                    BackupRouter(backupManager, authManager).register(this)
                    LogRouter().register(this)
                    SystemRouter().register(this)
                }

                webSocketHandler.register(this)
            }
        }

        server?.start(wait = false)
        Logger.info("Web server started successfully")
    }

    fun stop() {
        if (server != null) {
            Logger.info("Stopping web server...")
            webSocketHandler.stop()
            server?.stop(1000, 5000)
            Logger.info("Web server stopped")
        }
    }
}
