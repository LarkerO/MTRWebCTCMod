package cn.bg7qvu.mtrwebctc.exception;

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import cn.bg7qvu.mtrwebctc.util.Logger

/**
 * 全局异常处理器
 */
public object ExceptionHandler {
    
    /**
     * 注册异常处理器
     */
    public fun register(application: Application) {
        application.install(StatusPages) {
            exception<MTRWebCTCException> { call, cause ->
                Logger.error("MTRWebCTC error: ${cause.errorCode} - ${cause.message}")
                
                val statusCode = when (cause) {
                    is MTRWebCTCException.NotFoundException -> HttpStatusCode.NotFound
                    is MTRWebCTCException.AuthException -> HttpStatusCode.Unauthorized
                    is MTRWebCTCException.RateLimitException -> HttpStatusCode.TooManyRequests
                    is MTRWebCTCException.ValidationException -> HttpStatusCode.BadRequest
                    else -> HttpStatusCode.InternalServerError
                }
                
                val response = mutableMapOf(
                    "error" to cause.errorCode,
                    "message" to (cause.message ?: "Unknown error")
                )
                
                if (cause is MTRWebCTCException.RateLimitException) {
                    response["retry_after_ms"] = cause.retryAfterMs
                }
                
                call.respond(statusCode, response)
            }
            
            exception<IllegalArgumentException> { call, cause ->
                Logger.error("Invalid argument: ${cause.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to "INVALID_ARGUMENT",
                    "message" to (cause.message ?: "Invalid argument")
                ))
            }
            
            exception<Exception> { call, cause ->
                Logger.error("Unhandled exception: ${cause.javaClass.simpleName} - ${cause.message}")
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "INTERNAL_ERROR",
                    "message" to "An unexpected error occurred"
                ))
            }
            
            status(HttpStatusCode.NotFound) { call, _ ->
                call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "NOT_FOUND",
                    "message" to "Resource not found"
                ))
            }
            
            status(HttpStatusCode.Unauthorized) { call, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf(
                    "error" to "UNAUTHORIZED",
                    "message" to "Authentication required"
                ))
            }
            
            status(HttpStatusCode.Forbidden) { call, _ ->
                call.respond(HttpStatusCode.Forbidden, mapOf(
                    "error" to "FORBIDDEN",
                    "message" to "Access denied"
                ))
            }
            
            status(HttpStatusCode.MethodNotAllowed) { call, _ ->
                call.respond(HttpStatusCode.MethodNotAllowed, mapOf(
                    "error" to "METHOD_NOT_ALLOWED",
                    "message" to "HTTP method not supported"
                ))
            }
        }
    }
}
