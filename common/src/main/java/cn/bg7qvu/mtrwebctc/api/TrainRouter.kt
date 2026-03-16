package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.mtr.TrainTracker
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TrainRouter(private val trainTracker: TrainTracker) {

    fun register(routing: Route) {
        routing.route("/trains") {
            get {
                try {
                    val trains = trainTracker.allTrains
                    call.respond(mapOf(
                        "trains" to trains,
                        "count" to trains.size,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    Logger.error("Failed to get trains: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get trains: ${e.message}")
                    )
                }
            }

            get("{id}") {
                try {
                    val trainId = call.parameters["id"]
                    if (trainId.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing train ID"))
                        return@get
                    }
                    val train = trainTracker.getTrain(trainId)
                    if (train != null) {
                        call.respond(train)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Train not found: $trainId")
                        )
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to get train: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get train")
                    )
                }
            }

            get("{id}/history") {
                try {
                    val trainId = call.parameters["id"]
                    if (trainId.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing train ID"))
                        return@get
                    }
                    val history = trainTracker.getTrainHistory(trainId)
                    call.respond(mapOf(
                        "trainId" to trainId,
                        "positions" to history,
                        "count" to history.size,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    Logger.error("Failed to get train history: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get train history: ${e.message}")
                    )
                }
            }

            get("route/{routeId}") {
                try {
                    val routeIdStr = call.parameters["routeId"]
                    if (routeIdStr.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing route ID"))
                        return@get
                    }
                    val routeId = routeIdStr.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid route ID")
                        )
                    val trains = trainTracker.getRouteTrains(routeId)
                    call.respond(mapOf(
                        "routeId" to routeId,
                        "trains" to trains,
                        "count" to trains.size,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    Logger.error("Failed to get route trains: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get route trains")
                    )
                }
            }

            get("depot/{depotId}") {
                try {
                    val depotIdStr = call.parameters["depotId"]
                    if (depotIdStr.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing depot ID"))
                        return@get
                    }
                    val depotId = depotIdStr.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to "Invalid depot ID")
                        )
                    val trains = trainTracker.getDepotTrains(depotId)
                    call.respond(mapOf(
                        "depotId" to depotId,
                        "trains" to trains,
                        "count" to trains.size,
                        "timestamp" to System.currentTimeMillis()
                    ))
                } catch (e: Exception) {
                    Logger.error("Failed to get depot trains: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get depot trains")
                    )
                }
            }
        }
    }
}
