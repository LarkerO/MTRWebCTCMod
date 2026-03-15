package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * 认证 API 路由
 */
public class AuthRouter(private val authManager: AuthManager) {
    fun register(route: Route) {
        route.route("/auth") {
            // 登录
            post("/login") {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val body = call.receive<Map<String, Any>>()
                    val password = body["password"] as? String ?: ""
                    
                    val token = authManager.login(password)
                    if (token != null) {
                        call.respond(mapOf(
                            "success" to true,
                            "token" to token,
                            "message" to "Login successful"
                        ))
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("success" to false, "error" to "Invalid password")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 登出
            post("/logout") {
                try {
                    val token = call.request.headers["Authorization"]
                    authManager.logout(token)
                    call.respond(mapOf("success" to true, "message" to "Logged out"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 设置密码
            post("/set-password") {
                try {
                    val token = call.request.headers["Authorization"]
                    if (authManager.isPasswordRequired() && !authManager.validateToken(token)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                        return@post
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    val body = call.receive<Map<String, Any>>()
                    val password = body["password"] as? String
                    
                    if (password.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password required"))
                        return@post
                    }
                    
                    authManager.setPassword(password)
                    call.respond(mapOf("success" to true, "message" to "Password set"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 检查是否需要密码
            get("/status") {
                try {
                    call.respond(mapOf(
                        "passwordRequired" to authManager.isPasswordRequired(),
                        "webauthnEnabled" to false // TODO: WebAuthn support
                    ))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Unknown error"))
                    )
                }
            }
            
            // 验证 token
            get("/verify") {
                try {
                    val token = call.request.headers["Authorization"]
                    val valid = authManager.validateToken(token)
                    call.respond(mapOf("valid" to valid))
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
