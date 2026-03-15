package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.model.RouteDTO
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * 线路 API 路由
 */
public class RouteRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager
) {
    fun register(route: Route) {
        route.route("/routes") {
            // 获取所有线路
            get {
                try {
                    val routes = mtrDataManager.getRoutes()
                    call.respond(mapOf("routes" to routes))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取单个线路
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID"))
                        return@get
                    }
                    
                    val route = mtrDataManager.getRoute(id)
                    if (route == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Route not found"))
                    } else {
                        call.respond(route)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 修改线路
            put("/{id}") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@put
                    }
                    
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID"))
                        return@put
                    }
                    
                    val route = call.receive<RouteDTO>()
                    route.setId(id)
                    
                    mtrDataManager.updateRoute(route)
                    call.respond(mapOf("success" to true, "message" to "Route updated"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取线路上的列车
            get("/{id}/trains") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID"))
                        return@get
                    }
                    
                    val trains = mtrDataManager.getRouteTrains(id)
                    call.respond(mapOf("trains" to trains))
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
