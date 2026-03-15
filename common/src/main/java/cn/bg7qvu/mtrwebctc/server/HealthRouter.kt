package cn.bg7qvu.mtrwebctc.server;

import cn.bg7qvu.mtrwebctc.MTRWebCTCMod;
import io.ktor.server.application.*;
import io.ktor.server.routing.*;
import io.ktor.server.response.*;
import io.ktor.http.*;

/**
 * 健康检查路由
 */
public class HealthRouter {
    private final MTRWebCTCMod mod;
    
    public HealthRouter(MTRWebCTCMod mod) {
        this.mod = mod;
    }
    
    public void register(route: Route) {
        route.get("/health") {
            try {
                val health = mapOf(
                    "status" to "ok",
                    "version" to mod.getVersion(),
                    "uptime" to mod.getUptime(),
                    "minecraft" to mapOf(
                        "version" to mod.getMinecraftVersion(),
                        "loader" to mod.getLoaderType()
                    ),
                    "mtr" to mapOf(
                        "version" to mod.getMTRVersion(),
                        "loaded" to mod.isMTRLoaded()
                    ),
                    "timestamp" to System.currentTimeMillis()
                )
                call.respond(health)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("status" to "error", "message" to (e.message ?: "Unknown error"))
                )
            }
        }
        
        // 就绪探针
        route.get("/ready") {
            if (mod.isReady()) {
                call.respond(mapOf("ready" to true))
            } else {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf("ready" to false, "message" to "Server not ready")
                )
            }
        }
        
        // 存活探针
        route.get("/live") {
            call.respond(mapOf("alive" to true))
        }
    }
}
