package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.config.Config
import cn.bg7qvu.mtrwebctc.config.ConfigLoader
import cn.bg7qvu.mtrwebctc.MTRWebCTCMod
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * 配置 API 路由
 */
public class ConfigRouter(
    private val config: Config,
    private val authManager: AuthManager
) {
    fun register(route: Route) {
        route.route("/config") {
            // 获取配置
            get {
                try {
                    // 返回配置，但隐藏敏感信息
                    val safeConfig = mapOf(
                        "server" to mapOf(
                            "port" to config.getServer().getPort(),
                            "bind" to config.getServer().getBind(),
                            "staticResourceMode" to config.getServer().getStaticResourceMode()
                        ),
                        "trainTracker" to mapOf(
                            "positionUpdateIntervalMs" to config.getTrainTracker().getPositionUpdateIntervalMs(),
                            "historyRetentionMinutes" to config.getTrainTracker().getHistoryRetentionMinutes()
                        ),
                        "websocket" to mapOf(
                            "pushIntervalMs" to config.getWebsocket().getPushIntervalMs()
                        ),
                        "backup" to mapOf(
                            "enabled" to config.getBackup().isEnabled(),
                            "maxBackups" to config.getBackup().getMaxBackups()
                        ),
                        "storage" to mapOf(
                            "backend" to config.getStorage().getBackend()
                        ),
                        "auth" to mapOf(
                            "webauthnEnabled" to config.getAuth().isWebauthnEnabled()
                        )
                    )
                    call.respond(safeConfig)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 修改配置
            put {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@put
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    val body = call.receive<Map<String, Any>>()
                    
                    // 更新服务器配置
                    val server = body["server"] as? Map<String, Any>
                    if (server != null) {
                        (server["port"] as? Number)?.let { config.getServer().setPort(it.toInt()) }
                        (server["bind"] as? String)?.let { config.getServer().setBind(it) }
                        (server["staticResourceMode"] as? String)?.let { config.getServer().setStaticResourceMode(it) }
                    }
                    
                    // 更新列车追踪配置
                    val trainTracker = body["trainTracker"] as? Map<String, Any>
                    if (trainTracker != null) {
                        (trainTracker["positionUpdateIntervalMs"] as? Number)?.let { 
                            config.getTrainTracker().setPositionUpdateIntervalMs(it.toLong()) 
                        }
                        (trainTracker["historyRetentionMinutes"] as? Number)?.let { 
                            config.getTrainTracker().setHistoryRetentionMinutes(it.toInt()) 
                        }
                    }
                    
                    // 更新备份配置
                    val backup = body["backup"] as? Map<String, Any>
                    if (backup != null) {
                        (backup["enabled"] as? Boolean)?.let { config.getBackup().setEnabled(it) }
                        (backup["maxBackups"] as? Number)?.let { config.getBackup().setMaxBackups(it.toInt()) }
                    }
                    
                    // 保存配置
                    ConfigLoader.save(MTRWebCTCMod.getInstance().getConfigDir(), config)
                    
                    call.respond(mapOf("success" to true, "message" to "Config updated"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 重载配置
            post("/reload") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@post
                    }
                    
                    ConfigLoader.reload(MTRWebCTCMod.getInstance().getConfigDir())
                    call.respond(mapOf("success" to true, "message" to "Config reloaded"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
        }
    }
}
