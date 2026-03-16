package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.model.StationDTO
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class StationRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager
) {

    fun register(routing: Route) {
        routing.route("/stations") {
            get {
                try {
                    val stations = mtrDataManager.allStations
                    call.respond(stations)
                } catch (e: Exception) {
                    Logger.error("Failed to get stations: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get stations: ${e.message}")
                    )
                }
            }

            get("{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID")
                        )
                    val station = mtrDataManager.getStation(id)
                    if (station != null) {
                        call.respond(station)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Station not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to get station: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get station: ${e.message}")
                    )
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
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID")
                        )
                    val station = call.receive<StationDTO>()
                    station.id = id
                    val success = mtrDataManager.updateStation(station)
                    if (success) {
                        call.respond(station)
                        Logger.info("Station $id updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Station not found"))
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID"))
                } catch (e: Exception) {
                    Logger.error("Failed to update station: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update station: ${e.message}")
                    )
                }
            }

            get("{id}/platforms") {
                try {
                    val stationId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid station ID")
                        )
                    call.respond(mtrDataManager.getPlatformsByStation(stationId))
                } catch (e: Exception) {
                    Logger.error("Failed to get platforms: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get platforms: ${e.message}")
                    )
                }
            }
        }
    }
}
