package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.mtr.TrainTracker
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * 列车 API 路由
 */
public class TrainRouter(
    private val mtrDataManager: MTRDataManager,
    private val trainTracker: TrainTracker
) {
    fun register(route: Route) {
        route.route("/trains") {
            // 获取所有列车
            get {
                try {
                    val trains = trainTracker.getAllTrains()
                    call.respond(mapOf(
                        "trains" to trains,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取单个列车
            get("/{id}") {
                try {
                    val id = call.parameters["id"]
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid train ID"))
                        return@get
                    }
                    
                    val train = trainTracker.getTrain(id)
                    if (train == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Train not found"))
                    } else {
                        call.respond(train)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取列车历史轨迹
            get("/{id}/history") {
                try {
                    val id = call.parameters["id"]
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid train ID"))
                        return@get
                    }
                    
                    val history = trainTracker.getTrainHistory(id)
                    call.respond(mapOf(
                        "trainId" to id,
                        "history" to history,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 按线路查询列车
            get("/route/{routeId}") {
                try {
                    val routeId = call.parameters["routeId"]?.toLongOrNull()
                    if (routeId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID"))
                        return@get
                    }
                    
                    val trains = trainTracker.getRouteTrains(routeId)
                    call.respond(mapOf(
                        "routeId" to routeId,
                        "trains" to trains,
                        "timestamp" to System.currentTimeMillis()
                    ))
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
