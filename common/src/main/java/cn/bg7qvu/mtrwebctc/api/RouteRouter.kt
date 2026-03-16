package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.model.RouteDTO
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class RouteRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager
) {

    fun register(routing: Route) {
        routing.route("/routes") {
            get {
                try {
                    val routes = mtrDataManager.allRoutes
                    call.respond(routes)
                } catch (e: Exception) {
                    Logger.error("Failed to get routes: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get routes: ${e.message}")
                    )
                }
            }

            get("{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID")
                        )
                    val route = mtrDataManager.getRoute(id)
                    if (route != null) {
                        call.respond(route)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Route not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID"))
                }
            }

            put("{id}") {
                if (!authManager.validateRequest(call)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@put
                }
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID")
                        )
                    val route = call.receive<RouteDTO>()
                    route.id = id
                    val success = mtrDataManager.updateRoute(route)
                    if (success) {
                        call.respond(route)
                        Logger.info("Route $id updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Route not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to update route: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update route: ${e.message}")
                    )
                }
            }

            get("{id}/trains") {
                try {
                    val routeId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID")
                        )
                    val trains = mtrDataManager.getTrainsByRoute(routeId)
                    call.respond(trains)
                } catch (e: Exception) {
                    Logger.error("Failed to get trains: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get trains: ${e.message}")
                    )
                }
            }
        }
    }
}
