package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker
import cn.bg7qvu.mtrwebctc.model.DepotDTO
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * 车厂 API 路由
 */
public class DepotRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager,
    private val trainTracker: TrainTracker
) {
    fun register(route: Route) {
        route.route("/depots") {
            // 获取所有车厂
            get {
                try {
                    val depots = mtrDataManager.getDepots()
                    call.respond(mapOf("depots" to depots))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取单个车厂
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID"))
                        return@get
                    }
                    
                    val depot = mtrDataManager.getDepot(id)
                    if (depot == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Depot not found"))
                    } else {
                        call.respond(depot)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 修改车厂
            put("/{id}") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@put
                    }
                    
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID"))
                        return@put
                    }
                    
                    val depot = call.receive<DepotDTO>()
                    depot.setId(id)
                    
                    mtrDataManager.updateDepot(depot)
                    call.respond(mapOf("success" to true, "message" to "Depot updated"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 修改发车时间表
            put("/{id}/schedule") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@put
                    }
                    
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID"))
                        return@put
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    val body = call.receive<Map<String, Any>>()
                    val departures = (body["departures"] as? List<*>)?.filterIsInstance<Int>() ?: emptyList()
                    val frequencies = (body["frequencies"] as? List<*>)?.filterIsInstance<Int>() ?: emptyList()
                    val useRealTime = body["useRealTime"] as? Boolean ?: false
                    val repeatInfinitely = body["repeatInfinitely"] as? Boolean ?: true
                    
                    mtrDataManager.updateDepotSchedule(id, departures, frequencies, useRealTime, repeatInfinitely)
                    call.respond(mapOf("success" to true, "message" to "Schedule updated"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取车厂内的列车
            get("/{id}/trains") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID"))
                        return@get
                    }
                    
                    val trains = trainTracker.getDepotTrains(id)
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
