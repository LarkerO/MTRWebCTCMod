package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.backup.BackupManager
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * 备份 API 路由
 */
public class BackupRouter(
    private val backupManager: BackupManager,
    private val authManager: AuthManager
) {
    fun register(route: Route) {
        route.route("/backups") {
            // 获取备份列表
            get {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@get
                    }
                    
                    val backups = backupManager.listBackups()
                    call.respond(mapOf("backups" to backups))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 创建备份
            post {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@post
                    }
                    
                    val backupId = backupManager.createBackup("manual")
                    call.respond(mapOf(
                        "success" to true,
                        "backupId" to backupId,
                        "message" to "Backup created"
                    ))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 恢复备份
            post("/{id}/restore") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@post
                    }
                    
                    val id = call.parameters["id"]
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid backup ID"))
                        return@post
                    }
                    
                    val success = backupManager.restoreBackup(id)
                    if (success) {
                        call.respond(mapOf("success" to true, "message" to "Backup restored"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Backup not found"))
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 删除备份
            delete("/{id}") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@delete
                    }
                    
                    val id = call.parameters["id"]
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid backup ID"))
                        return@delete
                    }
                    
                    val success = backupManager.deleteBackup(id)
                    if (success) {
                        call.respond(mapOf("success" to true, "message" to "Backup deleted"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Backup not found"))
                    }
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
