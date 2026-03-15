package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.metrics.MetricsCollector;
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

/**
 * 系统 API 路由
 */
public class SystemRouter(
    private val mod: MTRWebCTCMod,
    private val metrics: MetricsCollector,
    private val authManager: AuthManager
) {
    public fun register(route: Route) {
        route.route("/system") {
            // 系统信息
            get("/info") {
                val info = mapOf(
                    "mod" to mapOf(
                        "id" to "mtrwebctc",
                        "version" to mod.getVersion(),
                        "uptime_ms" to mod.getUptime()
                    ),
                    "minecraft" to mapOf(
                        "version" to mod.getMinecraftVersion(),
                        "loader" to mod.getLoaderType()
                    ),
                    "mtr" to mapOf(
                        "version" to mod.getMTRVersion(),
                        "loaded" to mod.isMTRLoaded()
                    ),
                    "java" to mapOf(
                        "version" to System.getProperty("java.version"),
                        "vendor" to System.getProperty("java.vendor")
                    ),
                    "os" to mapOf(
                        "name" to System.getProperty("os.name"),
                        "version" to System.getProperty("os.version"),
                        "arch" to System.getProperty("os.arch")
                    )
                )
                call.respond(info)
            }
            
            // 指标
            get("/metrics") {
                val token = call.request.headers["Authorization"]
                if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@get
                }
                
                call.respond(metrics.getSummary())
            }
            
            // 端点统计
            get("/metrics/endpoints") {
                val token = call.request.headers["Authorization"]
                if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@get
                }
                
                call.respond(metrics.getEndpointStats())
            }
            
            // WebSocket 统计
            get("/ws/stats") {
                // TODO: 从 WebSocketHandler 获取统计
                call.respond(mapOf(
                    "connections" to 0,
                    "channels" to emptyMap<String, Int>()
                ))
            }
        }
    }
}
