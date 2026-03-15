package cn.bg7qvu.mtrwebctc.middleware;

import cn.bg7qvu.mtrwebctc.metrics.MetricsCollector;
import io.ktor.server.application.*
import io.ktor.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * 指标收集中间件
 */
public class MetricsMiddleware(private val metrics: MetricsCollector) {
    
    private val requestCounter = AtomicLong(0)
    
    public fun install(application: Application) {
        application.intercept(ApplicationCallPipeline.Monitoring) {
            val startTime = System.currentTimeMillis()
            val requestId = requestCounter.incrementAndGet()
            
            // 请求开始
            call.attributes.put(RequestKey, requestId)
            
            try {
                proceed()
            } finally {
                // 请求结束，记录指标
                val duration = System.currentTimeMillis() - startTime
                val endpoint = extractEndpoint(call.request.path())
                val method = call.request.httpMethod.value
                val status = call.response.status()?.value ?: 0
                
                metrics.recordRequest(endpoint, method, status, duration)
            }
        }
    }
    
    private fun extractEndpoint(path: String): String {
        // 将带 ID 的路径转为模板：/api/stations/123 -> /api/stations/{id}
        val segments = path.split("/")
        return segments.mapIndexed { index, segment ->
            if (index > 0 && segment.all { it.isDigit() }) "{id}" else segment
        }.joinToString("/")
    }
    
    companion object {
        private val RequestKey = AttributeKey<Long>("request_id")
    }
}
