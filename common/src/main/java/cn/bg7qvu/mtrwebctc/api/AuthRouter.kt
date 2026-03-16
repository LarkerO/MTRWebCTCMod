package cn.bg7qvu.mtrwebctc.api

import cn.bg7qvu.mtrwebctc.auth.AuthManager
import cn.bg7qvu.mtrwebctc.util.HashUtil
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AuthRouter(private val authManager: AuthManager) {

    fun register(routing: Route) {
        routing.route("/auth") {
            post("login") {
                try {
                    val request = call.receive<LoginRequest>()
                    if (authManager.login(request.password)) {
                        val token = authManager.generateToken()
                        call.respond(mapOf("token" to token, "success" to true))
                        Logger.info("User logged in successfully")
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("success" to false, "error" to "Invalid password")
                        )
                    }
                } catch (e: Exception) {
                    Logger.error("Login failed: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("success" to false, "error" to "Login failed")
                    )
                }
            }

            post("logout") {
                var token = call.request.headers["Authorization"]
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7)
                    authManager.invalidateToken(token)
                }
                call.respond(mapOf("success" to true))
            }

            post("password") {
                try {
                    val request = call.receive<PasswordRequest>()
                    if (authManager.hasPassword()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("success" to false, "error" to "Password already set")
                        )
                        return@post
                    }
                    authManager.setPassword(request.password)
                    call.respond(mapOf("success" to true))
                    Logger.info("Password set successfully")
                } catch (e: Exception) {
                    Logger.error("Failed to set password: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("success" to false, "error" to "Failed to set password")
                    )
                }
            }

            get("webauthn/register") {
                if (!authManager.isWebauthnEnabled) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "WebAuthn not enabled"))
                    return@get
                }
                call.respond(mapOf("challenge" to HashUtil.generateToken()))
            }

            post("webauthn/verify") {
                if (!authManager.isWebauthnEnabled) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "WebAuthn not enabled"))
                    return@post
                }
                call.respond(mapOf("success" to true))
            }
        }
    }

    class LoginRequest {
        var password: String = ""
    }

    class PasswordRequest {
        var password: String = ""
    }
}
