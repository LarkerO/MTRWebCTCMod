package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.backup.BackupManager
import cn.bg7qvu.mtrwebctc.mtr.MTRDataManager
import cn.bg7qvu.mtrwebctc.model.DepotDTO
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class DepotRouter(
    private val mtrDataManager: MTRDataManager,
    private val authManager: AuthManager,
    private val backupManager: BackupManager
) {

    fun register(routing: Route) {
        routing.route("/depots") {
            get {
                try {
                    val depots = mtrDataManager.allDepots
                    call.respond(depots)
                } catch (e: Exception) {
                    Logger.error("Failed to get depots: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get depots: ${e.message}")
                    )
                }
            }

            get("{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID")
                        )
                    val depot = mtrDataManager.getDepot(id)
                    if (depot != null) {
                        call.respond(depot)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Depot not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID"))
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
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID")
                        )
                    val depot = call.receive<DepotDTO>()
                    depot.id = id

                    backupManager.createBackup("before-depot-update-$id")

                    val success = mtrDataManager.updateDepot(depot)
                    if (success) {
                        call.respond(depot)
                        Logger.info("Depot $id updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Depot not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to update depot: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update depot: ${e.message}")
                    )
                }
            }

            put("{id}/schedule") {
                if (!authManager.validateRequest(call)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@put
                }
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID")
                        )
                    val request = call.receive<ScheduleUpdateRequest>()

                    backupManager.createBackup("before-schedule-update-$id")

                    val success = mtrDataManager.updateDepotSchedule(
                        id,
                        request.departures,
                        request.frequencies,
                        request.useRealTime,
                        request.repeatInfinitely
                    )
                    if (success) {
                        call.respond(mapOf("success" to true))
                        Logger.info("Depot $id schedule updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Depot not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to update schedule: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update schedule: ${e.message}")
                    )
                }
            }

            get("{id}/trains") {
                try {
                    val depotId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID")
                        )
                    val trains = mtrDataManager.getTrainsByDepot(depotId)
                    call.respond(trains)
                } catch (e: Exception) {
                    Logger.error("Failed to get depot trains: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get depot trains: ${e.message}")
                    )
                }
            }
        }
    }

    class ScheduleUpdateRequest {
        var departures: List<Long>? = null
        var frequencies: List<Int>? = null
        var useRealTime: Boolean = false
        var repeatInfinitely: Boolean = false
    }
}
