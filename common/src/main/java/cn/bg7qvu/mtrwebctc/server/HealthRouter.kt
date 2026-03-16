package cn.bg7qvu.mtrwebctc.server

import cn.bg7qvu.mtrwebctc.util.Constants
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Health check routes
 */
class HealthRouter {

    fun register(route: Route) {
        route.get("/health") {
            try {
                val health = mapOf(
                    "status" to "ok",
                    "version" to Constants.MOD_VERSION,
                    "modId" to Constants.MOD_ID,
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

        route.get("/ready") {
            call.respond(mapOf("ready" to true))
        }

        route.get("/live") {
            call.respond(mapOf("alive" to true))
        }
    }
}
