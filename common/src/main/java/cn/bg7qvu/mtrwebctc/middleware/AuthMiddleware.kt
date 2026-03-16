package cn.bg7qvu.mtrwebctc.middleware

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.exception.MTRWebCTCException
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * 认证中间件
 */
public class AuthMiddleware(private val authManager: AuthManager) {
    
    // 不需要认证的路径
    private val publicPaths = setOf(
        "/health",
        "/ready",
        "/live",
        "/api/auth/login",
        "/api/auth/token",
        "/favicon.ico"
    )
    
    public fun install(application: Application) {
        application.intercept(ApplicationCallPipeline.Plugins) {
            val path = call.request.path()
            
            // 跳过公开路径
            if (isPublicPath(path)) {
                return@intercept
            }
            
            // 如果不需要密码，跳过认证
            if (!authManager.isPasswordRequired()) {
                return@intercept
            }
            
            // 检查 token
            val authHeader = call.request.headers["Authorization"]
            
            if (authHeader == null) {
                Logger.debug("Missing auth header for: $path")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "UNAUTHORIZED",
                        "message" to "Authentication required"
                    )
                )
                finish()
                return@intercept
            }
            
            val token = extractToken(authHeader)
            
            if (token == null || !authManager.validateToken(token)) {
                Logger.debug("Invalid token for: $path")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "INVALID_TOKEN",
                        "message" to "Invalid or expired token"
                    )
                )
                finish()
                return@intercept
            }
            
            // 认证成功，继续
        }
    }
    
    private fun isPublicPath(path: String): Boolean {
        // 静态资源
        if (path.startsWith("/api/auth/")) return true
        if (publicPaths.contains(path)) return true
        if (path.startsWith("/web/")) return true
        if (path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".html")) return true
        return false
    }
    
    private fun extractToken(authHeader: String): String? {
        return when {
            authHeader.startsWith("Bearer ", ignoreCase = true) -> 
                authHeader.substring(7).trim()
            authHeader.startsWith("Token ", ignoreCase = true) -> 
                authHeader.substring(6).trim()
            else -> authHeader.trim()
        }
    }
}
