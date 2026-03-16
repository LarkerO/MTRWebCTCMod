package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod
import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.config.Config
import cn.bg7qvu.mtrwebctc.config.ConfigLoader
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ConfigRouter(
    private val config: Config,
    private val authManager: AuthManager
) {

    private suspend fun checkAuth(call: ApplicationCall): Boolean {
        val token = call.request.headers["Authorization"]
        if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            return false
        }
        return true
    }

    fun register(routing: Route) {
        routing.route("/config") {
            get {
                if (!checkAuth(call)) return@get

                val safeConfig = linkedMapOf<String, Any>(
                    "server" to mapOf(
                        "port" to config.server.port,
                        "bind" to config.server.bind,
                        "staticResourceMode" to config.server.staticResourceMode
                    ),
                    "trainTracker" to mapOf(
                        "positionUpdateIntervalMs" to config.trainTracker.positionUpdateIntervalMs,
                        "historyRetentionMinutes" to config.trainTracker.historyRetentionMinutes
                    ),
                    "websocket" to mapOf(
                        "pushIntervalMs" to config.websocket.pushIntervalMs
                    ),
                    "backup" to mapOf(
                        "enabled" to config.backup.isEnabled,
                        "maxBackups" to config.backup.maxBackups
                    ),
                    "storage" to mapOf(
                        "backend" to config.storage.backend
                    ),
                    "auth" to mapOf(
                        "webauthnEnabled" to config.auth.isWebauthnEnabled
                    )
                )
                call.respond(safeConfig)
            }

            put {
                if (!checkAuth(call)) return@put

                try {
                    @Suppress("UNCHECKED_CAST")
                    val body = call.receive<Map<String, Any>>()

                    @Suppress("UNCHECKED_CAST")
                    val serverMap = body["server"] as? Map<String, Any>
                    if (serverMap != null) {
                        val port = (serverMap["port"] as? Number)?.toInt()
                        if (port != null) {
                            if (port < 1 || port > 65535) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Invalid port")
                                )
                                return@put
                            }
                            config.server.port = port
                        }
                        val bind = serverMap["bind"] as? String
                        if (bind != null) {
                            config.server.bind = bind
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val backupMap = body["backup"] as? Map<String, Any>
                    if (backupMap != null) {
                        val enabled = backupMap["enabled"] as? Boolean
                        if (enabled != null) {
                            config.backup.isEnabled = enabled
                        }
                        val maxBackups = (backupMap["maxBackups"] as? Number)?.toInt()
                        if (maxBackups != null) {
                            config.backup.maxBackups = maxBackups
                        }
                    }

                    val mod = MTRWebCTCMod.getInstance()
                    if (mod != null) {
                        ConfigLoader.save(mod.configDir, config)
                    }

                    call.respond(mapOf("success" to true))
                    Logger.info("Configuration updated successfully")
                } catch (e: Exception) {
                    Logger.error("Failed to update config: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update config")
                    )
                }
            }

            post("reload") {
                if (!checkAuth(call)) return@post

                try {
                    val mod = MTRWebCTCMod.getInstance()
                    if (mod != null) {
                        ConfigLoader.reload(mod.configDir)
                    }
                    call.respond(mapOf("success" to true))
                    Logger.info("Configuration reloaded successfully")
                } catch (e: Exception) {
                    Logger.error("Failed to reload config: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to reload config")
                    )
                }
            }
        }
    }
}
