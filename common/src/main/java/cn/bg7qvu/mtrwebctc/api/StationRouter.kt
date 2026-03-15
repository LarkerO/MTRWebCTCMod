package cn.bg7qvu.mtrwebctc.api;

import cn.bg7qvu.mtrwebctc.auth.AuthManager;
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager;
import cn.bg7qvu.mtrwebctc.model.StationDTO;
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * 车站 API 路由
 */
public class StationRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager
) {
    fun register(route: Route) {
        route.route("/stations") {
            // 获取所有车站
            get {
                try {
                    val stations = mtrDataManager.getStations()
                    call.respond(mapOf("stations" to stations))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取单个车站
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID"))
                        return@get
                    }
                    
                    val station = mtrDataManager.getStation(id)
                    if (station == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Station not found"))
                    } else {
                        call.respond(station)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 修改车站
            put("/{id}") {
                try {
                    // 验证权限
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@put
                    }
                    
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID"))
                        return@put
                    }
                    
                    val station = call.receive<StationDTO>()
                    station.setId(id)
                    
                    mtrDataManager.updateStation(station)
                    call.respond(mapOf("success" to true, "message" to "Station updated"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 获取车站站台
            get("/{id}/platforms") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID"))
                        return@get
                    }
                    
                    val platforms = mtrDataManager.getStationPlatforms(id)
                    call.respond(mapOf("platforms" to platforms))
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
