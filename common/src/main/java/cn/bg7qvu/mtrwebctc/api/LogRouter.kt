package cn.bg7qvu.mtrwebctc.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class LogRouter {

    fun register(routing: Route) {
        routing.route("/logs") {
            get {
                // TODO: implement log retrieval
                call.respond(emptyList<Any>())
            }
        }
    }
}
