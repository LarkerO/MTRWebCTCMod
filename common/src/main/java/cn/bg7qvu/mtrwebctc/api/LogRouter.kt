package cn.bg7qvu.mtrwebctc.api

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * 日志 API 路由
 */
public class LogRouter {
    private val logs = mutableListOf<Map<String, Any>>()
    
    fun register(route: Route) {
        route.route("/logs") {
            // 获取操作日志
            get {
                try {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    
                    val paginatedLogs = logs
                        .drop(offset)
                        .take(limit)
                    
                    call.respond(mapOf(
                        "logs" to paginatedLogs,
                        "total" to logs.size,
                        "offset" to offset,
                        "limit" to limit
                    ))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 清除日志
            delete {
                try {
                    logs.clear()
                    call.respond(mapOf("success" to true, "message" to "Logs cleared"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
        }
    }
    
    // 记录操作日志
    public fun log(user: String, action: String, target: Map<String, Any>, changes: Map<String, Any>?) {
        logs.add(0, mapOf(
            "timestamp" to System.currentTimeMillis(),
            "user" to user,
            "action" to action,
            "target" to target,
            "changes" to (changes ?: emptyMap<String, Any>())
        ))
        
        // 限制日志数量
        while (logs.size > 10000) {
            logs.removeAt(logs.size - 1)
        }
    }
}
