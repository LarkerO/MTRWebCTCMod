package cn.bg7qvu.mtrwebctc.middleware

import cn.bg7qvu.mtrwebctc.security.RateLimiter
import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*

/**
 * 速率限制中间件
 */
public class RateLimitMiddleware(private val rateLimiter: RateLimiter) {
    
    public fun install(application: Application) {
        application.intercept(ApplicationCallPipeline.Plugins) {
            val clientIp = call.request.origin.remoteHost
            
            if (!rateLimiter.allowRequest(clientIp)) {
                val retryAfter = rateLimiter.getResetTime(clientIp)
                
                call.response.headers.append(
                    "Retry-After",
                    (retryAfter / 1000).toString()
                )
                
                Logger.warn("Rate limited: $clientIp")
                call.respond(
                    HttpStatusCode.TooManyRequests,
                    mapOf(
                        "error" to "RATE_LIMITED",
                        "message" to "Too many requests. Please slow down.",
                        "retry_after_ms" to retryAfter
                    )
                )
                finish()
            } else {
                call.response.headers.apply {
                    append("X-RateLimit-Remaining", rateLimiter.getRemainingRequests(clientIp).toString())
                    append("X-RateLimit-Reset", rateLimiter.getResetTime(clientIp).toString())
                }
            }
        }
    }
}
