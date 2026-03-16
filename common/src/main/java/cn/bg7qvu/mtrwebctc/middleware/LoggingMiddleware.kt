package cn.bg7qvu.mtrwebctc.middleware

import cn.bg7qvu.mtrwebctc.util.Logger
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * 请求日志中间件
 */
public class LoggingMiddleware {
    
    public fun install(application: Application) {
        application.intercept(ApplicationCallPipeline.Monitoring) {
            val startTime = System.currentTimeMillis()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val clientIp = call.request.origin.remoteHost
            
            // 请求开始日志
            Logger.debug("-> $method $path from $clientIp")
            
            try {
                proceed()
            } finally {
                // 请求结束日志
                val duration = System.currentTimeMillis() - startTime
                val status = call.response.status()?.value ?: 0
                
                val logMessage = "<- $method $path $status ${duration}ms"
                
                when {
                    status >= 500 -> Logger.error(logMessage)
                    status >= 400 -> Logger.warn(logMessage)
                    else -> Logger.debug(logMessage)
                }
            }
        }
    }
}
