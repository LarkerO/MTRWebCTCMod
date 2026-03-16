package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.backup.BackupManager
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class BackupRouter(
    private val backupManager: BackupManager,
    private val authManager: AuthManager
) {

    fun register(routing: Route) {
        routing.route("/backups") {
            get {
                try {
                    val backups = backupManager.backupList
                    call.respond(backups)
                } catch (e: Exception) {
                    Logger.error("Failed to get backups: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get backups")
                    )
                }
            }

            post {
                if (!authManager.validateRequest(call)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@post
                }
                try {
                    val backupId = backupManager.createBackup("manual")
                    call.respond(mapOf("success" to true, "backupId" to backupId))
                    Logger.info("Manual backup created: $backupId")
                } catch (e: Exception) {
                    Logger.error("Failed to create backup: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to create backup")
                    )
                }
            }

            post("{id}/restore") {
                if (!authManager.validateRequest(call)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@post
                }
                try {
                    val backupId = call.parameters["id"]
                    val success = backupManager.restoreBackup(backupId)
                    if (success) {
                        call.respond(mapOf("success" to true))
                        Logger.info("Backup restored: $backupId")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Backup not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to restore backup: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to restore backup")
                    )
                }
            }

            delete("{id}") {
                if (!authManager.validateRequest(call)) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@delete
                }
                try {
                    val backupId = call.parameters["id"]
                    val success = backupManager.deleteBackup(backupId)
                    if (success) {
                        call.respond(mapOf("success" to true))
                        Logger.info("Backup deleted: $backupId")
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Backup not found"))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to delete backup: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to delete backup")
                    )
                }
            }
        }
    }
}
